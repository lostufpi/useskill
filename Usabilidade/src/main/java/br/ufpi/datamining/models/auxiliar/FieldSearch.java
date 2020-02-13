package br.ufpi.datamining.models.auxiliar;

public class FieldSearch {
	private String field;
	private String alias;
	private Object value;
	private FieldSearchComparatorEnum comparator;
	
	public FieldSearch(String field, String alias, Object value, FieldSearchComparatorEnum comparator) {
		super();
		this.field = field;
		this.setAlias(alias);
		this.value = value;
		this.setComparator(comparator);
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public FieldSearchComparatorEnum getComparator() {
		return comparator;
	}
	public void setComparator(FieldSearchComparatorEnum comparator) {
		this.comparator = comparator;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
}
