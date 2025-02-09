package com.litongjava.playwright;

import com.litongjava.annotation.AComponentScan;
import com.litongjava.tio.boot.TioApplication;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Playwright;

@AComponentScan
public class PlaywrightApp {
  public static void main(String[] args) {
    boolean download = false;
    for (String string : args) {
      if ("--download".equals(string)) {
        download = true;
        break;
      }
    }
    if (download) {
      System.out.println("download start");
      LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true); // 使用无头模式
      try (Playwright playwright = Playwright.create(); Browser launch = playwright.chromium().launch(launchOptions)) {

      }
      System.out.println("download end");
    } else {
      long start = System.currentTimeMillis();
      TioApplication.run(PlaywrightApp.class, args);
      long end = System.currentTimeMillis();
      System.out.println((end - start) + "ms");
    }
  }
}
