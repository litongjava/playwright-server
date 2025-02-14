package com.litongjava.playwright.model.base;

import com.litongjava.db.activerecord.Model;
import com.litongjava.model.db.IBean;

/**
 * Generated by java-db, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseMaxKbParagraph<M extends BaseMaxKbParagraph<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Long id) {
		set("id", id);
		return (M)this;
	}
	
	public java.lang.Long getId() {
		return getLong("id");
	}
	
	public M setSourceId(java.lang.Long sourceId) {
		set("source_id", sourceId);
		return (M)this;
	}
	
	public java.lang.Long getSourceId() {
		return getLong("source_id");
	}
	
	public M setSourceType(java.lang.String sourceType) {
		set("source_type", sourceType);
		return (M)this;
	}
	
	public java.lang.String getSourceType() {
		return getStr("source_type");
	}
	
	public M setTitle(java.lang.String title) {
		set("title", title);
		return (M)this;
	}
	
	public java.lang.String getTitle() {
		return getStr("title");
	}
	
	public M setContent(java.lang.String content) {
		set("content", content);
		return (M)this;
	}
	
	public java.lang.String getContent() {
		return getStr("content");
	}
	
	public M setMd5(java.lang.String md5) {
		set("md5", md5);
		return (M)this;
	}
	
	public java.lang.String getMd5() {
		return getStr("md5");
	}
	
	public M setStatus(java.lang.String status) {
		set("status", status);
		return (M)this;
	}
	
	public java.lang.String getStatus() {
		return getStr("status");
	}
	
	public M setHitNum(java.lang.Integer hitNum) {
		set("hit_num", hitNum);
		return (M)this;
	}
	
	public java.lang.Integer getHitNum() {
		return getInt("hit_num");
	}
	
	public M setIsActive(java.lang.Boolean isActive) {
		set("is_active", isActive);
		return (M)this;
	}
	
	public java.lang.Boolean getIsActive() {
		return getBoolean("is_active");
	}
	
	public M setDatasetId(java.lang.Long datasetId) {
		set("dataset_id", datasetId);
		return (M)this;
	}
	
	public java.lang.Long getDatasetId() {
		return getLong("dataset_id");
	}
	
	public M setDocumentId(java.lang.Long documentId) {
		set("document_id", documentId);
		return (M)this;
	}
	
	public java.lang.Long getDocumentId() {
		return getLong("document_id");
	}
	
	public M setEmbedding(java.lang.String embedding) {
		set("embedding", embedding);
		return (M)this;
	}
	
	public java.lang.String getEmbedding() {
		return getStr("embedding");
	}
	
	public M setMeta(java.lang.String meta) {
		set("meta", meta);
		return (M)this;
	}
	
	public java.lang.String getMeta() {
		return getStr("meta");
	}
	
	public M setSearchVector(java.lang.String searchVector) {
		set("search_vector", searchVector);
		return (M)this;
	}
	
	public java.lang.String getSearchVector() {
		return getStr("search_vector");
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

