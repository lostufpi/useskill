package br.ufpi.datamining.models.vo;

import java.util.List;
import java.util.Map;

import br.ufpi.datamining.models.auxiliar.TaskMobileSmellAnalysisResult;

public class SmellMobileAnalysisResultVO {
	
	private Map<String, List<TaskMobileSmellAnalysisResult>> tasksMobileAnalysisResult;
	
	private List<TaskMobileSmellAnalysisResult> resultSmellAnalysisGeneral; 
	
	public SmellMobileAnalysisResultVO(Map<String, List<TaskMobileSmellAnalysisResult>> tasksAnalysisResult) {
		super();
		this.tasksMobileAnalysisResult = tasksAnalysisResult;
	}
	
	public SmellMobileAnalysisResultVO(Map<String, List<TaskMobileSmellAnalysisResult>> tasksAnalysisResult, List<TaskMobileSmellAnalysisResult> resultSmellAnalysisGeneral) {
		super();
		this.resultSmellAnalysisGeneral = resultSmellAnalysisGeneral;
		this.tasksMobileAnalysisResult = tasksAnalysisResult;
	}
	
	public Map<String, List<TaskMobileSmellAnalysisResult>> getTasksAnalysisResult() {
		return tasksMobileAnalysisResult;
	}
	public void setTasksAnalysisResult(Map<String, List<TaskMobileSmellAnalysisResult>> tasksAnalysisResult) {
		this.tasksMobileAnalysisResult = tasksAnalysisResult;
	}

	public List<TaskMobileSmellAnalysisResult> getResultSmellAnalysisGeneral() {
		return resultSmellAnalysisGeneral;
	}

	public void setResultSmellAnalysisGeneral(List<TaskMobileSmellAnalysisResult> resultSmellAnalysisGeneral) {
		this.resultSmellAnalysisGeneral = resultSmellAnalysisGeneral;
	}
	
	
}
