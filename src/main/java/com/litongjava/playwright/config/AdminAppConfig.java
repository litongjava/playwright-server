package com.litongjava.playwright.config;
import com.litongjava.annotation.AConfiguration;
import com.litongjava.annotation.Initialization;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.config.*;
import com.litongjava.tio.boot.admin.handler.SystemFileTencentCosHandler;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.http.server.router.HttpRequestRouter;

@AConfiguration
public class AdminAppConfig {

  @Initialization
  public void config() {
    // 配置数据库相关
    new TioAdminDbConfiguration().config();
    //new TioAdminRedisDbConfiguration().config();
    //new TioAdminMongoDbConfiguration().config();
    //new TioAdminSaTokenConfiguration().config();
    //new TioAdminInterceptorConfiguration().config();
    //new TioAdminHandlerConfiguration().config();

    // 获取 HTTP 请求路由器
    HttpRequestRouter r = TioBootServer.me().getRequestRouter();
    if (r != null) {
      // 获取文件处理器，并添加文件上传和获取 URL 的接口
      SystemFileTencentCosHandler systemUploadHandler = Aop.get(SystemFileTencentCosHandler.class);
      r.add("/api/system/file/upload", systemUploadHandler::upload);
      r.add("/api/system/file/url", systemUploadHandler::getUrl);
    }

    // 配置控制器
    new TioAdminControllerConfiguration().config();
  }
}
