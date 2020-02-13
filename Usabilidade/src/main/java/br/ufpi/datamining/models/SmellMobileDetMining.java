package br.ufpi.datamining.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity  
@Table(name="smellmobiledet")
@NamedQueries({
	@NamedQuery(name = "SmellMobileDetMining.findAll", query = "SELECT t FROM SmellMobileDetMining t"),
	@NamedQuery(name = "SmellMobileDetMining.findby", query = "SELECT t FROM SmellMobileDetMining t WHERE t.idSmell = :idsmell"),
	@NamedQuery(name = "SmellMobileDetMining.findbyId", query = "SELECT t FROM SmellMobileDetMining t WHERE t.id = :id")
})
public class SmellMobileDetMining implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private Long idSmell;
	private Long idSmellPattern;
	private String sEvent;
	private String sQuantity;
	private Long sDuration;
	private String sUsername;
	private String sIndTag;
	private Date createdAt;
	private Date updatedAt;
	
	private SmellMobileMining smellMobileMining;
	
	public SmellMobileDetMining() {
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

	public Long getIdSmell() {
		return idSmell;
	}

	public void setIdSmell(Long idSmell) {
		this.idSmell = idSmell;
	}

	public String getsEvent() {
		return sEvent;
	}

	public void setsEvent(String sEvent) {
		this.sEvent = sEvent;
	}

	public String getsQuantity() {
		return sQuantity;
	}

	public void setsQuantity(String sQuantity) {
		this.sQuantity = sQuantity;
	}

	public Long getsDuration() {
		return sDuration;
	}

	public void setsDuration(Long sDuration) {
		this.sDuration = sDuration;
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
	
	@ManyToOne
    //@JoinColumn(name="idSmell")
	public SmellMobileMining getSmellMobileMining() {
		return smellMobileMining;
	}

	public void setSmellMobileMining(SmellMobileMining smellMobileMining) {
		this.smellMobileMining = smellMobileMining;
	}

	public Long getIdSmellPattern() {
		return idSmellPattern;
	}

	public void setIdSmellPattern(Long idSmellPattern) {
		this.idSmellPattern = idSmellPattern;
	}

	public String getsIndTag() {
		return sIndTag;
	}

	public void setsIndTag(String sIndTag) {
		this.sIndTag = sIndTag;
	}	
		
}
