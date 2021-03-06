package iwb.domain.db;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

// Generated Feb 4, 2007 2:55:52 PM by Hibernate Tools 3.2.0.b9

/**
 * WQueryField generated by hbm2java
 */
@Entity
@Immutable
@Table(name="w5_query_field",schema="iwb")
public class W5QueryField implements java.io.Serializable, W5Param {

	private int queryFieldId;
		
	private int queryId;

	private String dsc;

	private short fieldTip;

	private short tabOrder;

	private short postProcessTip;
	private int lookupQueryId;
	
	private int mainTableFieldId;
	
	public W5QueryField() {
	}

	@Id
	@Column(name="query_field_id")
	public int getQueryFieldId() {
		return queryFieldId;
	}


	@Column(name="query_id")
	public int getQueryId() {
		return queryId;
	}

	@Column(name="dsc")
	public String getDsc() {
		return dsc;
	}

	@Column(name="field_tip")
	public short getFieldTip() {
		return fieldTip;
	}

	@Column(name="tab_order")
	public short getTabOrder() {
		return tabOrder;
	}



	public void setQueryFieldId(int queryFieldId) {
		this.queryFieldId = queryFieldId;
	}


	public void setQueryId(int queryId) {
		this.queryId = queryId;
	}

	public void setDsc(String dsc) {
		this.dsc = dsc;
	}

	public void setFieldTip(short fieldTip) {
		this.fieldTip = fieldTip;
	}

	public void setTabOrder(short tabOrder) {
		this.tabOrder = tabOrder;
	}


	@Column(name="post_process_tip")
	public short getPostProcessTip() {
		return postProcessTip;
	}

	public void setPostProcessTip(short postProcessTip) {
		this.postProcessTip = postProcessTip;
	}

	@Column(name="lookup_query_id")
	public int getLookupQueryId() {
		return lookupQueryId;
	}

	public void setLookupQueryId(int lookupQueryId) {
		this.lookupQueryId = lookupQueryId;
	}

	@Column(name="main_table_field_id")
	public int getMainTableFieldId() {
		return mainTableFieldId;
	}

	public void setMainTableFieldId(int mainTableFieldId) {
		this.mainTableFieldId = mainTableFieldId;
	}
	
	@Transient
	public short getParamTip(){
		return fieldTip;
	}
	
	@Transient
	public short getNotNullFlag(){
		return 0;
	}

	
	@Transient
	public short getSourceTip(){
		return 0;
	}

	
	@Transient
	public String getDefaultValue(){
		return null;
	}
	
	@Transient
	public BigDecimal getMinValue(){
		return null;
	}
	


	@Transient
	public BigDecimal getMaxValue(){
		return null;
	}
	


	@Transient
	public Short getMinLength(){
		return 0;
	}


	@Transient
	public Short getMaxLength(){
		return 0;
	}
	private String projectUuid;
	@Id
	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}

	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}

	
}
