package com.litongjava.playwright.service;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Striped;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.ehcache.EhCacheKit;
import com.litongjava.playwright.consts.TableNames;
import com.litongjava.playwright.pool.PlaywrightPool;
import com.litongjava.playwright.utils.PDFUtils;
import com.litongjava.playwright.utils.TaskExecutorUtils;
import com.litongjava.tio.utils.hutool.FilenameUtils;
import com.litongjava.tio.utils.hutool.StrUtil;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

public class AdvancedCrawlService {
  private static final Logger log = LoggerFactory.getLogger(AdvancedCrawlService.class);

  // 使用 Java 21 虚拟线程的 Executor（每个任务使用一个虚拟线程）
  private final ExecutorService executor = TaskExecutorUtils.executor;
  // 改用任务对象，记录 URL 与当前爬取深度
  private final BlockingQueue<CrawlTask> taskQueue = new LinkedBlockingQueue<>(10000);
  // 使用 Guava 的 Striped 锁，防止并发下重复存库
  private final Striped<Lock> stripedLocks = Striped.lock(64);
  // 活跃任务计数器
  private final AtomicInteger activeTasks = new AtomicInteger(0);
  // 已处理页面计数，用于限制总页面数量
  private final AtomicInteger processedPages = new AtomicInteger(0);
  // 基础域名（只爬取同一域内页面）
  private String baseDomain;

  // 工作线程数量（可根据需要调整）
  private static final int WORKER_COUNT = 100;
  // 最大爬取深度（防止无限制递归）
  private static final int MAX_DEPTH = 3;

  /**
   * 内部任务类，记录 URL 及其爬取深度
   */
  private static class CrawlTask {
    final String url;
    final int depth;

    public CrawlTask(String url, int depth) {
      this.url = url;
      this.depth = depth;
    }
  }

  /**
   * 构造时传入初始 URL，内部会对 URL 做规范化、提取域名，并启动爬虫任务及监控线程
   *
   * @param startUrl 爬虫入口 URL
   */
  public void start(String startUrl) {
    String normalizedStartUrl = normalizeUrl(startUrl);
    this.baseDomain = extractDomain(normalizedStartUrl);
    submitInitialTask(normalizedStartUrl, 0);
    // 启动多个工作线程（虚拟线程），实现并发爬取
    startWorkers();
    // 启动监控线程
    startMonitor();
  }

  /**
   * 将初始 URL 提交到任务队列
   */
  private void submitInitialTask(String url, int depth) {
    if (shouldProcess(url) && depth <= MAX_DEPTH) {
      taskQueue.offer(new CrawlTask(url, depth));
      activeTasks.incrementAndGet();
    }
  }

  /**
   * 启动多个工作线程（虚拟线程），并发处理任务队列中的 URL
   */
  private void startWorkers() {
    for (int i = 0; i < WORKER_COUNT; i++) {
      executor.submit(this::workerTask);
    }
  }

