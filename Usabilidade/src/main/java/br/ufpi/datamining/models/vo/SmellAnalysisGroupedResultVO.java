package br.ufpi.datamining.models.vo;

import java.util.List;
import java.util.Map;

import br.ufpi.datamining.models.auxiliar.TaskSmellAnalysisGroupedResult;

public class SmellAnalysisGroupedResultVO {
	private Map<String, List<TaskSmellAnalysisGroupedResult>> tasksAnalysisGroupedResult;
	private Map<String, Map<String, Map<String, String>>> actionsAnalysisResult;
	
	public SmellAnalysisGroupedResultVO(Map<String, List<TaskSmellAnalysisGroupedResult>> tasksAnalysisGroupedResult,
			Map<String, Map<String, Map<String, String>>> actionsAnalysisResult) {
		super();
		this.tasksAnalysisGroupedResult = tasksAnalysisGroupedResult;
		this.actionsAnalysisResult = actionsAnalysisResult;
	}
	
	public Map<String, List<TaskSmellAnalysisGroupedResult>> getTasksAnalysisGroupedResult() {
		return tasksAnalysisGroupedResult;
	}
	public void setTasksAnalysisGroupedResult(Map<String, List<TaskSmellAnalysisGroupedResult>> tasksAnalysisResult) {
		this.tasksAnalysisGroupedResult = tasksAnalysisResult;
	}
	public Map<String, Map<String, Map<String, String>>> getActionsAnalysisResult() {
		return actionsAnalysisResult;
	}
	public void setActionsAnalysisResult(Map<String, Map<String, Map<String, String>>> actionsAnalysisResult) {
		this.actionsAnalysisResult = actionsAnalysisResult;
	}
}
