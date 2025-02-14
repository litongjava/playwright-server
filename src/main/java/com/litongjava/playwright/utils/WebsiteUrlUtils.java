package com.litongjava.playwright.utils;

public class WebsiteUrlUtils {

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
