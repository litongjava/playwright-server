package com.litongjava.playwright.model.base;

import com.litongjava.db.activerecord.Model;
import com.litongjava.model.db.IBean;

/**
 * Generated by java-db, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseMaxKbWebPageDescription<M extends BaseMaxKbWebPageDescription<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Long id) {
		set("id", id);
		return (M)this;
	}
	
	public java.lang.Long getId() {
		return getLong("id");
	}
	
	public M setDescription(java.lang.String description) {
		set("description", description);
		return (M)this;
	}
	
	public java.lang.String getDescription() {
		return getStr("description");
	}
	
	public M setContent(java.lang.String content) {
		set("content", content);
		return (M)this;
	}
	
	public java.lang.String getContent() {
		return getStr("content");
	}
	
	public M setDescriptionMd5(java.lang.String descriptionMd5) {
		set("description_md5", descriptionMd5);
		return (M)this;
	}
	
	public java.lang.String getDescriptionMd5() {
		return getStr("description_md5");
	}
	
	public M setContentMd5(java.lang.String contentMd5) {
		set("content_md5", contentMd5);
		return (M)this;
	}
	
	public java.lang.String getContentMd5() {
		return getStr("content_md5");
	}
	
	public M setDescriptiontVector(java.lang.String descriptiontVector) {
		set("descriptiont_vector", descriptiontVector);
		return (M)this;
	}
	
	public java.lang.String getDescriptiontVector() {
		return getStr("descriptiont_vector");
	}
	
	public M setDescriptionVectorId(java.lang.Long descriptionVectorId) {
		set("description_vector_id", descriptionVectorId);
		return (M)this;
	}
	
	public java.lang.Long getDescriptionVectorId() {
		return getLong("description_vector_id");
	}
	
	public M setContentVector(java.lang.String contentVector) {
		set("content_vector", contentVector);
		return (M)this;
	}
	
	public java.lang.String getContentVector() {
		return getStr("content_vector");
	}
	
	public M setContentVectorId(java.lang.Long contentVectorId) {
		set("content_vector_id", contentVectorId);
		return (M)this;
	}
	
	public java.lang.Long getContentVectorId() {
		return getLong("content_vector_id");
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

