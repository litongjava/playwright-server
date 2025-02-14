package com.litongjava.playwright.service;

import com.litongjava.jfinal.aop.Aop;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdvancedCrawlService {

  /**
   * 构造时传入初始 URL，内部会对 URL 做规范化、提取域名，并启动爬虫任务、生产者和监控线程
   *
   * @param startUrl 爬虫入口 URL
   */
  public void start(String link) {
    CrawlWebPageTask crawlWebPageTask = Aop.get(CrawlWebPageTask.class);
    crawlWebPageTask.addLink(link);
  }
}
