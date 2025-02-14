package com.litongjava.playwright.pool;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import lombok.extern.slf4j.Slf4j;

import com.microsoft.playwright.BrowserType.LaunchOptions;

/**
 * PlaywrightPool 用于管理 BrowserContext 对象，减少频繁创建造成的性能开销。
 * 每次 acquirePage() 从池中取出一个 BrowserContext，并创建一个 Page，
 * 当 Page 关闭时自动将 BrowserContext 归还池中。
 */
@Slf4j
public class PlaywrightPool {
  private static BlockingQueue<BrowserContext> pool = null;
  private static BlockingQueue<Playwright> playwrightPool = null;
  private static BlockingQueue<Browser> browserPool = null;
  private static int poolSize = 0;
  public static LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);

  private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());

  /**
   * 构造时初始化 Playwright、Browser 以及固定数量的 BrowserContext
   *
   * @param poolSize 池大小
   */
  public static void init(int poolSize) {
    PlaywrightPool.poolSize = poolSize;

    PlaywrightPool.pool = new ArrayBlockingQueue<>(poolSize);
    PlaywrightPool.playwrightPool = new ArrayBlockingQueue<>(poolSize);
    PlaywrightPool.browserPool = new ArrayBlockingQueue<>(poolSize);
    for (int i = 0; i < poolSize; i++) {
      Playwright playwright = Playwright.create();
      Browser browser = playwright.chromium().launch(launchOptions);
      BrowserContext context = browser.newContext();
      playwrightPool.offer(playwright);
      browserPool.offer(browser);
      pool.offer(context);
    }

    scheduler.scheduleAtFixedRate(() -> {
      new Random().nextInt(1,10);
      log.info("PlaywrightPool - Available: {}/{}", PlaywrightPool.availableCount(), PlaywrightPool.totalCount());
    }, 0, 30, TimeUnit.SECONDS);
  }

  /**
   * 获取一个 Page 对象，内部会从池中取出一个 BrowserContext，
   * 并包装为 PooledPage（实现了 Page 接口、AutoCloseable）。
   *
   * @return PooledPage 对象，使用完毕后调用 close() 归还 BrowserContext
   * @throws InterruptedException 如果等待过程中被中断
   */
  public static Page acquirePage() throws InterruptedException {
    BrowserContext context = pool.take();
    Page page = context.newPage();
    return new PooledPage(page, context, pool);
  }

  /**
   * 返回当前池中可用的 BrowserContext 数量
   *
   * @return 可用数量
   */
  public static int availableCount() {
    return pool.size();
  }

  /**
   * 池的总大小
   *
   * @return 池大小
   */
  public static int totalCount() {
    return poolSize;
  }

  /**
   * 关闭池中所有 BrowserContext 以及 Browser、Playwright 实例
   */
  public static void close() {
    for (BrowserContext context : pool) {
      context.close();
    }

    for (Playwright context : playwrightPool) {
      context.close();
    }

    for (Browser context : browserPool) {
      context.close();
    }
  }

}
