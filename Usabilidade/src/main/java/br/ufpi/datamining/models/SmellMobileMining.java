package br.ufpi.datamining.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity  
@Table(name="smellMobile")
@NamedQueries({
	@NamedQuery(name = "SmellMobileMining.findAll", query = "SELECT t FROM SmellMobileMining t"),
	@NamedQuery(name = "SmellMobileMining.findby", query = "SELECT t FROM SmellMobileMining t WHERE t.sUsername = :userName"),
	@NamedQuery(name = "SmellMobileMining.findById", query = "SELECT t FROM SmellMobileMining t WHERE t.id = :idsmell")
})
public class SmellMobileMining implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String sSmellType;
	private String sName;
	private String sContent;
	private String sUsername;
	private Date createdAt;
	private Date updatedAt;
	//private String pattern;
	private List<SmellMobileDetMining> detail;
	
	public SmellMobileMining() {
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
	
	@OneToMany(mappedBy="smellMobileMining", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	public List<SmellMobileDetMining> getDetail() {
		return detail;
	}

	public void setDetail(List<SmellMobileDetMining> detail) {
		this.detail = detail;
	}

	/*public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}*/
		
}
