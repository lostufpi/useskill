package br.ufpi.datamining.models.auxiliar;

import java.util.List;

import br.ufpi.datamining.models.vo.FrequentSequentialPatternResultVO;

public class FSPMResultAux {

	private double minSup;
	private int minItens;
	private boolean uniqueSession;
	private List<FrequentSequentialPatternResultVO> frequentPatterns;
	
	public FSPMResultAux(double minSup, int minItens, boolean uniqueSession,
			List<FrequentSequentialPatternResultVO> frequentPatterns) {
		super();
		this.uniqueSession = uniqueSession;
		this.minSup = minSup;
		this.minItens = minItens;
		this.frequentPatterns = frequentPatterns;
	}
	public double getMinSup() {
		return minSup;
	}
	public void setMinSup(double minSup) {
		this.minSup = minSup;
	}
	public int getMinItens() {
		return minItens;
	}
	public void setMinItens(int minItens) {
		this.minItens = minItens;
	}
	public List<FrequentSequentialPatternResultVO> getFrequentPatterns() {
		return frequentPatterns;
	}
	public void setFrequentPatterns(List<FrequentSequentialPatternResultVO> frequentPatterns) {
		this.frequentPatterns = frequentPatterns;
	}
	public boolean isUniqueSession() {
		return uniqueSession;
	}
	public void setUniqueSession(boolean uniqueSession) {
		this.uniqueSession = uniqueSession;
	}
	
}
