package com.litongjava.playwright.config;

import com.litongjava.jfinal.aop.annotation.AConfiguration;
import com.litongjava.jfinal.aop.annotation.AInitialization;
import com.litongjava.playwright.instance.PlaywrightBrowser;
import com.litongjava.tio.boot.server.TioBootServer;

@AConfiguration
public class PlaywrightConfig {

  @AInitialization
  public void config() {
    // 启动
    PlaywrightBrowser.browser();

    // 服务关闭时，自动关闭浏览器和 Playwright 实例
    TioBootServer.me().addDestroyMethod(() -> {
      PlaywrightBrowser.close();
    });
  }
}
