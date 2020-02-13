package br.ufpi.datamining.models.auxiliar;

import java.util.List;

public class TaskSmellAnalysisGroupedResult {
	
	private String name;
	private List<List<SessionGraph>> sessions;
	private Double detectionRate;
	
	public TaskSmellAnalysisGroupedResult(String name, List<List<SessionGraph>> sessions, Double detectionRate) {
		super();
		this.name = name;
		this.sessions = sessions;
		this.detectionRate = detectionRate;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<List<SessionGraph>> getSessions() {
		return sessions;
	}
	public void setSessions(List<List<SessionGraph>> sessions) {
		this.sessions = sessions;
	}
	public Double getDetectionRate() {
		return detectionRate;
	}
	public void setDetectionRate(Double detectionRate) {
		this.detectionRate = detectionRate;
	}
	
}
