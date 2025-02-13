package com.litongjava.playwright.utils;

import org.junit.Test;

public class PDFUtilsTest {

  @Test
  public void test() {
    String content = PDFUtils.getContent("http://www.hawaii.edu/offices/bor/guide/docs/Board_of_Regents_General_Overview_with_Appendix.pdf");
    System.out.println(content.trim());
  }

}
