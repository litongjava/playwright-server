package com.litongjava.playwright.service;

import java.net.URL;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
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

import com.google.common.util.concurrent.Striped;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.ehcache.EhCacheKit;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.web.WebPageContent;
import com.litongjava.playwright.pool.PlaywrightPool;
import com.litongjava.playwright.utils.TaskExecutorUtils;
import com.litongjava.playwright.vo.CrawlTask;
import com.litongjava.tio.utils.hutool.FilenameUtils;
import com.litongjava.tio.utils.hutool.StrUtil;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdvancedCrawlService {

  // 使用 Java 21 虚拟线程的 Executor（每个任务使用一个虚拟线程）
  private final ExecutorService executor = TaskExecutorUtils.executor;
  // 限制队列容量为 10000，达到容量后 put() 方法将阻塞（仅用于消费任务）
  private final BlockingQueue<CrawlTask> taskQueue = new LinkedBlockingQueue<>(10000);
  // 内部缓冲区，用于存放 worker 解析后产生的新任务
  private final Queue<CrawlTask> internalBuffer = new ConcurrentLinkedQueue<>();
  // 使用 Guava 的 Striped 锁，防止并发下重复存库
  private final Striped<Lock> stripedLocks = Striped.lock(64);
  // 活跃任务计数器（记录已提交到 taskQueue 的任务数量）
  private final AtomicInteger activeTasks = new AtomicInteger(0);
  // 已处理页面计数，用于限制总页面数量
  private final AtomicInteger processedPages = new AtomicInteger(0);
  // 基础域名（只爬取同一域内页面）
  private String baseDomain;
  private String tableName;

  // 工作线程数量（可根据需要调整）
  private static final int WORKER_COUNT = 100;
  // 最大爬取深度（防止无限制递归）
  private static final int MAX_DEPTH = 5;
  // 最大重试次数（超过该次数后不再重试此 URL）
  private static final int MAX_RETRIES = 3;
  // 失败计数缓存的名称（EhCache 中存储 URL 对应的失败次数）
  private static final String FAIL_CACHE = "AdvancedCrawlService_fail";

  // 生产者线程池（单线程）
  private final ExecutorService producerExecutor = Executors.newSingleThreadExecutor();

  public AdvancedCrawlService(String tableName) {
    this.tableName = tableName;
  }

  /**
   * 构造时传入初始 URL，内部会对 URL 做规范化、提取域名，并启动爬虫任务、生产者和监控线程
   *
   * @param startUrl 爬虫入口 URL
   */
  public void start(String startUrl) {
    String normalizedStartUrl = normalizeUrl(startUrl);
    this.baseDomain = extractDomain(normalizedStartUrl);
    submitInitialTask(normalizedStartUrl, 0);
    // 启动多个工作线程（虚拟线程），实现并发爬取
    startWorkers();
    // 启动任务生产者线程，将 internalBuffer 中的任务提交到 taskQueue
    startProducer();
    // 启动监控线程
    startMonitor();
  }

  /**
   * 将初始 URL 提交到任务队列
   */
  private void submitInitialTask(String url, int depth) {
    if (shouldProcess(url) && depth <= MAX_DEPTH) {
      try {
        taskQueue.put(new CrawlTask(url, depth));
        activeTasks.incrementAndGet();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
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
   * 启动生产者线程，定时将 internalBuffer 中的任务提交到 taskQueue 中
   */
  private void startProducer() {
    producerExecutor.submit(this::producerTask);
  }

  /**
   * 生产者线程方法：不断检查 internalBuffer，将任务以非阻塞方式提交到 taskQueue
   */
  private void producerTask() {
    while (true) {
      try {
        CrawlTask task = internalBuffer.poll();
        if (task != null) {
          // 使用 offer() 非阻塞提交任务到队列
          boolean submitted = taskQueue.offer(task);
          if (submitted) {
            activeTasks.incrementAndGet();
          } else {
            // 队列满时，将任务重新放回缓冲区，并等待一段时间降低提交速率
            internalBuffer.offer(task);
            TimeUnit.MILLISECONDS.sleep(100);
          }
        } else {
          // 当缓冲区空时稍作等待
          TimeUnit.MILLISECONDS.sleep(50);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (Exception e) {
        log.error("Producer error", e);
      }
    }
  }

  /**
   * Worker线程方法：循环从队列中取任务进行处理  
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
          // 每个任务处理完毕后减少计数
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
   * 在解析页面时将解析出的新链接添加到 internalBuffer，由生产者线程负责提交到 taskQueue
   */
  private void processTask(CrawlTask task) {
    String url = task.getUrl();
    int currentDepth = task.getDepth();
    String canonical = canonicalizeUrl(url);
    // 检查该 URL 是否已累计失败达到上限
    Integer failCount = EhCacheKit.get(FAIL_CACHE, canonical);
    if (failCount != null && failCount >= MAX_RETRIES) {
      log.error("Skipping URL {} as it has already failed {} times", url, failCount);
      return;
    }

    // 针对 PDF 与 HTML 分支分别处理
    if (url.endsWith(".pdf")) {
      try {
        // 处理 PDF 文件
        String content = Aop.get(WebPageService.class).getPdfContent(url);
        String suffix = FilenameUtils.getSuffix(url);
        String filename = FilenameUtils.getBaseName(url);
        if (content != null) {
          content = content.replace("\u0000", "").trim();
          saveContent(url, filename, suffix, content);
        }
      } catch (Exception e) {
        log.error("PDF processing failed for URL {}: {}", url, e.getMessage());
        updateFailureCount(url);
      }
    } else {
      int attempt = 0;
      boolean success = false;
      while (attempt < MAX_RETRIES && !success) {
        try {
          log.info("Processing URL: {} (Attempt {}/{})", url, attempt + 1, MAX_RETRIES);
          WebPageContent webPage = Aop.get(WebPageService.class).getHtml(url);
          String title = webPage.getTitle();
          String html = webPage.getContent();
          saveContent(url, title, "html", html);
          // 如果当前爬取深度未达到最大深度，则解析页面并提取新链接
          if (currentDepth < MAX_DEPTH) {
            Document doc = Jsoup.parse(html, url);
            Set<String> links = extractValidLinks(doc);
            for (String link : links) {
              if (shouldProcess(link)) {
                // 将新任务加入到内部缓冲区，由生产者线程提交到 taskQueue
                internalBuffer.offer(new CrawlTask(link, currentDepth + 1));
              }
            }
          }
          success = true;
        } catch (Exception e) {
          attempt++;
          if (attempt < MAX_RETRIES) {
            log.warn("Attempt {} for URL {} failed with error: {}. Retrying...", attempt, url, e.getMessage());
          } else {
            log.error("All {} attempts failed for URL {}. Error: {}", MAX_RETRIES, url, e.getMessage());
            updateFailureCount(url);
          }
        }
      }
    }
  }

  /**
   * 使用 Jsoup 解析 DOM，提取页面中所有有效链接（剔除锚点、只保留同一域链接）
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
   */
  private void saveContent(String url, String title, String type, String html) {
    String canonical = canonicalizeUrl(url);
    if (exists(tableName, "url", canonical)) {
      return;
    }

    Lock lock = stripedLocks.get(canonical);
    lock.lock();
    try {
      if (exists(tableName, "url", canonical)) {
        return;
      }
      Row row = new Row().set("id", SnowflakeIdUtils.id()).set("url", canonical).set("title", title).set("type", type).set("html", html).set("text", Jsoup.parse(html).text());
      Db.save(tableName, row);
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
    boolean exists = Db.exists(tableName, field, value);
    if (exists) {
      EhCacheKit.put(cacheName, value, exists);
    }
    return exists;
  }

  /**
   * 判断是否需要处理某个 URL
   */
  private boolean shouldProcess(String url) {
    if (url == null || url.isEmpty())
      return false;
    String normalized = normalizeUrl(url);
    if (!isSameDomain(normalized)) {
      return false;
    }
    String canonical = canonicalizeUrl(url);
    Integer failureCount = EhCacheKit.get(FAIL_CACHE, canonical);
    if (failureCount != null && failureCount >= MAX_RETRIES) {
      return false;
    }
    return !exists(tableName, "url", canonical);
  }

  /**
   * 判断 URL 是否与基础域名相同
   */
  private boolean isSameDomain(String url) {
    String domain = extractDomain(url);
    return domain.equalsIgnoreCase(baseDomain);
  }

  /**
   * 规范化 URL：剔除锚点部分，去除尾部斜杠，并做 trim
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
   * 生成 URL 的标准形式，去除协议部分（http, https）
   */
  private String canonicalizeUrl(String url) {
    String normalized = normalizeUrl(url);
    return normalized.replaceFirst("(?i)^(https?://)", "");
  }

  /**
   * 更新指定 URL 的失败计数，失败次数超过 MAX_RETRIES 后，下次将跳过该 URL
   */
  private void updateFailureCount(String url) {
    String canonical = canonicalizeUrl(url);
    Integer count = EhCacheKit.get(FAIL_CACHE, canonical);
    if (count == null) {
      count = 0;
    }
    count++;
    EhCacheKit.put(FAIL_CACHE, canonical, count);
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
