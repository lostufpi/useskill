package br.ufpi.datamining.models.auxiliar;

public class ComparePatternsAux {

	private String patternsBest;
	private String patternsOthers;
	private Long testId;
	private Long evalTestId;
	private Long taskId;
	
	private Double minSupBest; 
	private Integer minItensBest;

	private Double minSupOthers; 
	private Integer minItensOthers;	
	
	public ComparePatternsAux() {
		super();
	}
	
	public ComparePatternsAux(String patternsBest, String patternsOthers, Long testId, 
								Long evalTestId, Long taskId, Double minSupBest, Integer minItensBest,
								Double minSupOthers, Integer minItensOthers) {
		super();
		this.patternsBest = patternsBest;
		this.patternsOthers = patternsOthers;
		this.testId = testId;
		this.evalTestId = evalTestId;
		this.taskId = taskId;
		this.minSupBest = minSupBest;
		this.minItensBest = minItensBest;
		this.minSupOthers = minSupOthers;
		this.minItensOthers = minItensOthers;
	}
	
	public Double getMinSupBest() {
		return minSupBest;
	}

	public void setMinSupBest(Double minSupBest) {
		this.minSupBest = minSupBest;
	}

	public Integer getMinItensBest() {
		return minItensBest;
	}

	public void setMinItensBest(Integer minItensBest) {
		this.minItensBest = minItensBest;
	}

	public Double getMinSupOthers() {
		return minSupOthers;
	}

	public void setMinSupOthers(Double minSupOthers) {
		this.minSupOthers = minSupOthers;
	}

	public Integer getMinItensOthers() {
		return minItensOthers;
	}

	public void setMinItensOthers(Integer minItensOthers) {
		this.minItensOthers = minItensOthers;
	}

	public String getPatternsBest() {
		return patternsBest;
	}
	public void setPatternsBest(String patternsBest) {
		this.patternsBest = patternsBest;
	}
	public String getPatternsOthers() {
		return patternsOthers;
	}
	public void setPatternsOthers(String patternsOthers) {
		this.patternsOthers = patternsOthers;
	}

	public Long getTestId() {
		return testId;
	}

	public void setTestId(Long testId) {
		this.testId = testId;
	}

	public Long getEvalTestId() {
		return evalTestId;
	}

	public void setEvalTestId(Long evalTestId) {
		this.evalTestId = evalTestId;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	
	
}
