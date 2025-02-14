package com.litongjava.playwright.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import com.litongjava.playwright.config.PlaywrightConfig;
import com.litongjava.playwright.pool.PlaywrightPool;
import com.litongjava.tio.boot.testing.TioBootTest;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CrawlWebPageTaskTest {

  @Test
  public void test() throws InterruptedException {
    TioBootTest.runWith(PlaywrightConfig.class);
    String url = "https://www.kapiolani.hawaii.edu/department-directory/";
    try (Page page = PlaywrightPool.acquirePage()) {
      // 设置页面超时时间为 1 分钟（60000ms）
      page.setDefaultNavigationTimeout(60000);
      page.setDefaultTimeout(60000);
      // 控制爬取速率
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      // 导航至目标 URL
      page.navigate(url);
      // 等待页面达到网络空闲状态和加载完成状态
      page.waitForLoadState(LoadState.NETWORKIDLE);
      page.waitForLoadState(LoadState.LOAD);
      String html = page.content();
      System.out.println(html);
      // 假设 htmlString 是你的 HTML 字符串

      Document document = Jsoup.parse(html, "https://www.kapiolani.hawaii.edu");
      Elements elements = document.select("a[href]");
      for (Element el : elements) {
        String absUrl = el.absUrl("href");
        System.out.println(absUrl);
      }
    }

  }
}
