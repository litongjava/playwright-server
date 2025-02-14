package com.litongjava.playwright.controller;

import com.litongjava.annotation.Get;
import com.litongjava.annotation.RequestPath;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.playwright.service.AdvancedCrawlService;

import lombok.extern.slf4j.Slf4j;

@RequestPath("/crawl")
@Slf4j
public class CrawlController {

  @Get("/hawaii_kapiolani_web_page")
  public RespBodyVo index() {
    String url="https://www.kapiolani.hawaii.edu/";
    // AdvancedCrawlService 构造时启动爬虫任务
    var advancedCrawlService = Aop.get(AdvancedCrawlService.class);
    advancedCrawlService.start(url);
    return RespBodyVo.ok();
  }
}
