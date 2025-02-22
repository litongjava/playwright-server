package com.litongjava.playwright.service;

import java.util.concurrent.locks.Lock;

import com.google.common.util.concurrent.Striped;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.model.web.WebPageContent;
import com.litongjava.playwright.consts.TableNames;
import com.litongjava.playwright.pool.PlaywrightPool;
import com.litongjava.playwright.utils.PDFUtils;
import com.litongjava.tio.utils.hutool.FilenameUtils;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

public class WebPageCrawlService {

  private final Striped<Lock> stripedLocks = Striped.lock(256);

  /**
   * cache
   * @param url
   * @return
   */
  public String getPdfContent(String url) {
    Lock lock = stripedLocks.get(url);
    lock.lock();
    try {
      String sql = "select html from %s where type=? and url=?";
      sql = String.format(sql, TableNames.web_page_cache);
      String title = FilenameUtils.getBaseName(url);
      //db cache
      String content = Db.queryStr(sql, "pdf", url);
      if (content != null) {
        return content;
      }
      // http
      content = PDFUtils.getContent(url);
      content = content.replace("\u0000", "").trim();
      Row row = new Row().set("id", SnowflakeIdUtils.id()).set("url", url).set("title", title).set("type", "pdf")
          //
          .set("html", content);
      Db.save(TableNames.web_page_cache, row);
      return content;
    } finally {
      lock.unlock();
    }

  }

  public WebPageContent getHtml(String url) throws InterruptedException {
    Lock lock = stripedLocks.get(url);
    lock.lock();
    try {
      String sql = "select title,html from %s where type=? and url=?";
      sql = String.format(sql, TableNames.web_page_cache);
      //db cache
      Row first = Db.findFirst(sql, "html", url);

      if (first != null) {
        String title = first.getStr("title");
        String html = first.getString("html");
        WebPageContent content = new WebPageContent(title, url).setContent(html);
        return content;
      }
      // http
      try (Page page = PlaywrightPool.acquirePage()) {
        // 设置页面超时时间为 1 分钟（60000ms）
        page.setDefaultNavigationTimeout(60000);
        page.setDefaultTimeout(60000);
        // 控制爬取速率
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        // 导航至目标 URL
        page.navigate(encodeUrl(url));
        // 等待页面达到网络空闲状态和加载完成状态
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForLoadState(LoadState.LOAD);
        String html = page.content();
        String title = page.title();

        Row row = new Row().set("id", SnowflakeIdUtils.id()).set("url", url).set("title", title).set("type", "html")
            //
            .set("html", html);
        Db.save(TableNames.web_page_cache, row);
        return new WebPageContent(title, url, "", html);
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * 对 URL 进行预处理，针对路径中的非法字符进行编码。
   *
   * @param url 原始 URL
   * @return 处理后的 URL
   */
  private String encodeUrl(String url) {
    if (url == null || url.isEmpty()) {
      return url;
    }
    return url.replace("[", "%5B").replace("]", "%5D");
  }
}