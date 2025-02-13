package com.litongjava.playwright.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内部任务类，记录 URL 及其爬取深度
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrawlTask {
  private String url;
  private int depth;
}