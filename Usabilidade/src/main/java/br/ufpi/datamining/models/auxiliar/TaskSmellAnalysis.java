package br.ufpi.datamining.models.auxiliar;

import java.util.List;

public class TaskSmellAnalysis {
	
	private String name;
	private List<SessionResultDataMining> sessions;
	
	public TaskSmellAnalysis(String name, List<SessionResultDataMining> sessions) {
		super();
		this.name = name;
		this.sessions = sessions;
	}

	public String getName() {
		return name;
	}

	public List<SessionResultDataMining> getSessions() {
		return sessions;
	}
	
}
