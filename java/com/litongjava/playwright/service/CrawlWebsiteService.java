package com.litongjava.playwright.service;

import java.util.HashSet;
import java.util.concurrent.locks.Lock;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.util.concurrent.Striped;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.playwright.consts.TableNames;
import com.litongjava.playwright.instance.PlaywrightBrowser;
import com.litongjava.tio.utils.hutool.StrUtil;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CrawlWebsiteService {

  // 使用Guava的Striped锁，设置64个锁段
  private static final Striped<Lock> stripedLocks = Striped.lock(64);

  public void craw(String baseUrl, String url) {
    // 先判断当前URL是否已经爬取，避免重复操作
    if (Db.exists(TableNames.cache_table_name, "url", url)) {
      return;
    }

    // 获取页面内容及解析
    String content = PlaywrightBrowser.getBodyHtml(url);
    Document doc = Jsoup.parse(content, url);

    Elements links = doc.select("a[href]");
    HashSet<String> linkSet = new HashSet<>();
    for (Element link : links) {
      String subUrl = link.absUrl("href");
      String normalizedSubUrl = normalizeUrl(subUrl);
      if (normalizedSubUrl.startsWith(normalizeUrl(baseUrl))) {
        linkSet.add(normalizedSubUrl);
      }
    }
    log.info("url:{} sub link size:{}", url, linkSet.size());

    // 遍历子链接前，也可以在此对每个子链接先判断是否已爬取
    for (String link : linkSet) {
      craw(baseUrl, link);
    }

    // 使用Striped锁，防止并发重复爬取（此时再做双重检查）
    Lock lock = stripedLocks.get(url);
    lock.lock();
    try {
      if (Db.exists(TableNames.cache_table_name, "url", url)) {
        return;
      }

      BrowserContext context = PlaywrightBrowser.acquire();
      String html = null;
      String bodyText = null;
      String title = null;
      try (Page page = context.newPage()) {
        page.navigate(url);
        bodyText = page.innerText("body");
        title = page.title();
        html = page.content();
      } catch (Exception e) {
        log.error("Error getting content from {}: {}", url, e.getMessage(), e);
      } finally {
        PlaywrightBrowser.release(context);
      }

      if (StrUtil.isNotBlank(bodyText)) {
        Row newRow = new Row();
        newRow.set("id", SnowflakeIdUtils.id()).set("url", url).set("title", title).set("text", bodyText).set("html", html);
        Db.save(TableNames.cache_table_name, newRow);
      }
    } finally {
      lock.unlock();
    }
  }

  public String normalizeUrl(String url) {
    int index = url.indexOf("#");
    return (index == -1) ? url : url.substring(0, index);
  }

}
