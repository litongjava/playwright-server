package com.litongjava.playwright.instance;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import com.litongjava.tio.utils.environment.EnvUtils;

public class PlaywrightBrowserTest {

  @Test
  public void test() {
    // 加载环境配置
    EnvUtils.load();

    // 获取页面 HTML 内容
    String content = PlaywrightBrowser.getBodyHtml("https://www.kapiolani.hawaii.edu");
    System.out.println("原始内容：");
    System.out.println(content);

    // 使用 Jsoup 解析 HTML 内容，并指定基础 URI
    Document doc = Jsoup.parse(content, "https://www.kapiolani.hawaii.edu");

    // 选择所有 a 标签，并提取 href 属性
    Elements links = doc.select("a[href]");
    System.out.println("\n检测到的子页面链接：");

    // 遍历所有链接，过滤出站内链接（即以该域名开头）
    for (Element link : links) {
      String url = link.absUrl("href");
      if (url.startsWith("https://www.kapiolani.hawaii.edu")) {
        System.out.println(url);
      }
    }
  }
}