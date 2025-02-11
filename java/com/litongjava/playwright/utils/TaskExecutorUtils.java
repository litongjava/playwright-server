package com.litongjava.playwright.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class TaskExecutorUtils {
  static AtomicLong threadCounter = new AtomicLong(0);
  public static ExecutorService executor = Executors.newThreadPerTaskExecutor(runnable -> {
    // 先用默认的虚拟线程工厂创建线程
    Thread t = Thread.ofVirtual().factory().newThread(runnable);
    // 使用计数器生成自定义的线程名称
    t.setName("crawl-thread-" + threadCounter.getAndIncrement());
    return t;
  });
}
