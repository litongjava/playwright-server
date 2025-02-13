package com.litongjava.playwright.config;

import com.litongjava.annotation.AConfiguration;
import com.litongjava.annotation.Initialization;
import com.litongjava.hook.HookCan;
import com.litongjava.playwright.pool.PlaywrightPool;
import com.litongjava.tio.utils.environment.EnvUtils;

import lombok.extern.slf4j.Slf4j;

@AConfiguration
@Slf4j
public class PlaywrightConfig {

  @Initialization
  public void config() {
    if (EnvUtils.getBoolean("playwright.enable", true)) {
      // 启动
      log.info("start init playwright");
      if (EnvUtils.isDev()) {
        PlaywrightPool.init(2);
      } else {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        PlaywrightPool.init(cpuCount * 2);
      }
      log.info("end init playwright");

      // 服务关闭时，自动关闭浏览器和 Playwright 实例
      HookCan.me().addDestroyMethod(() -> {
        PlaywrightPool.close();
      });
    }
  }
}
