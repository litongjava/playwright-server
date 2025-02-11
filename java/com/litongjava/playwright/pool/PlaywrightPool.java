package com.litongjava.playwright.pool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.BrowserType.LaunchOptions;

/**
 * PlaywrightPool 用于管理 BrowserContext 对象，减少频繁创建造成的性能开销。
 * 每次 acquirePage() 从池中取出一个 BrowserContext，并创建一个 Page，
 * 当 Page 关闭时自动将 BrowserContext 归还池中。
 */
public class PlaywrightPool {
  private final BlockingQueue<BrowserContext> pool;
  private final BlockingQueue<Playwright> playwrightPool;
  private final BlockingQueue<Browser> browserPool;
  private final int poolSize;
  LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(false);

  /**
   * 构造时初始化 Playwright、Browser 以及固定数量的 BrowserContext
   *
   * @param poolSize 池大小
   */
  public PlaywrightPool(int poolSize) {
    this.poolSize = poolSize;

    pool = new ArrayBlockingQueue<>(poolSize);
    playwrightPool = new ArrayBlockingQueue<>(poolSize);
    browserPool = new ArrayBlockingQueue<>(poolSize);
    for (int i = 0; i < poolSize; i++) {
      Playwright playwright = Playwright.create();
      Browser browser = playwright.chromium().launch(launchOptions);
      BrowserContext context = browser.newContext();
      playwrightPool.offer(playwright);
      browserPool.offer(browser);
      pool.offer(context);
    }
  }

  /**
   * 获取一个 Page 对象，内部会从池中取出一个 BrowserContext，
   * 并包装为 PooledPage（实现了 Page 接口、AutoCloseable）。
   *
   * @return PooledPage 对象，使用完毕后调用 close() 归还 BrowserContext
   * @throws InterruptedException 如果等待过程中被中断
   */
  public Page acquirePage() throws InterruptedException {
    BrowserContext context = pool.take();
    Page page = context.newPage();
    return new PooledPage(page, context, pool);
  }

  /**
   * 返回当前池中可用的 BrowserContext 数量
   *
   * @return 可用数量
   */
  public int availableCount() {
    return pool.size();
  }

  /**
   * 池的总大小
   *
   * @return 池大小
   */
  public int totalCount() {
    return poolSize;
  }

  /**
   * 关闭池中所有 BrowserContext 以及 Browser、Playwright 实例
   */
  public void close() {
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
