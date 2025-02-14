package com.litongjava.playwright.model;

import com.litongjava.playwright.model.base.BaseLlmChatSession;

/**
 * Generated by java-db.
 */
public class LlmChatSession extends BaseLlmChatSession<LlmChatSession> {
  private static final long serialVersionUID = 1L;
	public static final LlmChatSession dao = new LlmChatSession().dao();
	/**
	 * 
	 */
  public static final String tableName = "llm_chat_session";
  public static final String primaryKey = "id";
  //java.lang.Long 
  public static final String id = "id";
  //java.lang.String 
  public static final String userId = "user_id";
  //java.lang.String 
  public static final String name = "name";
  //java.lang.Long 
  public static final String schoolId = "school_id";
  //java.lang.Long 
  public static final String appId = "app_id";
  //java.lang.String 
  public static final String type = "type";
  //java.lang.Integer 
  public static final String chatType = "chat_type";
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

