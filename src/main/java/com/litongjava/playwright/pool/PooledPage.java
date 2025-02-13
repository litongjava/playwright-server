package com.litongjava.playwright.pool;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Keyboard;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Mouse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.Touchscreen;
import com.microsoft.playwright.Video;
import com.microsoft.playwright.WebSocket;
import com.microsoft.playwright.Worker;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BindingCallback;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.FunctionCallback;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.ViewportSize;

public class PooledPage implements Page {
  private final Page page;
  private final BrowserContext context;
  private final BlockingQueue<BrowserContext> pool;

  public PooledPage(Page page, BrowserContext context, BlockingQueue<BrowserContext> pool) {
    this.page = page;
    this.context = context;
    this.pool = pool;
  }

  @Override
  public void close() {
    page.close();
    pool.offer(context);
  }

  @Override
  public String title() {
    return page.title();
  }

  @Override
  public String content() {
    return page.content();
  }

  @Override
  public Response navigate(String url) {
    return page.navigate(url);
  }

  @Override
  public String innerText(String selector) {
    return page.innerText(selector);
  }

  @Override
  public ElementHandle querySelector(String selector) {
    return page.querySelector(selector);
  }

  @Override
  public Object evaluate(String expression, Object arg) {
    return page.evaluate(expression, arg);
  }

  @Override
  public void onClose(Consumer<Page> handler) {
    page.onClose(handler);
  }

  @Override
  public void offClose(Consumer<Page> handler) {
    page.offClose(handler);
  }

  @Override
  public void onConsoleMessage(Consumer<ConsoleMessage> handler) {
    page.onConsoleMessage(handler);
  }

  @Override
  public void offConsoleMessage(Consumer<ConsoleMessage> handler) {
    page.offConsoleMessage(handler);
  }

  @Override
  public void onCrash(Consumer<Page> handler) {
    page.onCrash(handler);
  }

  @Override
  public void offCrash(Consumer<Page> handler) {
    page.offCrash(handler);
  }

  @Override
  public void onDialog(Consumer<Dialog> handler) {
    page.onDialog(handler);
  }

  @Override
  public void offDialog(Consumer<Dialog> handler) {
    page.offDialog(handler);
  }

  @Override
  public void onDOMContentLoaded(Consumer<Page> handler) {
    page.onDOMContentLoaded(handler);
  }

  @Override
  public void offDOMContentLoaded(Consumer<Page> handler) {
    page.offDOMContentLoaded(handler);
  }

  @Override
  public void onDownload(Consumer<Download> handler) {
    page.onDownload(handler);
  }

  @Override
  public void offDownload(Consumer<Download> handler) {
    page.offDownload(handler);
  }

  @Override
  public void onFileChooser(Consumer<FileChooser> handler) {
    page.onFileChooser(handler);
  }

  @Override
  public void offFileChooser(Consumer<FileChooser> handler) {
    page.offFileChooser(handler);
  }

  @Override
  public void onFrameAttached(Consumer<Frame> handler) {
    page.onFrameAttached(handler);
  }

  @Override
  public void offFrameAttached(Consumer<Frame> handler) {
    page.offFrameAttached(handler);
  }

  @Override
  public void onFrameDetached(Consumer<Frame> handler) {
    page.onFrameDetached(handler);
  }

  @Override
  public void offFrameDetached(Consumer<Frame> handler) {
    page.offFrameDetached(handler);
  }

  @Override
  public void onFrameNavigated(Consumer<Frame> handler) {
    page.onFrameNavigated(handler);
  }

  @Override
  public void offFrameNavigated(Consumer<Frame> handler) {
    page.offFrameNavigated(handler);
  }

  @Override
  public void onLoad(Consumer<Page> handler) {
    page.onLoad(handler);
  }

  @Override
  public void offLoad(Consumer<Page> handler) {
    page.offLoad(handler);
  }

  @Override
  public void onPageError(Consumer<String> handler) {
    page.onPageError(handler);
  }

  @Override
  public void offPageError(Consumer<String> handler) {
    page.offPageError(handler);
  }

