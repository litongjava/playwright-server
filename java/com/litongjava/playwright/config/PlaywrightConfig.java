package com.litongjava.playwright.config;

import com.litongjava.annotation.AConfiguration;
import com.litongjava.annotation.Initialization;
import com.litongjava.hook.HookCan;
import com.litongjava.playwright.instance.PlaywrightBrowser;
import com.litongjava.tio.utils.environment.EnvUtils;

import lombok.extern.slf4j.Slf4j;

@AConfiguration
@Slf4j
public class PlaywrightConfig {

  @Initialization
  public void config() {
    if(EnvUtils.getBoolean("playwright.enable",false)) {
      // 启动
      log.info("start init playwright");
      PlaywrightBrowser.init();
      log.info("end init playwright");

      // 服务关闭时，自动关闭浏览器和 Playwright 实例
      HookCan.me().addDestroyMethod(() -> {
        PlaywrightBrowser.close();
      });
    }
  }
}
