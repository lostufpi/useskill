package br.ufpi.datamining.models.auxiliar;

public class ResultComparePatterns {
	
	private FSPMResultAux resultFSPMBest;
	private FSPMResultAux resultFSPMOthers;
	
	public ResultComparePatterns(FSPMResultAux resultFSPMBest,
			FSPMResultAux resultFSPMOthers) {
		super();
		this.resultFSPMBest = resultFSPMBest;
		this.resultFSPMOthers = resultFSPMOthers;
	}
	
	public FSPMResultAux getResultFSPMBest() {
		return resultFSPMBest;
	}
	public void setResultFSPMBest(FSPMResultAux resultFSPMBest) {
		this.resultFSPMBest = resultFSPMBest;
	}
	public FSPMResultAux getResultFSPMOthers() {
		return resultFSPMOthers;
	}
	public void setResultFSPMOthers(FSPMResultAux resultFSPMOthers) {
		this.resultFSPMOthers = resultFSPMOthers;
	}
	
}
