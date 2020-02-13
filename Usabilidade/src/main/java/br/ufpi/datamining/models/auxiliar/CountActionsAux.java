package br.ufpi.datamining.models.auxiliar;

public class CountActionsAux {

	private String description;
	private FieldSearch fieldGroup;
	private Long count;
	
	public CountActionsAux(String description, FieldSearch fieldGroup,
			Long count) {
		super();
		this.description = description;
		this.fieldGroup = fieldGroup;
		this.count = count;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public FieldSearch getFieldGroup() {
		return fieldGroup;
	}
	public void setFieldGroup(FieldSearch fieldGroup) {
		this.fieldGroup = fieldGroup;
	}
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
}
