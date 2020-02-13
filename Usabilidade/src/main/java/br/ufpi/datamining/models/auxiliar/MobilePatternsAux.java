package br.ufpi.datamining.models.auxiliar;

import java.util.ArrayList;

/** 
 * Auxiliar para el manejo de los padrones de WMUS
 * @author María Isabel Limaylla Lunarejo
*/
public class MobilePatternsAux {

	private Long idSmellPattern;
	private String pattern;
	private ArrayList<String> listado;
	
	public Long getIdSmellPattern() {
		return idSmellPattern;
	}
	public void setIdSmellPattern(Long idSmellPattern) {
		this.idSmellPattern = idSmellPattern;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public ArrayList<String> getListado() {
		return listado;
	}
	public void setListado(ArrayList<String> listado) {
		this.listado = listado;
	}
}
