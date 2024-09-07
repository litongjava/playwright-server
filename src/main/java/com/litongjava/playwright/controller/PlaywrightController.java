package com.litongjava.playwright.controller;

import com.litongjava.playwright.instance.PlaywrightBrowser;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.annotation.RequestPath;
import com.litongjava.tio.http.server.util.Resps;

import lombok.extern.slf4j.Slf4j;

@RequestPath("/playwright")
@Slf4j
public class PlaywrightController {

  @RequestPath()
  public HttpResponse index(String url) {
    log.info("访问的 URL: {}", url);

    String content = PlaywrightBrowser.getContent(url);

    // 返回网页内容
    return Resps.html(TioRequestContext.getResponse(), content);
  }


}