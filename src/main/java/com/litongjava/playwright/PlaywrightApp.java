package com.litongjava.playwright;

import com.litongjava.annotation.AComponentScan;
import com.litongjava.playwright.instance.PlaywrightBrowser;
import com.litongjava.tio.boot.TioApplication;

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
      PlaywrightBrowser.getContent("https://tio-boot.litongjava.com/");
      PlaywrightBrowser.close();
      System.out.println("download end");
    } else {
      long start = System.currentTimeMillis();
      TioApplication.run(PlaywrightApp.class, args);
      long end = System.currentTimeMillis();
      System.out.println((end - start) + "ms");
    }
  }
}
