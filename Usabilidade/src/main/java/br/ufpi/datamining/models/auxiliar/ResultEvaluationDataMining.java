package br.ufpi.datamining.models.auxiliar;

import java.util.Date;
import java.util.List;

import br.ufpi.datamining.models.EvaluationTaskDataMining;
import br.ufpi.datamining.models.vo.FrequentSequentialPatternResultVO;
import br.ufpi.datamining.models.vo.TaskDataMiningVO;

public class ResultEvaluationDataMining {

	private List<FrequentSequentialPatternResultVO> frequentPatterns;
	private ResultDataMining result;
	private EvaluationTaskDataMining evalTask;
	private Date evalTestDateInit;
	private Date evalTestDateEnd;
	private TaskDataMiningVO task;
	
	
	public ResultEvaluationDataMining(
			List<FrequentSequentialPatternResultVO> frequentPatterns,
			ResultDataMining result, EvaluationTaskDataMining evalTask,
			Date evalTestDateInit, Date evalTestDateEnd,
			TaskDataMiningVO task) {
		super();
		this.frequentPatterns = frequentPatterns;
		this.result = result;
		this.evalTask = evalTask;
		this.task = task;
		this.setEvalTestDateInit(evalTestDateInit);
		this.setEvalTestDateEnd(evalTestDateEnd);
	}
	
	public ResultEvaluationDataMining() {
	}

	public List<FrequentSequentialPatternResultVO> getFrequentPatterns() {
		return frequentPatterns;
	}
	public void setFrequentPatterns(
			List<FrequentSequentialPatternResultVO> frequentPatterns) {
		this.frequentPatterns = frequentPatterns;
	}
	public ResultDataMining getResult() {
		return result;
	}
	public void setResult(ResultDataMining result) {
		this.result = result;
	}
	public EvaluationTaskDataMining getEvalTask() {
		return evalTask;
	}
	public void setEvalTask(EvaluationTaskDataMining evalTask) {
		this.evalTask = evalTask;
	}
	public TaskDataMiningVO getTask() {
		return task;
	}
	public void setTask(TaskDataMiningVO task) {
		this.task = task;
	}

	public Date getEvalTestDateInit() {
		return evalTestDateInit;
	}

	public void setEvalTestDateInit(Date evalTestDateInit) {
		this.evalTestDateInit = evalTestDateInit;
	}

	public Date getEvalTestDateEnd() {
		return evalTestDateEnd;
	}

	public void setEvalTestDateEnd(Date evalTestDateEnd) {
		this.evalTestDateEnd = evalTestDateEnd;
	}
	
	
	
}
