package com.litongjava.playwright.example;

import java.nio.file.Paths;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class RemotePlaywrightExample {
  public static void main(String[] args) {
    // 创建 Playwright 实例
    try (Playwright playwright = Playwright.create()) {
      // 连接到远程浏览器实例
      BrowserType browserType = playwright.chromium();
      Browser browser = browserType.connect("ws://localhost:9222");

      // 创建新的浏览器上下文和页面
      BrowserContext context = browser.newContext();
      Page page = context.newPage();

      // 导航到网页
      page.navigate("https://example.com");

      // 截取页面截图
      page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("remote_example.png")));

      // 关闭浏览器
      browser.close();
    }
  }
}