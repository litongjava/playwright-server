package com.litongjava.playwright.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.litongjava.tio.utils.http.HttpDownloadUtils;

public class PDFUtils {
  public static String getContent(String url) {
    // 假设 url 已经定义，HttpDownloadUtils.download 下载 PDF 文件
    ByteArrayOutputStream download = HttpDownloadUtils.download(url, null);
    // 将 ByteArrayOutputStream 转换为 ByteArrayInputStream 供 PDFBox 读取
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(download.toByteArray()); PDDocument document = PDDocument.load(inputStream)) {

      // 使用 PDFTextStripper 提取 PDF 中的文本内容
      PDFTextStripper pdfStripper = new PDFTextStripper();
      String text = pdfStripper.getText(document);
      return text;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
