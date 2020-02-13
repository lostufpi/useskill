package br.ufpi.datamining.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import br.ufpi.models.Action;



@NamedQueries({
	@NamedQuery(name = "ActionDataMining.findAll", query = "SELECT a FROM ActionDataMining a"),
	@NamedQuery(name = "ActionDataMining.findById", query = "SELECT a FROM ActionDataMining a WHERE a.id = :id")
})
@Entity  
@Table(name="ActionDataMinings")  
public class ActionDataMining implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String sActionType;
	private String sContent;
	private Integer sPosX;
	private Integer sPosY;
	private String sTag;
	private String sTagIndex;
	private Long sTime;
	private String sUrl;
	private String sContentText;
	private String sClass;
	private String sId;
	private String sName;
	private String sXPath;
	private String sUserAgent;
	private String sClient;
	private Integer sVersion;
	private String sUsername;
	private String sRole;
	private String sJhm;
	private String sActionJhm;
	private String sSectionJhm;
	private String sStepJhm;
	private Boolean sDeleted;
	private Date createdAt;
	private Date updatedAt;
	/*CAMBIOS MILL*/
	private Integer sMobile;
	private String sMobileConf;
	private Long sDuration;	
		
	public Integer getsMobile() {
		return sMobile;
	}

	public void setsMobile(Integer sMobile) {
		this.sMobile = sMobile;
	}

	public String getsMobileConf() {
		return sMobileConf;
	}

	public void setsMobileConf(String sMobileConf) {
		this.sMobileConf = sMobileConf;
	}

	public Long getsDuration() {
		return sDuration;
	}

	public void setsDuration(Long sDuration) {
		this.sDuration = sDuration;
	}
	/*FIN MILL*/
	
	public ActionDataMining(Action a) {
		super();
		this.id = a.getId();
		this.sActionType = a.getsActionType();
		this.sContent = a.getsContent();
		this.sPosX = a.getsPosX();
		this.sPosY = a.getsPosY();
		this.sTag = a.getsTag();
		this.sTagIndex = a.getsTagIndex();
		this.sTime = a.getsTime();
		this.sUrl = a.getsUrl();
		this.sClass = a.getsClass();
		this.sId = a.getsId();
		this.sName = a.getsName();
		this.sXPath = a.getsXPath();
	}
	
	public ActionDataMining() {
		super();
	}
	
	@Id  
	@GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getsActionType() {
		return sActionType;
	}
	public void setsActionType(String sActionType) {
		this.sActionType = sActionType;
	}
	@Column(columnDefinition="text")
	public String getsContent() {
		return sContent;
	}
	public void setsContent(String sContent) {
		this.sContent = sContent;
	}
	public Integer getsPosX() {
		return sPosX;
	}
	public void setsPosX(Integer sPosX) {
		this.sPosX = sPosX;
	}
	public Integer getsPosY() {
		return sPosY;
	}
	public void setsPosY(Integer sPosY) {
		this.sPosY = sPosY;
	}
	public String getsTag() {
		return sTag;
	}
	public void setsTag(String sTag) {
		this.sTag = sTag;
	}
	public String getsTagIndex() {
		return sTagIndex;
	}
	public void setsTagIndex(String sTagIndex) {
		this.sTagIndex = sTagIndex;
	}
	public Long getsTime() {
		return sTime;
	}
	public void setsTime(Long sTime) {
		this.sTime = sTime;
	}
	public String getsUrl() {
		return sUrl;
	}
	public void setsUrl(String sUrl) {
		this.sUrl = sUrl;
	}
	@Column(columnDefinition="text")
	public String getsContentText() {
		return sContentText;
	}
	public void setsContentText(String sContentText) {
		this.sContentText = sContentText;
	}
	public String getsClass() {
		return sClass;
	}
	public void setsClass(String sClass) {
		this.sClass = sClass;
	}
	public String getsId() {
		return sId;
	}
	public void setsId(String sId) {
		this.sId = sId;
	}
	public String getsName() {
		return sName;
	}
	public void setsName(String sName) {
		this.sName = sName;
	}
	@Column(columnDefinition="text")
	public String getsXPath() {
		return sXPath;
	}
	public void setsXPath(String sXPath) {
		this.sXPath = sXPath;
	}
	@Column(columnDefinition="text")
	public String getsUserAgent() {
		return sUserAgent;
	}
	public void setsUserAgent(String sUserAgent) {
		this.sUserAgent = sUserAgent;
	}
	public String getsClient() {
		return sClient;
	}
	public void setsClient(String sClient) {
		this.sClient = sClient;
	}
	public Integer getsVersion() {
		return sVersion;
	}
	public void setsVersion(Integer sVersion) {
		this.sVersion = sVersion;
	}
	public String getsUsername() {
		return sUsername;
	}
	public void setsUsername(String sUsername) {
		this.sUsername = sUsername;
	}
	public String getsRole() {
		return sRole;
	}
	public void setsRole(String sRole) {
		this.sRole = sRole;
	}
	public String getsJhm() {
		return sJhm;
	}
	public void setsJhm(String sJhm) {
		this.sJhm = sJhm;
	}
	public String getsActionJhm() {
		return sActionJhm;
	}
	public void setsActionJhm(String sActionJhm) {
		this.sActionJhm = sActionJhm;
	}
	public String getsSectionJhm() {
		return sSectionJhm;
	}
	public void setsSectionJhm(String sSectionJhm) {
		this.sSectionJhm = sSectionJhm;
	}
	public Boolean getsDeleted() {
		return sDeleted;
	}
	public void setsDeleted(Boolean sDeleted) {
		this.sDeleted = sDeleted;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public Date getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
	public String getsStepJhm() {
		return sStepJhm;
	}
	public void setsStepJhm(String sStepJhm) {
		this.sStepJhm = sStepJhm;
	}
	
	@Override
	public String toString(){
		return "[" + this.getId() + ", " + new Date(this.getsTime()) + "] - " + this.getsActionType() + ", Jhm: " + this.getsJhm() + ", " + this.getsStepJhm() + " / Elemento: " + this.getsTag() + "#" + this.getsId() + " [" + this.getsXPath() + "]";
	}
}
