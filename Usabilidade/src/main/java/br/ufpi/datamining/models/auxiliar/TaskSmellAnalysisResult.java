package br.ufpi.datamining.models.auxiliar;

import java.util.List;

public class TaskSmellAnalysisResult {
	
	private String name;
	private List<SessionGraph> sessions;
	private Double detectionRate;
	
	public TaskSmellAnalysisResult(String name, List<SessionGraph> sessions, Double detectionRate) {
		super();
		this.name = name;
		this.sessions = sessions;
		this.detectionRate = detectionRate;
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
	
}
