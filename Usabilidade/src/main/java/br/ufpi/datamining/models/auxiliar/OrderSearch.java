package br.ufpi.datamining.models.auxiliar;

public class OrderSearch {
	private String field;
	private boolean ascending;
	
	public OrderSearch(String field, boolean ascending) {
		super();
		this.field = field;
		this.ascending = ascending;
	}
	public boolean isAscending() {
		return ascending;
	}
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	
}
