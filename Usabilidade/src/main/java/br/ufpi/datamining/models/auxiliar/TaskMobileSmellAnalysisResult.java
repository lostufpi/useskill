package br.ufpi.datamining.models.auxiliar;

import java.util.List;
import java.util.Map;

import br.ufpi.datamining.models.ActionDataMining;

/** 
 * Clase para guardar el resultado del analisis de los WMUS
 * @author María Isabel Limaylla Lunarejo
*/

public class TaskMobileSmellAnalysisResult {
	
	private String name;
	private List<SessionGraph> sessions;
	private Double detectionRate;
	private List<Map<String, String>> listEventos; 
	
	private String patron;
	private Map<String, List<ActionDataMining>> listActions;
	private Map<String, List<String>> listEventosAction;
	private String lcs;
	
	public TaskMobileSmellAnalysisResult(String name, List<SessionGraph> sessions, Double detectionRate, List<Map<String, String>> listEventos, String patron) {
		super();
		this.name = name;
		this.sessions = sessions;
		this.detectionRate = detectionRate;
		this.setListEventos(listEventos);
		this.setPatron(patron);		
	}
	
	public TaskMobileSmellAnalysisResult(String patron, Map<String, List<ActionDataMining>> listActions, Map<String, List<String>> listEventosAction, String lcs) {
		super();
		this.setPatron(patron);
		this.setListActions(listActions);
		this.setListEventosAction(listEventosAction);
		if (lcs!=null) {
			this.setLcs(lcs);
		}		
	}
	
	public String getName() {
		return name;
	}
	public List<SessionGraph> getSessions() {
		return sessions;
	}
	public Double getDetectionRate() {
		return detectionRate;
	}

	public List<Map<String, String>> getListEventos() {
		return listEventos;
	}

	public void setListEventos(List<Map<String, String>> listEventos) {
		this.listEventos = listEventos;
	}

	public String getPatron() {
		return patron;
	}

	public void setPatron(String patron) {
		this.patron = patron;
	}

	public Map<String, List<ActionDataMining>> getListActions() {
		return listActions;
	}

	public void setListActions(Map<String, List<ActionDataMining>> listActions) {
		this.listActions = listActions;
	}

	public Map<String, List<String>> getListEventosAction() {
		return listEventosAction;
	}

	public void setListEventosAction(Map<String, List<String>> listEventosAction) {
		this.listEventosAction = listEventosAction;
	}

	public String getLcs() {
		return lcs;
	}

	public void setLcs(String lcs) {
		this.lcs = lcs;
	}
	
}
