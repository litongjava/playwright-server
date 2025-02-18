package com.litongjava.playwright.controller;

import com.litongjava.annotation.Get;
import com.litongjava.annotation.RequestPath;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.playwright.consts.TableNames;
import com.litongjava.playwright.service.CrawlWebPageTask;
import com.litongjava.tio.utils.thread.TioThreadUtils;

import lombok.extern.slf4j.Slf4j;

@RequestPath("/crawl")
@Slf4j
public class CrawlController {

  @Get("/hawaii_kapiolani_web_page")
  public RespBodyVo index() {
    TioThreadUtils.execute(() -> {
      String url = "https://www.kapiolani.hawaii.edu/";
      // AdvancedCrawlService 构造时启动爬虫任务
      CrawlWebPageTask crawlWebPageTask = new CrawlWebPageTask(url, TableNames.hawaii_kapioalni_web_page);
      try {
        crawlWebPageTask.run();
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    });
    return RespBodyVo.ok();
  }
}
