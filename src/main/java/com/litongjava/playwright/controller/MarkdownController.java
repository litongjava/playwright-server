package com.litongjava.playwright.controller;

import com.litongjava.annotation.RequestPath;
import com.litongjava.playwright.instance.PlaywrightBrowser;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.util.Resps;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;

import lombok.extern.slf4j.Slf4j;

@RequestPath("/markdown")
@Slf4j
public class MarkdownController {
  @RequestPath()
  public HttpResponse markdown(String url) {
    log.info("访问的 URL: {}", url);

    String html = PlaywrightBrowser.getContent(url);

    // 创建转换器实例
    FlexmarkHtmlConverter converter = FlexmarkHtmlConverter.builder().build();

    // 将 HTML 转换为 Markdown
    String markdown = converter.convert(html);

    // 返回网页内容
    return Resps.html(TioRequestContext.getResponse(), markdown);
  }
}
