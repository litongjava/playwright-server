package com.litongjava.playwright.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TaskExecutorUtils {
  public static int cpuCount = Runtime.getRuntime().availableProcessors();
  private static AtomicLong threadCounter = new AtomicLong(0);

  private static int queueCapacity = 100;

  public static ExecutorService executor;

  static {
    executor = new ThreadPoolExecutor(cpuCount, // corePoolSize
        cpuCount, // maximumPoolSize 
        0L, // keepAliveTime
        TimeUnit.MILLISECONDS, // time unit
        new ArrayBlockingQueue<>(queueCapacity), //
        runnable -> {
          Thread t = Thread.ofVirtual().factory().newThread(runnable);
          t.setName("crawl-thread-" + threadCounter.getAndIncrement());
          return t;
        },

        new RejectedExecutionHandler() {
          @Override
          public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
              executor.getQueue().put(r);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              throw new RejectedExecutionException("Task submission interrupted", e);
            }
          }
        });
  }
}