  @Override
  public void onPopup(Consumer<Page> handler) {
    page.onPopup(handler);
  }

  @Override
  public void offPopup(Consumer<Page> handler) {
    page.offPopup(handler);
  }

  @Override
  public void onRequest(Consumer<Request> handler) {
    page.onRequest(handler);
  }

  @Override
  public void offRequest(Consumer<Request> handler) {
    page.offRequest(handler);
  }

  @Override
  public void onRequestFailed(Consumer<Request> handler) {
    page.onRequestFailed(handler);
  }

  @Override
  public void offRequestFailed(Consumer<Request> handler) {
    page.offRequestFailed(handler);
  }

  @Override
  public void onRequestFinished(Consumer<Request> handler) {
    page.onRequestFinished(handler);
  }

  @Override
  public void offRequestFinished(Consumer<Request> handler) {
    page.offRequestFinished(handler);
  }

  @Override
  public void onResponse(Consumer<Response> handler) {
    page.onResponse(handler);
  }

  @Override
  public void offResponse(Consumer<Response> handler) {
    page.offResponse(handler);
  }

  @Override
  public void onWebSocket(Consumer<WebSocket> handler) {
    page.onWebSocket(handler);
  }

  @Override
  public void offWebSocket(Consumer<WebSocket> handler) {
    page.offWebSocket(handler);
  }

  @Override
  public void onWorker(Consumer<Worker> handler) {
    page.onWorker(handler);
  }

  @Override
  public void offWorker(Consumer<Worker> handler) {
    page.offWorker(handler);
  }

  @Override
  public void addInitScript(String script) {
    page.addInitScript(script);
  }

  @Override
  public void addInitScript(Path script) {
    page.addInitScript(script);
  }

  @Override
  public ElementHandle addScriptTag(AddScriptTagOptions options) {
    return page.addScriptTag(options);
  }

  @Override
  public ElementHandle addStyleTag(AddStyleTagOptions options) {
    return page.addStyleTag(options);
  }

  @Override
  public void bringToFront() {
    page.bringToFront();
  }

  @Override
  public void check(String selector, CheckOptions options) {
    page.check(selector, options);
  }

  @Override
  public void click(String selector, ClickOptions options) {
    page.click(selector, options);
  }

  @Override
  public void close(CloseOptions options) {
    page.close(options);
    pool.offer(context);
  }

  @Override
  public BrowserContext context() {
    return page.context();
  }

  @Override
  public void dblclick(String selector, DblclickOptions options) {
    page.dblclick(selector, options);
  }

