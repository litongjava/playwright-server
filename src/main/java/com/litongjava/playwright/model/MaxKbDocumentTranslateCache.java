package com.litongjava.playwright.model;

import com.litongjava.playwright.model.base.BaseMaxKbDocumentTranslateCache;

/**
 * Generated by java-db.
 */
public class MaxKbDocumentTranslateCache extends BaseMaxKbDocumentTranslateCache<MaxKbDocumentTranslateCache> {
  private static final long serialVersionUID = 1L;
	public static final MaxKbDocumentTranslateCache dao = new MaxKbDocumentTranslateCache().dao();
	/**
	 * 
	 */
  public static final String tableName = "max_kb_document_translate_cache";
  public static final String primaryKey = "id";
  //java.lang.String 
  public static final String id = "id";
  //java.lang.String 
  public static final String target = "target";
  //java.lang.String 
  public static final String content = "content";

  @Override
  protected String _getPrimaryKey() {
    return primaryKey;
  }

  @Override
  protected String _getTableName() {
    return tableName;
  }
}

