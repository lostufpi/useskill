package br.ufpi.datamining.models.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.google.gson.ExclusionStrategy;

import br.ufpi.datamining.models.ActionSingleDataMining;
import br.ufpi.datamining.models.EvaluationTaskDataMining;
import br.ufpi.datamining.models.SmellMobileDetMining;
import br.ufpi.datamining.models.SmellMobileMining;
import br.ufpi.datamining.models.TaskDataMining;
import br.ufpi.datamining.models.TestDataMining;
import br.ufpi.datamining.utils.GsonExclusionStrategy;
import br.ufpi.models.Tarefa;
import br.ufpi.models.Teste;

public class SmellMobileMiningVO {

	public static ExclusionStrategy exclusionStrategy = new GsonExclusionStrategy(
			TestDataMining.class, TaskDataMining.class, ActionSingleDataMining.class, EvaluationTaskDataMining.class, Teste.class, Tarefa.class);
	
	private Long id;
	private String sSmellType;
	private String sName;
	private String sContent;
	private String sUsername;
	private Date createdAt;
	private Date updatedAt;
	private List<String> pattern;
	private List<SmellMobileDetMining> detail;
	
	public SmellMobileMiningVO() {
		super();
	}
	
	public SmellMobileMiningVO(SmellMobileMining smellMob) {
		super();
		this.id = smellMob.getId();
		this.sSmellType = smellMob.getsSmellType();
		this.sName = smellMob.getsName();
		this.sContent = smellMob.getsContent();
		this.sUsername = smellMob.getsUsername();
		this.createdAt = smellMob.getCreatedAt();
		this.updatedAt = smellMob.getUpdatedAt();
		//this.setPattern(smellMob.getPattern());
		this.setDetail(smellMob.getDetail());
	}
	
	@Id  
	@GeneratedValue
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getsSmellType() {
		return sSmellType;
	}

	public void setsSmellType(String sSmellType) {
		this.sSmellType = sSmellType;
	}

	public String getsName() {
		return sName;
	}

	public void setsName(String sName) {
		this.sName = sName;
	}

	@Column(columnDefinition="text")
	public String getsContent() {
		return sContent;
	}

	public void setsContent(String sContent) {
		this.sContent = sContent;
	}

	public String getsUsername() {
		return sUsername;
	}

	public void setsUsername(String sUsername) {
		this.sUsername = sUsername;
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

	public List<SmellMobileDetMining> getDetail() {
		return detail;
	}

	public void setDetail(List<SmellMobileDetMining> detail) {
		this.detail = detail;
	}

	public List<String> getPattern() {
		return pattern;
	}

	public void setPattern(List<String> pattern) {
		this.pattern = pattern;
	}
	
}
