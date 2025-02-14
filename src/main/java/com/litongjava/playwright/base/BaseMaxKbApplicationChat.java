package com.litongjava.playwright.base;

import com.litongjava.db.activerecord.Model;
import com.litongjava.model.db.IBean;

/**
 * Generated by java-db, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseMaxKbApplicationChat<M extends BaseMaxKbApplicationChat<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Long id) {
		set("id", id);
		return (M)this;
	}
	
	public java.lang.Long getId() {
		return getLong("id");
	}
	
	public M setAbstract(java.lang.String _abstract) {
		set("abstract", _abstract);
		return (M)this;
	}
	
	public java.lang.String getAbstract() {
		return getStr("abstract");
	}
	
	public M setApplicationId(java.lang.Long applicationId) {
		set("application_id", applicationId);
		return (M)this;
	}
	
	public java.lang.Long getApplicationId() {
		return getLong("application_id");
	}
	
	public M setClientId(java.lang.Long clientId) {
		set("client_id", clientId);
		return (M)this;
	}
	
	public java.lang.Long getClientId() {
		return getLong("client_id");
	}
	
	public M setChatType(java.lang.Integer chatType) {
		set("chat_type", chatType);
		return (M)this;
	}
	
	public java.lang.Integer getChatType() {
		return getInt("chat_type");
	}
	
	public M setIsDeleted(java.lang.Boolean isDeleted) {
		set("is_deleted", isDeleted);
		return (M)this;
	}
	
	public java.lang.Boolean getIsDeleted() {
		return getBoolean("is_deleted");
	}
	
	public M setCreator(java.lang.String creator) {
		set("creator", creator);
		return (M)this;
	}
	
	public java.lang.String getCreator() {
		return getStr("creator");
	}
	
	public M setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
		return (M)this;
	}
	
	public java.util.Date getCreateTime() {
		return getDate("create_time");
	}
	
	public M setUpdater(java.lang.String updater) {
		set("updater", updater);
		return (M)this;
	}
	
	public java.lang.String getUpdater() {
		return getStr("updater");
	}
	
	public M setUpdateTime(java.util.Date updateTime) {
		set("update_time", updateTime);
		return (M)this;
	}
	
	public java.util.Date getUpdateTime() {
		return getDate("update_time");
	}
	
	public M setDeleted(java.lang.Integer deleted) {
		set("deleted", deleted);
		return (M)this;
	}
	
	public java.lang.Integer getDeleted() {
		return getInt("deleted");
	}
	
	public M setTenantId(java.lang.Long tenantId) {
		set("tenant_id", tenantId);
		return (M)this;
	}
	
	public java.lang.Long getTenantId() {
		return getLong("tenant_id");
	}
	
}