  @Override
  public void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options) {
    page.dispatchEvent(selector, type, eventInit, options);
  }

  @Override
  public void dragAndDrop(String source, String target, DragAndDropOptions options) {
    page.dragAndDrop(source, target, options);
  }

  @Override
  public void emulateMedia(EmulateMediaOptions options) {
    page.emulateMedia(options);
  }

  @Override
  public Object evalOnSelector(String selector, String expression, Object arg, EvalOnSelectorOptions options) {
    return page.evalOnSelector(selector, expression, arg, options);
  }

  @Override
  public Object evalOnSelectorAll(String selector, String expression, Object arg) {
    return page.evalOnSelectorAll(selector, expression, arg);
  }

  @Override
  public JSHandle evaluateHandle(String expression, Object arg) {
    return page.evaluateHandle(expression, arg);
  }

  @Override
  public void exposeBinding(String name, BindingCallback callback, ExposeBindingOptions options) {
    page.exposeBinding(name, callback, options);
  }

  @Override
  public void exposeFunction(String name, FunctionCallback callback) {
    page.exposeFunction(name, callback);
  }

  @Override
  public void fill(String selector, String value, FillOptions options) {
    page.fill(selector, value, options);
  }

  @Override
  public void focus(String selector, FocusOptions options) {
    page.focus(selector, options);
  }

  @Override
  public Frame frame(String name) {
    return page.frame(name);
  }

  @Override
  public Frame frameByUrl(String url) {
    return page.frameByUrl(url);
  }

  @Override
  public Frame frameByUrl(Pattern url) {
    return page.frameByUrl(url);
  }

  @Override
  public Frame frameByUrl(Predicate<String> url) {
    return page.frameByUrl(url);
  }

  @Override
  public FrameLocator frameLocator(String selector) {
    return page.frameLocator(selector);
  }

  @Override
  public List<Frame> frames() {
    return page.frames();
  }

  @Override
  public String getAttribute(String selector, String name, GetAttributeOptions options) {
    return page.getAttribute(selector, name, options);
  }

  @Override
  public Locator getByAltText(String text, GetByAltTextOptions options) {
    return page.getByAltText(text, options);
  }

  @Override
  public Locator getByAltText(Pattern text, GetByAltTextOptions options) {
    return page.getByAltText(text, options);
  }

  @Override
  public Locator getByLabel(String text, GetByLabelOptions options) {
    return page.getByLabel(text, options);
  }

  @Override
  public Locator getByLabel(Pattern text, GetByLabelOptions options) {
    return page.getByLabel(text, options);
  }

  @Override
  public Locator getByPlaceholder(String text, GetByPlaceholderOptions options) {
    return page.getByPlaceholder(text, options);
  }

  @Override
  public Locator getByPlaceholder(Pattern text, GetByPlaceholderOptions options) {
    return page.getByPlaceholder(text, options);
  }

  @Override
  public Locator getByRole(AriaRole role, GetByRoleOptions options) {
    return page.getByRole(role, options);
  }

  @Override
  public Locator getByTestId(String testId) {
    return page.getByTestId(testId);
  }

  @Override
  public Locator getByText(String text, GetByTextOptions options) {
    return page.getByText(text, options);
  }

  @Override
  public Locator getByText(Pattern text, GetByTextOptions options) {
    return page.getByText(text, options);
  }

  @Override
  public Locator getByTitle(String text, GetByTitleOptions options) {
    return page.getByTitle(text, options);
  }

  @Override
  public Locator getByTitle(Pattern text, GetByTitleOptions options) {
    return page.getByTitle(text, options);
  }

  @Override
  public Response goBack(GoBackOptions options) {
    return page.goBack(options);
  }

  @Override
  public Response goForward(GoForwardOptions options) {
    return page.goForward(options);
  }

  @Override
  public Response navigate(String url, NavigateOptions options) {
    return page.navigate(url, options);
  }

  @Override
  public void hover(String selector, HoverOptions options) {
    page.hover(selector, options);
  }

  @Override
  public String innerHTML(String selector, InnerHTMLOptions options) {
    return page.innerHTML(selector, options);
  }

  @Override
  public String innerText(String selector, InnerTextOptions options) {
    return page.innerText(selector, options);
  }

  @Override
  public String inputValue(String selector, InputValueOptions options) {
    return page.inputValue(selector, options);
  }

  @Override
  public boolean isChecked(String selector, IsCheckedOptions options) {
    return page.isChecked(selector, options);
  }

  @Override
  public boolean isClosed() {
    return page.isClosed();
  }

  @Override
  public boolean isDisabled(String selector, IsDisabledOptions options) {
    return page.isDisabled(selector, options);
  }

  @Override
  public boolean isEditable(String selector, IsEditableOptions options) {
    return page.isEditable(selector, options);
  }

  @Override
  public boolean isEnabled(String selector, IsEnabledOptions options) {
    return page.isEnabled(selector, options);
  }

  @Override
  public boolean isHidden(String selector, IsHiddenOptions options) {
    return page.isHidden(selector, options);
  }

  @Override
  public boolean isVisible(String selector, IsVisibleOptions options) {
    return page.isVisible(selector, options);
  }

  @Override
  public Keyboard keyboard() {
    return page.keyboard();
  }

  @Override
  public Locator locator(String selector, LocatorOptions options) {
    return page.locator(selector, options);
  }

  @Override
  public Frame mainFrame() {
    return page.mainFrame();
  }

  @Override
  public Mouse mouse() {
    return page.mouse();
  }

  @Override
  public Page opener() {
    return page.opener();
  }

  @Override
  public void pause() {
    page.pause();
  }

  @Override
  public byte[] pdf(PdfOptions options) {
    return page.pdf(options);
  }

  @Override
  public void press(String selector, String key, PressOptions options) {
    page.press(selector, key, options);
  }

  @Override
  public ElementHandle querySelector(String selector, QuerySelectorOptions options) {
    return page.querySelector(selector, options);
  }

  @Override
  public List<ElementHandle> querySelectorAll(String selector) {
    return page.querySelectorAll(selector);
  }

  @Override
  public Response reload(ReloadOptions options) {
    return page.reload(options);
  }

  @Override
  public APIRequestContext request() {
    return page.request();
  }

  @Override
  public void route(String url, Consumer<Route> handler, RouteOptions options) {
    page.route(url, handler, options);
  }

  @Override
  public void route(Pattern url, Consumer<Route> handler, RouteOptions options) {
    page.route(url, handler, options);
  }

  @Override
  public void route(Predicate<String> url, Consumer<Route> handler, RouteOptions options) {
    page.route(url, handler, options);
  }

  @Override
  public void routeFromHAR(Path har, RouteFromHAROptions options) {
    page.routeFromHAR(har, options);
  }

  @Override
  public byte[] screenshot(ScreenshotOptions options) {
    return page.screenshot(options);
  }

  @Override
  public List<String> selectOption(String selector, String values, SelectOptionOptions options) {
    return page.selectOption(selector, values, options);
  }

  @Override
  public List<String> selectOption(String selector, ElementHandle values, SelectOptionOptions options) {
    return page.selectOption(selector, values, options);
  }

  @Override
  public List<String> selectOption(String selector, String[] values, SelectOptionOptions options) {
    return page.selectOption(selector, values, options);
  }

  @Override
  public List<String> selectOption(String selector, SelectOption values, SelectOptionOptions options) {
    return page.selectOption(selector, values, options);
  }

  @Override
  public List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options) {
    return page.selectOption(selector, values, options);
  }

  @Override
  public List<String> selectOption(String selector, SelectOption[] values, SelectOptionOptions options) {
    return page.selectOption(selector, values, options);
  }

  @Override
  public void setChecked(String selector, boolean checked, SetCheckedOptions options) {
    page.setChecked(selector, checked, options);
  }

  @Override
  public void setContent(String html, SetContentOptions options) {
    page.setContent(html, options);
  }

  @Override
  public void setDefaultNavigationTimeout(double timeout) {
    page.setDefaultNavigationTimeout(timeout);
  }

  @Override
  public void setDefaultTimeout(double timeout) {
    page.setDefaultTimeout(timeout);
  }

  @Override
  public void setExtraHTTPHeaders(Map<String, String> headers) {
    page.setExtraHTTPHeaders(headers);
  }

  @Override
  public void setInputFiles(String selector, Path files, SetInputFilesOptions options) {
    page.setInputFiles(selector, files, options);
  }

  @Override
  public void setInputFiles(String selector, Path[] files, SetInputFilesOptions options) {
    page.setInputFiles(selector, files, options);
  }

  @Override
  public void setInputFiles(String selector, FilePayload files, SetInputFilesOptions options) {
    page.setInputFiles(selector, files, options);
  }

  @Override
  public void setInputFiles(String selector, FilePayload[] files, SetInputFilesOptions options) {
    page.setInputFiles(selector, files, options);
  }

  @Override
  public void setViewportSize(int width, int height) {
    page.setViewportSize(width, height);
  }

  @Override
  public void tap(String selector, TapOptions options) {
    page.tap(selector, options);
  }

  @Override
  public String textContent(String selector, TextContentOptions options) {
    return page.textContent(selector, options);
  }

  @Override
  public Touchscreen touchscreen() {
    return page.touchscreen();
  }

  @Override
  public void type(String selector, String text, TypeOptions options) {
    page.type(selector, text, options);
  }

  @Override
  public void uncheck(String selector, UncheckOptions options) {
    page.uncheck(selector, options);
  }

  @Override
  public void unroute(String url, Consumer<Route> handler) {
    page.unroute(url, handler);
  }

  @Override
  public void unroute(Pattern url, Consumer<Route> handler) {
    page.unroute(url, handler);
  }

  @Override
  public void unroute(Predicate<String> url, Consumer<Route> handler) {
    page.unroute(url, handler);
  }

  @Override
  public String url() {
    return page.url();
  }

  @Override
  public Video video() {
    return page.video();
  }

  @Override
  public ViewportSize viewportSize() {
    return page.viewportSize();
  }

  @Override
  public Page waitForClose(WaitForCloseOptions options, Runnable callback) {
    return page.waitForClose(options, callback);
  }

  @Override
  public ConsoleMessage waitForConsoleMessage(WaitForConsoleMessageOptions options, Runnable callback) {
    return page.waitForConsoleMessage(options, callback);
  }

  @Override
  public Download waitForDownload(WaitForDownloadOptions options, Runnable callback) {
    return page.waitForDownload(options, callback);
  }

  @Override
  public FileChooser waitForFileChooser(WaitForFileChooserOptions options, Runnable callback) {
    return page.waitForFileChooser(options, callback);
  }

  @Override
  public JSHandle waitForFunction(String expression, Object arg, WaitForFunctionOptions options) {
    return page.waitForFunction(expression, arg, options);
  }

  @Override
  public void waitForLoadState(LoadState state, WaitForLoadStateOptions options) {
    page.waitForLoadState(state, options);
  }

  @Override
  public Response waitForNavigation(WaitForNavigationOptions options, Runnable callback) {
    return page.waitForNavigation(options, callback);
  }

  @Override
  public Page waitForPopup(WaitForPopupOptions options, Runnable callback) {
    return page.waitForPopup(options, callback);
  }

  @Override
  public Request waitForRequest(String urlOrPredicate, WaitForRequestOptions options, Runnable callback) {
    return page.waitForRequest(urlOrPredicate, options, callback);
  }

  @Override
  public Request waitForRequest(Pattern urlOrPredicate, WaitForRequestOptions options, Runnable callback) {
    return page.waitForRequest(urlOrPredicate, options, callback);
  }

  @Override
  public Request waitForRequest(Predicate<Request> urlOrPredicate, WaitForRequestOptions options, Runnable callback) {
    return page.waitForRequest(urlOrPredicate, options, callback);
  }

  @Override
  public Request waitForRequestFinished(WaitForRequestFinishedOptions options, Runnable callback) {
    return page.waitForRequestFinished(options, callback);
  }

  @Override
  public Response waitForResponse(String urlOrPredicate, WaitForResponseOptions options, Runnable callback) {
    return page.waitForResponse(urlOrPredicate, options, callback);
  }

  @Override
  public Response waitForResponse(Pattern urlOrPredicate, WaitForResponseOptions options, Runnable callback) {
    return page.waitForResponse(urlOrPredicate, options, callback);
  }

  @Override
  public Response waitForResponse(Predicate<Response> urlOrPredicate, WaitForResponseOptions options, Runnable callback) {
    return page.waitForResponse(urlOrPredicate, options, callback);
  }

  @Override
  public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) {
    return page.waitForSelector(selector, options);
  }

  @Override
  public void waitForTimeout(double timeout) {
    page.waitForTimeout(timeout);
  }

  @Override
  public void waitForURL(String url, WaitForURLOptions options) {
    page.waitForURL(url, options);
  }

  @Override
  public void waitForURL(Pattern url, WaitForURLOptions options) {
    page.waitForURL(url, options);
  }

  @Override
  public void waitForURL(Predicate<String> url, WaitForURLOptions options) {
    page.waitForURL(url, options);
  }

  @Override
  public WebSocket waitForWebSocket(WaitForWebSocketOptions options, Runnable callback) {
    return page.waitForWebSocket(options, callback);
  }

  @Override
  public Worker waitForWorker(WaitForWorkerOptions options, Runnable callback) {
    return page.waitForWorker(options, callback);
  }

  @Override
  public List<Worker> workers() {
    return page.workers();
  }

  @Override
  public void onceDialog(Consumer<Dialog> handler) {
    page.onceDialog(handler);
  }
}