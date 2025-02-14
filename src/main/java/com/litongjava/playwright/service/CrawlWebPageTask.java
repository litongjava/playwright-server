package com.litongjava.playwright.service;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.litongjava.db.activerecord.Db;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.web.WebPageContent;
import com.litongjava.playwright.consts.TableNames;
import com.litongjava.playwright.dao.WebPageDao;
import com.litongjava.playwright.model.WebPageUrl;
import com.litongjava.playwright.utils.MarkdownUtils;
import com.litongjava.playwright.utils.TaskExecutorUtils;
import com.litongjava.playwright.utils.WebsiteUrlUtils;
import com.litongjava.tio.utils.hutool.FilenameUtils;
import com.litongjava.tio.utils.hutool.StrUtil;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CrawlWebPageTask {

  WebPageCrawlService crawlWebPageService = Aop.get(WebPageCrawlService.class);
  WebPageDao webPageDao = Aop.get(WebPageDao.class);
  String tableName = TableNames.hawaii_kapioalni_web_page;
  String baseDomain = "kapiolani.hawaii.edu";
  String baseUrl = "https://www.kapiolani.hawaii.edu";

  public void run() {

    while (true) {
      /**
       * status 0 add 1 starting 2.finish
       */
      String sql = "select id,url,status,tried from web_page_url where status=0 and tried < 3 limit 10";
      List<WebPageUrl> list = WebPageUrl.dao.find(sql);
      if (list.size() > 0) {
        for (WebPageUrl webPageUrl : list) {
          this.updateUrlStatusToRunning(webPageUrl);
          TaskExecutorUtils.executor.submit(() -> {
            try {
              this.processUrl(webPageUrl);
            } catch (Exception e) {
              log.error(e.getMessage(), e);
            }
          });
        }
      }
      try {
        Thread.sleep(1000L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void processUrl(WebPageUrl webPageUrl) {
    String url = "https://" + webPageUrl.getUrl();
    // 针对 PDF 与 HTML 分支分别处理
    if (url.endsWith(".pdf")) {
      try {
        // 处理 PDF 文件
        String content = Aop.get(WebPageCrawlService.class).getPdfContent(url);
        String filename = FilenameUtils.getBaseName(url);
        if (content != null) {
          content = content.replace("\u0000", "").trim();
          try {
            webPageDao.saveMarkdown(tableName, url, filename, "pdf", content);
            updateUrlStatusToFinished(webPageUrl);
          } catch (Exception e) {
            log.error("Failed to save:{},{}", url, filename, e);
          }

        }
      } catch (Exception e) {
        log.error("PDF processing failed for URL {}: {}", url, e.getMessage());
        updateFailureCount(webPageUrl);
      }
    } else {
      try {
        log.info("Processing URL: {} (Attempt {})", url, webPageUrl.getTried());
        WebPageContent webPage = Aop.get(WebPageCrawlService.class).getHtml(url);
        String title = webPage.getTitle();
        String html = webPage.getContent();
        // 假设 htmlString 是你的 HTML 字符串
        Document document = Jsoup.parse(html, baseUrl);
        Element body = document.body();
        String bodyHtml = body.html();
        String markdown = MarkdownUtils.toMd(bodyHtml);
        try {
          webPageDao.saveHtmlAndMarkdown(tableName, url, title, html, markdown);
          updateUrlStatusToFinished(webPageUrl);
        } catch (Exception e) {
          log.error("Failed to save:{},{}", url, title, e);
        }
        Set<String> links = extractValidLinks(document);
        for (String link : links) {
          addLink(link);
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        updateFailureCount(webPageUrl);
      }
    }
  }

  public void addLink(String link) {
    String canonical = WebsiteUrlUtils.canonicalizeUrl(link);
    boolean urlExists = webPageDao.exists("web_page_url", "url", canonical);
    if (!urlExists) {
      new WebPageUrl().setId(SnowflakeIdUtils.id()).setUrl(canonical).save();
    }
  }

  /**
   * 使用 Jsoup 解析 DOM，提取页面中所有有效链接（剔除锚点、只保留同一域链接）
   */
  public Set<String> extractValidLinks(Document doc) {
    Set<String> links = new HashSet<>();
    Elements elements = doc.select("a[href]");
    for (Element el : elements) {
      String absUrl = el.absUrl("href");
      String normalized = WebsiteUrlUtils.normalizeUrl(absUrl);
      if (StrUtil.isBlank(normalized)) {
        continue;
      }
      if (isSameDomain(normalized)) {
        links.add(normalized);
      }
    }
    return links;
  }

  /**
   * 提取 URL 中的域名（不含 www. 前缀）
   */
  private String extractDomain(String url) {
    try {
      @SuppressWarnings("deprecation")
      URL netUrl = new URL(url);
      String host = netUrl.getHost();
      if (host != null) {
        return host.startsWith("www.") ? host.substring(4) : host;
      }
    } catch (Exception e) {
      log.error("Error extracting domain from url: {}", url, e);
    }
    return "";
  }

  /**
   * 判断 URL 是否与基础域名相同
   */
  private boolean isSameDomain(String url) {
    String domain = extractDomain(url);
    return domain.equalsIgnoreCase(baseDomain);
  }

  private void updateUrlStatusToRunning(WebPageUrl webPageUrl) {
    Db.updateBySql("update web_page_url set tried=1,status=1 where id=?", webPageUrl.getId());
  }

  private void updateUrlStatusToFinished(WebPageUrl webPageUrl) {
    Db.updateBySql("update web_page_url set tried=1,status=2 where id=?", webPageUrl.getId());
  }

  private void updateFailureCount(WebPageUrl webPageUrl) {
    Integer tried = webPageUrl.getTried();
    tried++;
    Db.updateBySql("update web_page_url set status =0,tried=? where id=?", tried, webPageUrl.getId());
  }

}
