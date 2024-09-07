package com.litongjava.playwright.instance;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Playwright;

public enum PlaywrightBrowser {
  INSTANCE;

  // 创建 Playwright 实例
  public static Playwright playwright = Playwright.create();
  public static Browser browser;
  static {
    // 启动 Chromium 浏览器
    LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true); // 使用无头模式
    browser = playwright.chromium().launch(launchOptions);
  }

  public static Browser browser() {
    return browser;
  }

  public static void close() {
    browser.close();
    playwright.close();
  }

  public static String getContent(String url) {
    // 创建新浏览器上下文
    BrowserContext context = browser.newContext();
    Page page = context.newPage();

    // 导航到目标网页
    page.navigate(url);
    // 获取网页内容
    String content = page.content();
    // 关闭页面
    page.close();

    return content;
  }
}