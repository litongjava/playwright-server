package com.litongjava.playwright.model.base;

import com.litongjava.db.activerecord.Model;
import com.litongjava.model.db.IBean;

/**
 * Generated by java-db, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseMaxKbDocumentMarkdownCache<M extends BaseMaxKbDocumentMarkdownCache<M>> extends Model<M> implements IBean {

	public M setId(java.lang.String id) {
		set("id", id);
		return (M)this;
	}
	
	public java.lang.String getId() {
		return getStr("id");
	}
	
	public M setTarget(java.lang.String target) {
		set("target", target);
		return (M)this;
	}
	
	public java.lang.String getTarget() {
		return getStr("target");
	}
	
	public M setContent(java.lang.String content) {
		set("content", content);
		return (M)this;
	}
	
	public java.lang.String getContent() {
		return getStr("content");
	}
	
}

