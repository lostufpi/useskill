package br.ufpi.datamining.models.auxiliar;

import java.util.List;

public class StackedAreaChart {

	private String description;
	private String descX;
	private String descY;
	private List<XYSerie> series;
	
	public StackedAreaChart(String description, String descX, String descY, List<XYSerie> series) {
		super();
		this.description = description;
		this.descX = descX;
		this.descY = descY;
		this.series = series;
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
	public List<XYSerie> getDatasets() {
		return series;
	}
	public void setDatasets(List<XYSerie> datasets) {
		this.series = datasets;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
