package com.litongjava.playwright.example;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class StaticResourceDownloader {

  /**
   * 判断是否为我们关心的“静态资源类型”
   */
  public static boolean isStaticResource(String resourceType) {
    // 常见类型: "script", "stylesheet", "image", "media", "font", ...
    // 你可以根据需要扩充或精简
    return true;
//    return "script".equals(resourceType) || "stylesheet".equals(resourceType) || "image".equals(resourceType) ||
//        "media".equals(resourceType) || "font".equals(resourceType);
  }

  /**
   * 将请求返回的字节数组保存在本地文件，并返回一个相对于某个根目录的本地路径
   */
  public static String saveBytes(byte[] data, String remoteUrl) {
    // 你可以定义一个专门存放静态资源的目录
    String baseDir = "cloned_site";

    // 根据远程URL解析出一个合理的路径
    // 例如把 host + path 组成本地文件路径
    try {
      URL url = new URL(remoteUrl);
      String host = url.getHost();
      String path = url.getPath(); // 例: /styles/main.css

      // 如果 path 为空或只有 "/"，可改成默认值
      if (path == null || path.isEmpty() || "/".equals(path)) {
        path = "/index.dat";
      }

      // 将 query (若有) 也拼进文件名，以免同一路径多份
      String query = url.getQuery();
      if (query != null && !query.isEmpty()) {
        // 做一个安全替换，把 "?" ":" 等不合法字符去掉
        path += "__" + query.replaceAll("[\\\\/:*?\"<>|]", "_");
      }

      // 去除 path 最前面的 "/"
      if (path.startsWith("/")) {
        path = path.substring(1);
      }

      // 替换 Windows 不允许的字符(跨平台保险起见也可统一做)
      path = path.replaceAll("[\\\\:*?\"<>|]", "_");

      // 最终输出路径: baseDir/host/path
      Path outputPath = Paths.get(baseDir, host, path);

      // 创建上级目录
      Files.createDirectories(outputPath.getParent());

      // 写入文件
      Files.write(outputPath, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

      // 返回给调用方：可返回绝对路径，也可返回相对路径
      return outputPath.toString();

    } catch (MalformedURLException e) {
      // 如果不是合法的URL，就不去下载
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
