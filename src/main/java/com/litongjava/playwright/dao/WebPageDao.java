package com.litongjava.playwright.dao;

import java.util.concurrent.locks.Lock;

import org.jsoup.Jsoup;

import com.google.common.util.concurrent.Striped;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.playwright.utils.WebsiteUrlUtils;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

public class WebPageDao {
  private final Striped<Lock> stripedLocks = Striped.lock(256);

  public boolean exists(String cacheTableName, String field, String value) {
    return Db.exists(cacheTableName, field, value);
  }

  public void saveMarkdown(String tableName, String url, String title, String type, String markdown) {
    String canonical = WebsiteUrlUtils.canonicalizeUrl(url);
    if (exists(tableName, "url", canonical)) {
      return;
    }

    Lock lock = stripedLocks.get(canonical);
    lock.lock();
    try {
      if (exists(tableName, "url", canonical)) {
        return;
      }
      Row row = new Row().set("id", SnowflakeIdUtils.id()).set("url", canonical).set("title", title).set("type", type).set("markdown", markdown);
      Db.save(tableName, row);
    } finally {
      lock.unlock();
    }
  }

  /**
   * 保存页面内容到数据库（使用 Striped 锁防止并发写入同一 URL）
   */
  public void saveContent(String tableName, String url, String title, String type, String html) {

    String canonical = WebsiteUrlUtils.canonicalizeUrl(url);
    if (exists(tableName, "url", canonical)) {
      return;
    }

    Lock lock = stripedLocks.get(canonical);
    lock.lock();
    try {
      if (exists(tableName, "url", canonical)) {
        return;
      }
      Row row = new Row().set("id", SnowflakeIdUtils.id()).set("url", canonical).set("title", title).set("type", type).set("html", html).set("text", Jsoup.parse(html).text());
      Db.save(tableName, row);
    } finally {
      lock.unlock();
    }
  }

  public void saveHtmlAndMarkdown(String tableName, String url, String title, String html, String markdown) {
    String canonical = WebsiteUrlUtils.canonicalizeUrl(url);
    if (exists(tableName, "url", canonical)) {
      return;
    }

    Lock lock = stripedLocks.get(canonical);
    lock.lock();
    try {
      if (exists(tableName, "url", canonical)) {
        return;
      }
      Row row = new Row().set("id", SnowflakeIdUtils.id()).set("url", canonical).set("title", title).set("type", "html")
          //
          .set("html", html).set("markdown", markdown);
      Db.save(tableName, row);
    } finally {
      lock.unlock();
    }
  }

}
