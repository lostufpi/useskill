package br.ufpi.datamining.models.auxiliar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ResultDataMining {

	private Double actionsAverageOk;
	private Double timesAverageOk;
	
	private Integer countSessionsSuccess;
	private Integer countSessionsError;
	private Integer countSessionsRepeat;
	private Integer countSessionsThreshold;
	
	private Integer countSessions;
	private Double rateSuccess;
	private Double rateRequired;
	private HashMap<String, Integer> actionsRequiredTask;
	
	
	private Double meanActionsOk;
	private Double meanTimesOk;
	private Double stdDevActionsOk;
	private Double stdDevTimesOk;
	private Double maxActionsOk;
	private Double maxTimesOk;
	private Double minActionsOk;
	private Double minTimesOk;
	private Double lastMinSup;
	private Integer lastMinItens;
	
	private HashMap<String, String> pageViewActionIds;
	private HashMap<String, Integer> pageViewActionCount;
	
	private HashMap<String, String> usersSequences;
	
	private List<UserResultDataMining> users;
	private List<SessionResultDataMining> sessions;
	
	public ResultDataMining() {
		this.users = new ArrayList<UserResultDataMining>();
		this.sessions = new ArrayList<SessionResultDataMining>();
	}
	
	public ResultDataMining(List<UserResultDataMining> users, List<SessionResultDataMining> sessions, 
			Double actionsAverageOk, Double timesAverageOk,
			Integer countSessionsSuccess, Integer countSessionsError, Integer countSessionsRepeat,
			Integer countSessionsThreshold, HashMap<String, String> pageViewActionIds, HashMap<String, Integer> pageViewActionCount) {
		this();
		this.users = users;
		this.sessions = sessions;
		this.actionsAverageOk = actionsAverageOk;
		this.timesAverageOk = timesAverageOk;
		
		this.countSessionsSuccess = countSessionsSuccess;
		this.countSessionsError = countSessionsError;
		this.countSessionsRepeat = countSessionsRepeat;
		this.countSessionsThreshold = countSessionsThreshold;
		
		this.setPageViewActionIds(pageViewActionIds);
		this.setPageViewActionCount(pageViewActionCount);
		
		this.setCountSessions(countSessionsSuccess + countSessionsError + countSessionsRepeat + countSessionsThreshold);
		this.setRateSuccess(((double)countSessionsSuccess/this.getCountSessions())*100);
	}
	
	public List<UserResultDataMining> getUsers() {
		return users;
	}
	public void setUsers(List<UserResultDataMining> users) {
		this.users = users;
	}
	public List<SessionResultDataMining> getSessions() {
		return sessions;
	}
	public void setSessions(List<SessionResultDataMining> sessions) {
		this.sessions = sessions;
	}

	public Integer getCountSessionsSuccess() {
		return countSessionsSuccess;
	}

	public void setCountSessionsSuccess(Integer countSessionsSuccess) {
		this.countSessionsSuccess = countSessionsSuccess;
	}

	public Integer getCountSessionsError() {
		return countSessionsError;
	}

	public void setCountSessionsError(Integer countSessionsError) {
		this.countSessionsError = countSessionsError;
	}

	public Integer getCountSessionsRepeat() {
		return countSessionsRepeat;
	}

	public void setCountSessionsRepeat(Integer countSessionsRepeat) {
		this.countSessionsRepeat = countSessionsRepeat;
	}

	public Integer getCountSessionsThreshold() {
		return countSessionsThreshold;
	}

	public void setCountSessionsThreshold(Integer countSessionsThreshold) {
		this.countSessionsThreshold = countSessionsThreshold;
	}

	public Double getRateSuccess() {
		return rateSuccess;
	}

	public void setRateSuccess(Double rateSuccess) {
		this.rateSuccess = rateSuccess;
	}

	public Integer getCountSessions() {
		return countSessions;
	}

	public void setCountSessions(Integer countSessions) {
		this.countSessions = countSessions;
	}

	public Double getActionsAverageOk() {
		return actionsAverageOk;
	}

	public void setActionsAverageOk(Double actionsAverageOk) {
		this.actionsAverageOk = actionsAverageOk;
	}

	public Double getTimesAverageOk() {
		return timesAverageOk;
	}

	public void setTimesAverageOk(Double timesAverageOk) {
		this.timesAverageOk = timesAverageOk;
	}

	public Double getMeanActionsOk() {
		return meanActionsOk;
	}

	public void setMeanActionsOk(Double meanActionsOk) {
		this.meanActionsOk = meanActionsOk;
	}

	public Double getMeanTimesOk() {
		return meanTimesOk;
	}

	public void setMeanTimesOk(Double meanTimesOk) {
		this.meanTimesOk = meanTimesOk;
	}

	public Double getStdDevActionsOk() {
		return stdDevActionsOk;
	}

	public void setStdDevActionsOk(Double stdDevActionsOk) {
		this.stdDevActionsOk = stdDevActionsOk;
	}

	public Double getStdDevTimesOk() {
		return stdDevTimesOk;
	}

	public void setStdDevTimesOk(Double stdDevTimesOk) {
		this.stdDevTimesOk = stdDevTimesOk;
	}

	public Double getMaxActionsOk() {
		return maxActionsOk;
	}

	public void setMaxActionsOk(Double maxActionsOk) {
		this.maxActionsOk = maxActionsOk;
	}

	public Double getMaxTimesOk() {
		return maxTimesOk;
	}

	public void setMaxTimesOk(Double maxTimesOk) {
		this.maxTimesOk = maxTimesOk;
	}

	public Double getMinActionsOk() {
		return minActionsOk;
	}

	public void setMinActionsOk(Double minActionsOk) {
		this.minActionsOk = minActionsOk;
	}

	public Double getMinTimesOk() {
		return minTimesOk;
	}

	public void setMinTimesOk(Double minTimesOk) {
		this.minTimesOk = minTimesOk;
	}

	public HashMap<String, String> getPageViewActionIds() {
		return pageViewActionIds;
	}

	public void setPageViewActionIds(HashMap<String, String> pageViewActionIds) {
		this.pageViewActionIds = pageViewActionIds;
	}

	public HashMap<String, Integer> getPageViewActionCount() {
		return pageViewActionCount;
	}

	public void setPageViewActionCount(HashMap<String, Integer> pageViewActionCount) {
		this.pageViewActionCount = pageViewActionCount;
	}

	public Double getRateRequired() {
		return rateRequired;
	}

	public void setRateRequired(Double rateRequired) {
		this.rateRequired = rateRequired;
	}

	public HashMap<String, Integer> getActionsRequiredTask() {
		return actionsRequiredTask;
	}

	public void setActionsRequiredTask(HashMap<String, Integer> actionsRequiredTask) {
		this.actionsRequiredTask = actionsRequiredTask;
	}

	public HashMap<String, String> getUsersSequences() {
		return usersSequences;
	}

	public void setUsersSequences(HashMap<String, String> usersSequences) {
		this.usersSequences = usersSequences;
	}

	public Double getLastMinSup() {
		return lastMinSup;
	}

	public void setLastMinSup(Double lastMinSup) {
		this.lastMinSup = lastMinSup;
	}

	public Integer getLastMinItens() {
		return lastMinItens;
	}

	public void setLastMinItens(Integer lastMinItens) {
		this.lastMinItens = lastMinItens;
	}
		
	@Override
	public String toString() {
		String s = "---------- Result: ----------\n";
		s += "actionsAverageOk: " + actionsAverageOk + ";\n";
		s += "timesAverageOk: " + timesAverageOk + ";\n";
		s += "countSessionsSuccess: " + countSessionsSuccess + ";\n";
		s += "countSessionsError: " + countSessionsError + ";\n";
		s += "countSessionsRepeat: " + countSessionsRepeat + ";\n";
		s += "countSessionsThreshold: " + countSessionsThreshold + ";\n";
		s += "countSessions: " + countSessions + ";\n";
		s += "rateSuccess: " + rateSuccess + ";\n";
		s += "rateRequired: " + rateRequired + ";\n";
		
		s += mapIntegerToString("actionsRequiredTask", actionsRequiredTask);
		
		s += "meanActionsOk: " + meanActionsOk + ";\n";
		s += "meanTimesOk: " + meanTimesOk + ";\n";
		s += "stdDevActionsOk: " + stdDevActionsOk + ";\n";
		s += "stdDevTimesOk: " + stdDevTimesOk + ";\n";
		s += "maxActionsOk: " + maxActionsOk + ";\n";
		s += "maxTimesOk: " + maxTimesOk + ";\n";
		s += "minActionsOk: " + minActionsOk + ";\n";
		s += "minTimesOk: " + minTimesOk + ";\n";
		s += "lastMinSup: " + lastMinSup + ";\n";
		s += "lastMinItens: " + lastMinItens + ";\n";
		
		s += mapStringToString("pageViewActionIds", pageViewActionIds);
		s += mapIntegerToString("pageViewActionCount", pageViewActionCount);
		s += mapStringToString("usersSequences", usersSequences);
		
		s += "users.size: " + users.size() + ";\n";
		s += "sessions.size: " + sessions.size() + ";\n";
		
		return s;
	}
	
	private String mapIntegerToString(String mapName, HashMap<String, Integer> map) {
		Set<String> keySet = map.keySet();
		String s = mapName + ".size: " + keySet.size() + ";\n";
		for (String k : keySet) {
		//	s += mapName + "[" + k + "]: " + map.get(k) + ";\n";
		}
		s += "\n";
		return s;
	}
	
	private String mapStringToString(String mapName, HashMap<String, String> map) {
		Set<String> keySet = map.keySet();
		String s = mapName + ".size: " + keySet.size() + ";\n";
		for (String k : keySet) {
		//	s += mapName + "[" + k + "]: " + map.get(k) + ";\n";
		}
		s += "\n";
		return s;
	}
}
