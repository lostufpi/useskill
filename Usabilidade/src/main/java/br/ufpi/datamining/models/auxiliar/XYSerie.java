package br.ufpi.datamining.models.auxiliar;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.xy.XYCoordinate;

public class XYSerie {

	private String description;
	private List<XYCoordinate> coordinates;
	
	public XYSerie(String description, List<XYCoordinate> coordinates) {
		super();
		this.description = description;
		this.coordinates = coordinates;
	}
	
	public XYSerie(String description) {
		super();
		this.description = description;
		this.coordinates = new ArrayList<XYCoordinate>();
	}
	
	public void addCoordinate (XYCoordinate coordinate) {
		coordinates.add(coordinate);
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<XYCoordinate> getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(List<XYCoordinate> coordinates) {
		this.coordinates = coordinates;
	}
	
	
}
