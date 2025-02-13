package com.litongjava.playwright.example;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SiteCrawler {
  private static final String DOMAIN = "";
  private static final String START_URL = "";

  public static void main(String[] args) {
    try (Playwright playwright = Playwright.create()) {
      BrowserType browserType = playwright.chromium();
      // 如果要无头模式，可设成true
      Browser browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(false));
      Page page = browser.newPage();

      // 待访问队列
      Queue<String> toVisit = new LinkedList<>();
      // 已访问链接集合
      Set<String> visited = new HashSet<>();

      // 初始化
      toVisit.offer(START_URL);
      visited.add(START_URL);

      while (!toVisit.isEmpty()) {
        String currentUrl = toVisit.poll();
        System.out.println("正在访问: " + currentUrl);

        // 打开页面
        try {
          page.navigate(currentUrl);
        } catch (Exception e) {
          log.error("url:" + currentUrl, e);
          continue;
        }

        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("debug.png")));
        page.waitForTimeout(3000); // 等3秒

        // 1. 设置监听器，捕捉页面上的所有请求完成事件
        page.onResponse(response -> {
          // 如果请求成功（状态码 200-299）并且是需要的静态资源
          if (response.ok()) {
            // resourceType() 返回例如 "document", "script", "stylesheet", "image", "media", "font", "xhr", "fetch", ...
            String resourceType = response.request().resourceType();
            if (StaticResourceDownloader.isStaticResource(resourceType)) {
              // 下载文件内容（byte[]）
              String remoteUrl = response.url();
              try {
                byte[] body = response.body();
                // 将其写到本地
                String localPath = StaticResourceDownloader.saveBytes(body, remoteUrl);
                System.out.println("已保存静态资源: " + remoteUrl + " -> " + localPath);
              } catch (Exception e) {
                log.error("url:", remoteUrl, e);
              }

            }
          }
        });

        // 拿到当前页面内容
        String htmlContent = page.content();
        // 保存到本地（修正版）
        savePageContent(currentUrl, htmlContent);

        // 从当前页面提取所有链接
        List<ElementHandle> anchorElements = page.querySelectorAll("a");
        List<String> newLinks = new ArrayList<>();

        for (ElementHandle anchor : anchorElements) {
          String href = anchor.getAttribute("href");
          if (href != null && !href.trim().isEmpty()) {
            newLinks.add(href.trim());
          }
        }

        // 处理链接
        for (String link : newLinks) {
          if (isInternalLink(link)) {
            String absoluteUrl = toAbsoluteUrl(currentUrl, link);
            if (!visited.contains(absoluteUrl)) {
              visited.add(absoluteUrl);
              toVisit.offer(absoluteUrl);
            }
          }
        }
      }

      // 最后关浏览器
      browser.close();
    }
  }

  private static String toAbsoluteUrl(String baseUrl, String maybeRelative) {
    try {
      // 如果是绝对URL，new URL(...) 就直接返回
      URL url = new URL(new URL(baseUrl), maybeRelative);
      return url.toString();
    } catch (MalformedURLException e) {
      return maybeRelative; // 实在解析不了，就原样返回
    }
  }

  /**
   * 判断是否同域（以 blog.collegebot.ai 为例）
   */
  private static boolean isInternalLink(String link) {
    if (link == null || link.isEmpty()) {
      return false;
    }

    // 1) 如果以 "/" 开头：认为是站内链接（相对地址）
    if (link.startsWith("/")) {
      return true;
    }

    // 2) 如果是绝对 URL，就尝试解析
    try {
      URL parsed = new URL(link);
      // 如果 host 恰好就是 "blog.collegebot.ai"，则认为是内部链接
      return DOMAIN.equals(parsed.getHost());
    } catch (MalformedURLException e) {
      // 如果解析不成功，比如 mailto:xxx, javascript:void(0) 等，认为不是内部链接
      return false;
    }
  }

  /**
   * 修正后的保存方法：
   * 1. 使用 URL 解析
   * 2. 自动在目录末尾添加 index.html（若 path 为空或 / 结尾）
   * 3. 处理非法字符
   * 4. 按 host/path 的结构在本地生成目录层级
   */
  private static void savePageContent(String url, String html) {
    try {
      // 解析 URL
      URL parsedUrl = new URL(url);

      // 主机名 (如 blog.collegebot.ai)
      String host = parsedUrl.getHost();
      // 路径 (如 /about/)，若为空或根目录则后面会做处理
      String path = parsedUrl.getPath();
      // 查询参数 (如 ?key=value)，可视需要拼接在文件名里
      String query = parsedUrl.getQuery();

      // 若 path 为空或是纯 "/", 则用 /index.html 替代
      if (path == null || path.isEmpty() || path.equals("/")) {
        path = "/index.html";
      }
      // 若是以 / 结尾，则也认为需要补上 index.html
      else if (path.endsWith("/")) {
        path += "index.html";
      }

      // 如果存在查询参数，可以加在文件名末尾，如 path + "__query"
      // 避免带 "?" 直接进文件名
      if (query != null && !query.isEmpty()) {
        // 简单做个拼接，也可以做更复杂处理
        // 比如 path="/post.html" + "__key=value"
        path += "__" + query;
      }

      // 去掉最前面的 "/"，避免在本地目录里变成绝对路径
      if (path.startsWith("/")) {
        path = path.substring(1);
      }

      // 替换文件系统不允许的字符
      // Windows 上如 : ? * 等都是非法字符，跨平台也最好做下统一处理
      path = path.replaceAll("[\\\\/:*?\"<>|]", "_");

      // 组装输出文件的完整路径：cloned_site/host/path
      Path outputPath = Paths.get("cloned_site", host, path);

      // 如果所在目录不存在，就先创建
      Files.createDirectories(outputPath.getParent());

      // 写入文件
      Files.write(outputPath, html.getBytes(StandardCharsets.UTF_8));

      System.out.println("页面已保存: " + outputPath);
    } catch (MalformedURLException e) {
      System.err.println("URL 解析失败: " + url);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
