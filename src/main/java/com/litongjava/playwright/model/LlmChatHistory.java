package com.litongjava.playwright.model;

import com.litongjava.playwright.model.base.BaseLlmChatHistory;

/**
 * Generated by java-db.
 */
public class LlmChatHistory extends BaseLlmChatHistory<LlmChatHistory> {
  private static final long serialVersionUID = 1L;
	public static final LlmChatHistory dao = new LlmChatHistory().dao();
	/**
	 * 
	 */
  public static final String tableName = "llm_chat_history";
  public static final String primaryKey = "id";
  //java.lang.Long 
  public static final String id = "id";
  //java.lang.Long 
  public static final String sessionId = "session_id";
  //java.lang.String 
  public static final String role = "role";
  //java.lang.String 
  public static final String content = "content";
  //java.lang.String 
  public static final String citions = "citions";
  //java.lang.String 
  public static final String type = "type";
  //java.lang.String 
  public static final String metadata = "metadata";
  //java.lang.Boolean 
  public static final String hidden = "hidden";
  //java.lang.Boolean 
  public static final String liked = "liked";
  //java.lang.String 
  public static final String remark = "remark";
  //java.lang.String 
  public static final String creator = "creator";
  //java.util.Date 
  public static final String createTime = "create_time";
  //java.lang.String 
  public static final String updater = "updater";
  //java.util.Date 
  public static final String updateTime = "update_time";
  //java.lang.Integer 
  public static final String deleted = "deleted";
  //java.lang.Long 
  public static final String tenantId = "tenant_id";

  @Override
  protected String _getPrimaryKey() {
    return primaryKey;
  }

  @Override
  protected String _getTableName() {
    return tableName;
  }
}

