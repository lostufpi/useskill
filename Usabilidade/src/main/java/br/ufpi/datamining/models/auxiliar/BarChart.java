package br.ufpi.datamining.models.auxiliar;

import java.util.Map;

public class BarChart {
	private String description;
	private String descX;
	private String descY;
	private Map<String, Double> series;
	private Map<String, Map<String, String>> actionsAttributes;
	
	public BarChart(String description, String descX, String descY, Map<String, Double> series, Map<String, Map<String, String>> actionsAttributes) {
		super();
		this.description = description;
		this.descX = descX;
		this.descY = descY;
		this.series = series;
		this.actionsAttributes = actionsAttributes;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescX() {
		return descX;
	}

	public void setDescX(String descX) {
		this.descX = descX;
	}

	public String getDescY() {
		return descY;
	}

	public void setDescY(String descY) {
		this.descY = descY;
	}

	public Map<String, Double> getSeries() {
		return series;
	}

	public void setSeries(Map<String, Double> series) {
		this.series = series;
	}

	public Map<String, Map<String, String>> getActionsAttributes() {
		return actionsAttributes;
	}

	public void setActionsAttributes(Map<String, Map<String, String>> actionsAttributes) {
		this.actionsAttributes = actionsAttributes;
	}
	
}
