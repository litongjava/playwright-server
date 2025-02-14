package com.litongjava.playwright.dao;

import java.util.concurrent.locks.Lock;

import com.google.common.util.concurrent.Striped;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.ehcache.EhCacheKit;
import com.litongjava.playwright.utils.WebsiteUrlUtils;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

public class WebPageDao {
  private final Striped<Lock> stripedLocks = Striped.lock(256);
  
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
  
  public boolean exists(String cacheTableName, String field, String value) {
    String cacheName = cacheTableName + "_" + field;
    Boolean cacheData = EhCacheKit.get(cacheName, value);
    if (cacheData != null) {
      return cacheData;
    }
    boolean exists = Db.exists(cacheTableName, field, value);
    if (exists) {
      EhCacheKit.put(cacheName, value, exists);
    }
    return exists;
  }


}
