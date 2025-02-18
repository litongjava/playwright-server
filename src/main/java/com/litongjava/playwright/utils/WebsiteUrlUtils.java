package com.litongjava.playwright.utils;

import java.net.URL;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebsiteUrlUtils {

  /**
   * 提取 URL 中的域名（不含 www. 前缀）
   */
  public static String extractDomain(String url) {
    try {
      @SuppressWarnings("deprecation")
      URL netUrl = new URL(url);
      String host = netUrl.getHost();
      if (host != null) {
        return host.startsWith("www.") ? host.substring(4) : host;
      }
    } catch (Exception e) {
      log.error("Error extracting domain from url: {}", url, e);
    }
    return "";
  }
  
  /**
   * 生成 URL 的标准形式，去除协议部分（http, https）
   */
  public static String canonicalizeUrl(String url) {
    String normalized = normalizeUrl(url);
    return normalized.replaceFirst("(?i)^(https?://)", "");
  }
  
  /**
   * 规范化 URL：剔除锚点部分，去除尾部斜杠，并做 trim
   */
  public static String normalizeUrl(String url) {
    if (url == null || url.isEmpty())
      return "";
    int index = url.indexOf("#");
    if (index != -1) {
      url = url.substring(0, index);
    }
    url = url.trim();
    if (url.endsWith("/") && url.length() > 1) {
      url = url.substring(0, url.length() - 1);
    }
    return url;
  }
}
