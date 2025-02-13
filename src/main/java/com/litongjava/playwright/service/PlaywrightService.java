package com.litongjava.playwright.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;

import com.google.common.util.concurrent.Striped;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.model.web.WebPageContent;
import com.litongjava.playwright.instance.PlaywrightBrowser;
import com.litongjava.tio.utils.hutool.FilenameUtils;
import com.litongjava.tio.utils.hutool.StrUtil;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;
import com.litongjava.tio.utils.thread.TioThreadUtils;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * PlaywrightService 负责爬取网页并将结果缓存至数据库。
 */
@Slf4j
public class PlaywrightService {

  // 数据表名称
  public static final String cache_table_name = "web_page_cache";

  // 使用Guava的Striped锁，设置64个锁段
  private static final Striped<Lock> stripedLocks = Striped.lock(64);

  /**
   * 批量异步抓取网页内容
   *
   * @param pages 包含url的WebPageConteont列表
   * @return 返回同一个列表，但其中的content属性已被填充或保持为空
   */
  public List<WebPageContent> spiderAsync(List<WebPageContent> pages) {
    List<Future<String>> futures = new ArrayList<>();

    // 为每个页面启动一个异步任务
    for (int i = 0; i < pages.size(); i++) {
      String link = pages.get(i).getUrl();

      Future<String> future = TioThreadUtils.submit(() -> {
        // 若后缀为pdf等其他非网页格式，直接跳过
        String suffix = FilenameUtils.getSuffix(link);
        if ("pdf".equalsIgnoreCase(suffix)) {
          log.info("skip: {}", link);
          return null;
        } else {
          // 爬取并返回文本内容
          return getPageContent(link);
        }
      });
      futures.add(i, future);
    }

    // 等待所有任务执行完成，并将结果填充回pages
    for (int i = 0; i < pages.size(); i++) {
      Future<String> future = futures.get(i);
      try {
        String result = future.get();
        if (StrUtil.isNotBlank(result)) {
          pages.get(i).setContent(result);
        }
      } catch (InterruptedException | ExecutionException e) {
        log.error("Error retrieving task result: {}", e.getMessage(), e);
      }
    }
    return pages;
  }

  /**
   * 通过URL获取页面内容；若数据库有缓存则直接返回，否则利用Playwright实际爬取并写入缓存。
   *
   * @param link 要抓取的URL
   * @return 页面文本内容
   */
  public String getPageContent(String link) {
    // 先检查数据库缓存
    if (Db.exists(cache_table_name, "url", link)) {
      // 此处可以读取 text 或 html 等字段
      return Db.queryStr("SELECT text FROM " + cache_table_name + " WHERE url = ?", link);
    }

    // 使用Striped锁，为每个URL生成一把独立的锁，避免并发重复爬取
    Lock lock = stripedLocks.get(link);
    lock.lock();
    try {
      // 双重检查，防止其他线程已在获取锁后写入
      if (Db.exists(cache_table_name, "url", link)) {
        return Db.queryStr("SELECT text FROM " + cache_table_name + " WHERE url = ?", link);
      }

      // 使用 PlaywrightBrowser 获取context对象，执行真实的网页爬取
      BrowserContext context = PlaywrightBrowser.acquire();
      String html = null;
      String bodyText = null;
      String title = null;
      try (Page page = context.newPage()) {
        page.navigate(link);
        // 获取文本内容
        bodyText = page.innerText("body");
        title = page.title();
        // 获取完整HTML
        html = page.content();
      } catch (Exception e) {
        log.error("Error getting content from {}: {}", link, e.getMessage(), e);
      } finally {
        // 归还context
        PlaywrightBrowser.release(context);
      }

      // 成功获取到的内容写入数据库缓存
      if (StrUtil.isNotBlank(bodyText)) {
        Row newRow = new Row();
        newRow.set("id", SnowflakeIdUtils.id()).set("url", link).set("title", title)
            //
            .set("text", bodyText).set("html", html);
        Db.save(cache_table_name, newRow);
      }

      return bodyText;
    } finally {
      lock.unlock();
    }
  }
}