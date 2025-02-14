package com.litongjava.playwright.utils;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;

public class MarkdownUtils {
  public static FlexmarkHtmlConverter converter = FlexmarkHtmlConverter.builder().build();

  public static String toMd(String html) {
    return converter.convert(html);
  }
}
