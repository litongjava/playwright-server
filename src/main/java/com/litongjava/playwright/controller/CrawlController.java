package com.litongjava.playwright.controller;

import com.litongjava.annotation.Get;
import com.litongjava.annotation.RequestPath;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.playwright.service.AdvancedCrawlService;

import lombok.extern.slf4j.Slf4j;

@RequestPath("/crawl")
@Slf4j
public class CrawlController {

  @Get
  public RespBodyVo index(String url,String table_name) {
    log.info("url:{}", url);
    // AdvancedCrawlService 构造时启动爬虫任务
    new AdvancedCrawlService(table_name).start(url);
    return RespBodyVo.ok();
  }
  
  @Get("hawaii_kapiolani_web_page")
  public RespBodyVo index(String url) {
    log.info("url:{}", url);
    // AdvancedCrawlService 构造时启动爬虫任务
    new AdvancedCrawlService("hawaii_kapiolani_web_page").start(url);
    return RespBodyVo.ok();
  }
}
