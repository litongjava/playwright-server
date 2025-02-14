package com.litongjava.playwright.task;

import org.quartz.JobExecutionContext;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.playwright.service.CrawlWebPageTask;
import com.litongjava.tio.utils.quartz.AbstractJobWithLog;

public class CrawlTask extends AbstractJobWithLog {

  CrawlWebPageTask crawlWebPageTask = Aop.get(CrawlWebPageTask.class);

  @Override
  public void run(JobExecutionContext context) throws Exception {
    crawlWebPageTask.run();
  }
}
