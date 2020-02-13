package br.ufpi.datamining.models.auxiliar;

public enum FieldSearchComparatorEnum {
	EQUALS("="),
	NOTEQUALS("!="),
	GREATER_THAN(">"),
	LESS_THAN("<"),
	GREATER_EQUALS_THAN(">="),
	LESS_EQUALS_THAN("<=");
	
	private String desc;
	
	FieldSearchComparatorEnum(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}
	
}