  /**
   * 工作线程方法：循环从队列中取任务进行处理  
   * 当队列为空且活跃任务计数为 0 时退出
   */
  private void workerTask() {
    while (true) {
      CrawlTask task = null;
      try {
        task = taskQueue.poll(1, TimeUnit.SECONDS);
        if (task == null) {
          if (activeTasks.get() <= 0) {
            log.info("所有任务已完成，退出 workerTask。");
            break;
          }
          continue;
        }
        try {
          processTask(task);
        } finally {
          // 确保每个任务结束时都减少计数
          activeTasks.decrementAndGet();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (Exception e) {
        log.error("Worker error", e);
      }
    }
  }

  /**
   * 处理单个爬取任务，包含重试机制  
   * 并在解析页面时根据深度决定是否提交新任务
   */
  private void processTask(CrawlTask task) {
    String url = task.url;
    int currentDepth = task.depth;
    if (url.endsWith(".pdf")) {
      // 处理 PDF 文件
      String content = PDFUtils.getContent(url);
      String suffix = FilenameUtils.getSuffix(url);
      if (content != null) {
        content = content.replace("\u0000", "").trim();
        saveContent(url, suffix, content);
      }
    } else {
      int maxRetries = 3;
      int attempt = 0;
      boolean success = false;
      while (attempt < maxRetries && !success) {
        try (Page page = PlaywrightPool.acquirePage()) {
          // 设置页面超时时间为 1 分钟（60000ms）
          page.setDefaultNavigationTimeout(60000);
          page.setDefaultTimeout(60000);
          // 控制爬取速率
          Thread.sleep(500);
          log.info("Processing URL: {} (Attempt {}/{})", url, attempt + 1, maxRetries);
          // 导航至目标 URL
          page.navigate(url);
          // 等待页面达到网络空闲状态和加载完成状态
          page.waitForLoadState(LoadState.NETWORKIDLE);
          page.waitForLoadState(LoadState.LOAD);
          String html = page.content();
          String title = page.title();
          // 存储页面内容（防止重复存储）
          saveContent(url, title, html);
          // 如果当前爬取深度未达到最大深度，并且总页面数未超出限制，则解析页面并提交新任务
          if (currentDepth < MAX_DEPTH) {
            Document doc = Jsoup.parse(html, url);
            Set<String> links = extractValidLinks(doc);
            for (String link : links) {
              if (shouldProcess(link)) {
                taskQueue.offer(new CrawlTask(link, currentDepth + 1));
                activeTasks.incrementAndGet();
              }
            }
          }
          success = true;
        } catch (Exception e) {
          attempt++;
          if (attempt < maxRetries) {
            log.warn("Attempt {} for URL {} failed with error: {}. Retrying...", attempt, url, e.getMessage());
          } else {
            log.error("All {} attempts failed for URL {}. Error: {}", maxRetries, url, e.getMessage(), e);
            handleError(url, e);
          }
        }
      }
    }
  }

  /**
   * 使用 Jsoup 解析 DOM，提取页面中所有有效链接（剔除锚点、只保留同一域链接）
   *
   * @param doc Jsoup Document 对象
   * @return 符合条件的 URL 集合
   */
  private Set<String> extractValidLinks(Document doc) {
    Set<String> links = new HashSet<>();
    Elements elements = doc.select("a[href]");
    for (Element el : elements) {
      String absUrl = el.absUrl("href");
      String normalized = normalizeUrl(absUrl);
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
   * 保存页面内容到数据库（使用 Striped 锁防止并发写入同一 URL）
   *
   * @param url   当前页面 URL
   * @param title 页面标题或文件后缀
   * @param html  页面 HTML 内容或 PDF 提取的文本
   */
  private void saveContent(String url, String title, String html) {
    // 获取标准化 URL
    String canonical = canonicalizeUrl(url);
    Lock lock = stripedLocks.get(canonical);
    lock.lock();
    try {
      // 如果数据库中已经存在该 canonical URL，则跳过保存
      if (exists(TableNames.web_page_cache, "url", canonical)) {
        return;
      }
      Row row = new Row().set("id", SnowflakeIdUtils.id()).set("url", canonical).set("title", title).set("html", html).set("text", Jsoup.parse(html).text());
      Db.save(TableNames.web_page_cache, row);
      // 计数新保存的页面
      processedPages.incrementAndGet();
    } finally {
      lock.unlock();
    }
  }

  private boolean exists(String cacheTableName, String field, String value) {
    String cacheName = cacheTableName + "_" + field;
    Boolean cacheData = EhCacheKit.get(cacheName, value);
    if (cacheData != null) {
      return cacheData;
    }

    boolean exists = Db.exists(TableNames.web_page_cache, "field", value);
    EhCacheKit.put(cacheName, value, exists);
    return exists;
  }

  /**
   * 判断是否需要处理某个 URL：
   * - URL 非空
   * - URL 属于同一域
   * - 数据库中不存在该 URL（去重）
   *
   * @param url 待处理 URL
   * @return true 表示需要处理；false 表示已处理或不符合条件
   */
  private boolean shouldProcess(String url) {
    if (url == null || url.isEmpty())
      return false;
    String normalized = normalizeUrl(url);
    if (!isSameDomain(normalized))
      return false;
    // 使用标准化 URL 进行去重判断，依赖数据库
    String canonical = canonicalizeUrl(url);
    return !exists(TableNames.web_page_cache, "url", canonical);
  }

  /**
   * 判断 URL 是否与基础域名相同
   *
   * @param url URL 字符串
   * @return true 表示同一域；false 否则
   */
  private boolean isSameDomain(String url) {
    String domain = extractDomain(url);
    return domain.equalsIgnoreCase(baseDomain);
  }

  /**
   * 规范化 URL：剔除锚点部分，去除尾部斜杠，并做 trim
   *
   * @param url 原始 URL
   * @return 规范化后的 URL
   */
  private String normalizeUrl(String url) {
    if (url == null || url.isEmpty())
      return "";
    int index = url.indexOf("#");
    if (index != -1) {
      url = url.substring(0, index);
    }
    url = url.trim();
    if (url.endsWith("/") && url.length() > 1) {
      url = url.substring(0, url.length() - 1);
    }
    return url;
  }

  /**
   * 对 URL 进行预处理，针对路径中的非法字符进行编码。
   *
   * @param url 原始 URL
   * @return 处理后的 URL
   */
  private String encodeUrl(String url) {
    if (url == null || url.isEmpty()) {
      return url;
    }
    return url.replace("[", "%5B").replace("]", "%5D");
  }

  /**
   * 提取 URL 中的域名（不含 www. 前缀）
   *
   * @param url URL 字符串
   * @return 域名字符串
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
   * 生成 URL 的标准形式，去除协议部分（http, https）
   *
   * @param url 原始 URL
   * @return 标准化的 URL 字符串（不含协议部分）
   */
  private String canonicalizeUrl(String url) {
    String normalized = normalizeUrl(url);
    return normalized.replaceFirst("(?i)^(https?://)", "");
  }

  /**
   * 处理爬取过程中的异常，记录错误日志
   *
   * @param url 当前处理的 URL
   * @param e   异常对象
   */
  private void handleError(String url, Exception e) {
    log.error("Error processing URL {}: {}", url, e.getMessage(), e);
  }

  /**
   * 启动监控线程，定时输出队列大小、活跃任务数、已处理页面数量以及 PlaywrightPool 状态  
   * 使用 Java 21 虚拟线程创建单线程调度器
   */
  private void startMonitor() {
    Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory()).scheduleAtFixedRate(() -> {
      log.info("Status - Queue size: {}, Active tasks: {}, Processed pages: {}", taskQueue.size(), activeTasks.get(), processedPages.get());
      log.info("PlaywrightPool - Available: {}/{}", PlaywrightPool.availableCount(), PlaywrightPool.totalCount());
    }, 0, 30, TimeUnit.SECONDS);
  }
}
