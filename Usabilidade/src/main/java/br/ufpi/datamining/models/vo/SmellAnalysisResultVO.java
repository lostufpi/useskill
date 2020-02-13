package br.ufpi.datamining.models.vo;

import java.util.List;
import java.util.Map;

import br.ufpi.datamining.models.auxiliar.TaskSmellAnalysisResult;

public class SmellAnalysisResultVO {
	
	private Map<String, List<TaskSmellAnalysisResult>> tasksAnalysisResult;
	private Map<String, Map<String, Map<String, String>>> actionsAnalysisResult;
	
	public SmellAnalysisResultVO(Map<String, List<TaskSmellAnalysisResult>> tasksAnalysisResult,
			Map<String, Map<String, Map<String, String>>> actionsAnalysisResult) {
		super();
		this.tasksAnalysisResult = tasksAnalysisResult;
		this.actionsAnalysisResult = actionsAnalysisResult;
	}
	
	public Map<String, List<TaskSmellAnalysisResult>> getTasksAnalysisResult() {
		return tasksAnalysisResult;
	}
	public void setTasksAnalysisResult(Map<String, List<TaskSmellAnalysisResult>> tasksAnalysisResult) {
		this.tasksAnalysisResult = tasksAnalysisResult;
	}
	
	public Map<String, Map<String, Map<String, String>>> getActionsAnalysisResult() {
		return actionsAnalysisResult;
	}
	public void setActionsAnalysisResult(Map<String, Map<String, Map<String, String>>> actionsAnalysisResult) {
		this.actionsAnalysisResult = actionsAnalysisResult;
	}
	
}
