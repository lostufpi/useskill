package br.ufpi.datamining.analisys;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import br.ufpi.datamining.analisys.fuzzy.FuzzyCMeansAlgorithm;
import br.ufpi.datamining.analisys.fuzzy.Point;
import br.ufpi.datamining.models.EvaluationTaskDataMining;
import br.ufpi.datamining.models.TaskDataMining;
import br.ufpi.datamining.models.auxiliar.SessionResultDataMining;
import br.ufpi.datamining.models.auxiliar.UserResultDataMining;
import br.ufpi.util.ApplicationPath;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionDiscrete;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionPieceWiseLinear;
import net.sourceforge.jFuzzyLogic.membership.Value;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;
import net.sourceforge.jFuzzyLogic.rule.Rule;
import net.sourceforge.jFuzzyLogic.rule.RuleBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;

public class Fuzzy {
	
	private static final String FCL_PATH = "/Volumes/Backup/Trabalhos/Eclipse/workspace/Usabilidade/src/main/webapp/";
	private static final String FCL_AT = "files/fuzzy/datamining/priority.fcl";
	private static final String FCL_ATC = "files/fuzzy/datamining/priority3.fcl";
	private static final String FCL_ATC_CMEANS = FCL_PATH+"files/fuzzy/datamining/priority-fcm.fcl";
	private static final String FCL_EftEfcSes_CMEANS = FCL_PATH+"files/fuzzy/datamining/priority-eft-efc-ses-fcm.fcl";
	private static final String FCL_EftEfc_CMEANS = FCL_PATH+"files/fuzzy/datamining/priority-eft-efc-fcm.fcl";
	
	/***** FUZZY *****/
	
	public static double fuzzyPriority(double time, double actions, boolean debug) throws IOException {
		HashMap<String, Double> params = new HashMap<String, Double>();
		params.put("time", time);
		params.put("actions", actions);
        return fuzzyParams(ApplicationPath.getFilePath(FCL_AT), params, "priority", debug);
	}
	
	public static double fuzzyPriority3(double c, double t, double a, boolean debug) throws IOException {
		HashMap<String, Double> params = new HashMap<String, Double>();
		params.put("c", c);
		params.put("t", t);
		params.put("a", a);
        return fuzzyParams(ApplicationPath.getFilePath(FCL_ATC), params, "priority", debug);
	}
	
	public static double fuzzyParams(String filePath, HashMap<String, Double> params, String sResult, boolean debug) throws IOException {
        FIS fis = FIS.load(filePath, true);
        Set<String> keySet = params.keySet();

        // Set inputs
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
        	String key = it.next();
            fis.setVariable(key, params.get(key));
        }

        // Evaluate
        fis.evaluate();
        double value = fis.getVariable(sResult).getValue();
        System.out.println(fis.getVariable(sResult).getDefuzzifier().getName());
        
        if(debug){
        	// Show 
            JFuzzyChart.get().chart(fis);
            // Show output variable's chart
            Variable tip = fis.getVariable(sResult);
            JFuzzyChart.get().chart(tip, tip.getDefuzzifier(), true);

//            // Print ruleSet
//            System.out.println(fis);
//            System.out.print("Input value: ");
//            it = keySet.iterator();
//            while (it.hasNext()) {
//            	String key = it.next();
//                System.out.print(params.get(key) + "(" + key + "), ");
//            }
//            System.out.println("\nOutput value: " + fis.getVariable(sResult).getValue()); 
//            
//            // Show each rule (and degree of support)
//            Set<Entry<String, RuleBlock>> ruleBlocksSet = fis.getFunctionBlock("fuzzy").getRuleBlocks().entrySet();
//    		for(Entry<String, RuleBlock> entry : ruleBlocksSet){
//            	System.out.println("RuleBlock: "+entry.getKey());
//            	for(Rule rule : entry.getValue()){
//            		System.out.println(rule);
//            	}
//            }
        }
        
        return value;
	}
	
	/***** FUZZY WITH CMEANS *****/
	
	/* FUZZY CMEANS - RESULTADOS DAS SESSÕES */
	
	public static void executeFuzzySystemWithFCMSession(List<SessionResultDataMining> sessions, boolean ignoreZero, boolean debug) {
		/*
		FIS fis = createMembershipFunctionsWithFCMSession(sessions, ignoreZero, FCL_ATC_CMEANS);
		
		for(SessionResultDataMining s : sessions){
			if(ignoreZero){
				if(!s.isClassificationSuccess()){
					s.setFuzzyPriority(0d);
					continue;
				}
			}
			
			fis.setVariable("actions", s.getActionsNormalized());
			fis.setVariable("times", s.getTimeNormalized());
			fis.setVariable("completeness", s.getUserRateSuccess());
			
	        fis.evaluate();
	        double defuzzifiedValue = fis.getVariable("priority").getLatestDefuzzifiedValue();
	        s.setFuzzyPriority(defuzzifiedValue);
	        
	        System.out.println(s.getUsername() + " -> (" + s.getActionsNormalized() + ", " + s.getTimeNormalized() + ", " + s.getUserRateSuccess() + ") = " + s.getFuzzyPriority());
		}
		
		if(debug){
			 JFuzzyChart.get().chart(fis);
		}
		*/
	}
	
	public static FIS createMembershipFunctionsWithFCMSession(List<SessionResultDataMining> sessions, boolean ignoreZero, String fclPath){
		/*
		//Fuzzy CMeans
		ArrayList<Point> actionsPoints = new ArrayList<Point>(), 
				timePoints = new ArrayList<Point>(), 
				completedPoints = new ArrayList<Point>();
		
		long globalCount = 1l;
		for(SessionResultDataMining s : sessions){
			if(ignoreZero){
				if(!s.isClassificationSuccess()){
					continue;
				}
			}
			actionsPoints.add(new Point(globalCount++, s.getActionsNormalized(), 1.0));
			timePoints.add(new Point(globalCount++, s.getTimeNormalized(), 1.0));
			completedPoints.add(new Point(globalCount++, s.getUserRateSuccess(), 1.0));
		}
		
		FuzzyCMeansAlgorithm cMeans = new FuzzyCMeansAlgorithm(3, 2.0, actionsPoints, 0.01, null);
		MembershipFunctionPieceWiseLinear[] actionsMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		//MembershipFunctionPieceWiseLinear[] actionsMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		//time
		cMeans = new FuzzyCMeansAlgorithm(3, 2.0, timePoints, 0.01, null);
		MembershipFunctionPieceWiseLinear[] timeMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		//completed
		cMeans = new FuzzyCMeansAlgorithm(3, 2.0, completedPoints, 0.01, null);
		MembershipFunctionPieceWiseLinear[] completedMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		
		FIS fis = FIS.load(fclPath, true);
		
		fis.getFunctionBlock("fuzzyCMeans").getVariable("actions").add("Low", actionsMembershipFunctions[0]);
		fis.getFunctionBlock("fuzzyCMeans").getVariable("actions").add("Moderate", actionsMembershipFunctions[1]);
		fis.getFunctionBlock("fuzzyCMeans").getVariable("actions").add("High", actionsMembershipFunctions[2]);
		
		fis.getFunctionBlock("fuzzyCMeans").getVariable("times").add("Low", timeMembershipFunctions[0]);
		fis.getFunctionBlock("fuzzyCMeans").getVariable("times").add("Moderate", timeMembershipFunctions[1]);
		fis.getFunctionBlock("fuzzyCMeans").getVariable("times").add("High", timeMembershipFunctions[2]);
		
		fis.getFunctionBlock("fuzzyCMeans").getVariable("completeness").add("Low", completedMembershipFunctions[0]);
		fis.getFunctionBlock("fuzzyCMeans").getVariable("completeness").add("Moderate", completedMembershipFunctions[1]);
		fis.getFunctionBlock("fuzzyCMeans").getVariable("completeness").add("High", completedMembershipFunctions[2]);
		
		return fis;
		*/
		
		return null;
	}
	
	/* FUZZY CMEANS - RESULTADOS DOS USUÁRIOS */
	
	public static void executeFuzzySystemWithFCM(List<UserResultDataMining> usersResult, boolean ignoreZero, boolean debug) {
		FIS fis = createMembershipFunctionsWithFCM(usersResult, ignoreZero, FCL_ATC_CMEANS);
		
		for(UserResultDataMining u : usersResult){
			if(ignoreZero){
				if(u.getActionsAvarageOkNormalized() == 0 && u.getTimesAvarageOkNormalized() == 0){
					u.setFuzzyPriority(0d);
					continue;
				}
			}
			
			/* Set inputs [Importance x Coupling x Complexity] of the class */
			fis.setVariable("actions", u.getActionsAvarageOkNormalized());
			fis.setVariable("times", u.getActionsAvarageOkNormalized());
			fis.setVariable("completeness", u.getUncompleteNormalized());
			
			/* Evaluate */
	        fis.evaluate();
	        double defuzzifiedValue = fis.getVariable("priority").getLatestDefuzzifiedValue();
	        u.setFuzzyPriority(defuzzifiedValue);
	        
	        System.out.println(u.getUsername() + " -> (" + u.getActionsAvarageOkNormalized() + ", " + u.getTimesAvarageOkNormalized() + ", " + u.getUncompleteNormalized() + ") = " + u.getFuzzyPriority());
		}
		
		/* Just for debug: Show chart*/ 
		if(debug){
			 JFuzzyChart.get().chart(fis);
		}
	}
	
	public static FIS createMembershipFunctionsWithFCM(List<UserResultDataMining> usersResult, boolean ignoreZero, String fclPath){
		//Fuzzy CMeans
		ArrayList<Point> actionsPoints = new ArrayList<Point>(), 
				timePoints = new ArrayList<Point>(), 
				completedPoints = new ArrayList<Point>();
		
		long globalCount = 1l;
		for(UserResultDataMining u : usersResult){
			if(ignoreZero){
				if(u.getActionsAvarageOkNormalized() == 0 && u.getTimesAvarageOkNormalized() == 0){
					System.out.println(u.getUsername()+"IGNORE");
					continue;
				}
			}
			System.out.println(u.getUsername()+"OK");
			actionsPoints.add(new Point(globalCount++, u.getActionsAvarageOkNormalized(), 1.0));
			timePoints.add(new Point(globalCount++, u.getTimesAvarageOkNormalized(), 1.0));
			completedPoints.add(new Point(globalCount++, u.getUncompleteNormalized(), 1.0));
		}
		
		/* Execute FCM Algorithm: actions variable */
		FuzzyCMeansAlgorithm cMeans = new FuzzyCMeansAlgorithm(3, 2.0, actionsPoints, 0.01, null);
		MembershipFunctionPieceWiseLinear[] actionsMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		//MembershipFunctionPieceWiseLinear[] actionsMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		//time
		cMeans = new FuzzyCMeansAlgorithm(3, 2.0, timePoints, 0.01, null);
		MembershipFunctionPieceWiseLinear[] timeMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		//completed
		cMeans = new FuzzyCMeansAlgorithm(3, 2.0, completedPoints, 0.01, null);
		MembershipFunctionPieceWiseLinear[] completedMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		
		/* Load from 'FCL' file */
		FIS fis = FIS.load(fclPath, true);
		
		/* Update membership functions */
		fis.getFunctionBlock("fuzzyCMeans").getVariable("actions").add("Low", actionsMembershipFunctions[0]);
		fis.getFunctionBlock("fuzzyCMeans").getVariable("actions").add("Moderate", actionsMembershipFunctions[1]);
		fis.getFunctionBlock("fuzzyCMeans").getVariable("actions").add("High", actionsMembershipFunctions[2]);
		
		fis.getFunctionBlock("fuzzyCMeans").getVariable("times").add("Low", timeMembershipFunctions[0]);
		fis.getFunctionBlock("fuzzyCMeans").getVariable("times").add("Moderate", timeMembershipFunctions[1]);
		fis.getFunctionBlock("fuzzyCMeans").getVariable("times").add("High", timeMembershipFunctions[2]);
		
		fis.getFunctionBlock("fuzzyCMeans").getVariable("completeness").add("Low", completedMembershipFunctions[0]);
		fis.getFunctionBlock("fuzzyCMeans").getVariable("completeness").add("Moderate", completedMembershipFunctions[1]);
		fis.getFunctionBlock("fuzzyCMeans").getVariable("completeness").add("High", completedMembershipFunctions[2]);
		
		return fis;
	}
	
	/* FUZZY CMEANS 2 INPUTS - RESULTADOS DAS SESSÕES */
	
	public static void executeFuzzySystemWithFCMSessionEfcEft(List<SessionResultDataMining> sessions, boolean ignoreZero, boolean debug) {
		FIS fis = createMembershipFunctionsWithFCMSessionEfcEft(sessions, ignoreZero, FCL_EftEfc_CMEANS);
		
		for(SessionResultDataMining s : sessions){
			if(ignoreZero){
				if(!s.isClassificationSuccess()){
					s.setFuzzyPriorityEfcEft(0d);
					continue;
				}
			}
			
			/* Set inputs [Importance x Coupling x Complexity] of the class */
			fis.setVariable("efficiency", (1-s.getEfficiencyNormalized()));
			fis.setVariable("effectiveness", (1-s.getEffectivenessNormalized()));
			
			/* Evaluate */
	        fis.evaluate();
	        double defuzzifiedValue = fis.getVariable("priority").getLatestDefuzzifiedValue();
	        s.setFuzzyPriorityEfcEft(defuzzifiedValue);
	        
	        System.out.println(s.getUsername() + " -> (" + s.getEfficiencyNormalized() + ", " + s.getEffectivenessNormalized() + ") = " + s.getFuzzyPriorityEfcEft());
		}
		
		/* Just for debug: Show chart*/ 
		if(debug){
			 JFuzzyChart.get().chart(fis);
		}
	}
	
	public static FIS createMembershipFunctionsWithFCMSessionEfcEft(List<SessionResultDataMining> sessions, boolean ignoreZero, String fclPath){
		//Fuzzy CMeans
		ArrayList<Point> efficiencyPoints = new ArrayList<Point>(), 
				effectivenessPoints = new ArrayList<Point>();
		
		long globalCount = 1l;
		for(SessionResultDataMining s : sessions){
			if(ignoreZero){
				if(!s.isClassificationSuccess()){
					continue;
				}
			}
			efficiencyPoints.add(new Point(globalCount++, (1-s.getEfficiencyNormalized()), 1.0));
			effectivenessPoints.add(new Point(globalCount++, (1-s.getEffectivenessNormalized()), 1.0));
		}
		
		/* Execute FCM Algorithm: actions variable */
		FuzzyCMeansAlgorithm cMeans = new FuzzyCMeansAlgorithm(3, 2.0, efficiencyPoints, 0.01, null);
		MembershipFunctionPieceWiseLinear[] efficiencyMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		//MembershipFunctionPieceWiseLinear[] actionsMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		//time
		cMeans = new FuzzyCMeansAlgorithm(3, 2.0, effectivenessPoints, 0.01, null);
		MembershipFunctionPieceWiseLinear[] effectivenessMembershipFunctionsMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		
		/* Load from 'FCL' file */
		FIS fis = FIS.load(fclPath, true);
		
		/* Update membership functions */
		fis.getFunctionBlock("fuzzyCMeansEftEfc").getVariable("efficiency").add("Low", efficiencyMembershipFunctions[0]);
		fis.getFunctionBlock("fuzzyCMeansEftEfc").getVariable("efficiency").add("Moderate", efficiencyMembershipFunctions[1]);
		fis.getFunctionBlock("fuzzyCMeansEftEfc").getVariable("efficiency").add("High", efficiencyMembershipFunctions[2]);
		
		fis.getFunctionBlock("fuzzyCMeansEftEfc").getVariable("effectiveness").add("Low", effectivenessMembershipFunctionsMembershipFunctions[0]);
		fis.getFunctionBlock("fuzzyCMeansEftEfc").getVariable("effectiveness").add("Moderate", effectivenessMembershipFunctionsMembershipFunctions[1]);
		fis.getFunctionBlock("fuzzyCMeansEftEfc").getVariable("effectiveness").add("High", effectivenessMembershipFunctionsMembershipFunctions[2]);
		
		return fis;
	}
	
/* FUZZY CMEANS 2 INPUTS - RESULTADOS DOS USUARIOS */
	
	public static void executeFuzzySystemWithFCMUsersEfcEft(List<UserResultDataMining> users, boolean ignoreZero, boolean debug) {
		FIS fis = createMembershipFunctionsWithFCMUsersEfcEft(users, ignoreZero, FCL_EftEfc_CMEANS);
		
		for(UserResultDataMining u : users){
			if(ignoreZero){
				if(u.getEfficiencyNormalized() == 0 && u.getEffectivenessNormalized() == 0){
					u.setFuzzyPriorityEfcEft(0d);
					continue;
				}
			}
			
			/* Set inputs [Importance x Coupling x Complexity] of the class */
			fis.setVariable("efficiency", (1-u.getEfficiencyNormalized()));
			fis.setVariable("effectiveness", (1-u.getEffectivenessNormalized()));
			
			/* Evaluate */
	        fis.evaluate();
	        double defuzzifiedValue = fis.getVariable("priority").getLatestDefuzzifiedValue();
	        u.setFuzzyPriorityEfcEft(defuzzifiedValue);
	        
	        System.out.println(u.getUsername() + " -> (" + u.getEfficiencyNormalized() + ", " + u.getEffectivenessNormalized() + ") = " + u.getFuzzyPriorityEfcEft());
		}
		
		/* Just for debug: Show chart*/ 
		if(debug){
			 JFuzzyChart.get().chart(fis);
		}
	}
	
	public static FIS createMembershipFunctionsWithFCMUsersEfcEft(List<UserResultDataMining> users, boolean ignoreZero, String fclPath){
		//Fuzzy CMeans
		ArrayList<Point> efficiencyPoints = new ArrayList<Point>(), 
				effectivenessPoints = new ArrayList<Point>();
		
		long globalCount = 1l;
		for(UserResultDataMining u : users){
			if(ignoreZero){
				if(u.getEfficiencyNormalized() == null && u.getEffectivenessNormalized() == null){
					System.out.println(u.getUsername()+"IGNORE");
					continue;
				}
			}

			efficiencyPoints.add(new Point(globalCount++, (1-u.getEfficiencyNormalized()), 1.0));
			effectivenessPoints.add(new Point(globalCount++, (1-u.getEffectivenessNormalized()), 1.0));
		}
		
		/* Execute FCM Algorithm: actions variable */
		FuzzyCMeansAlgorithm cMeans = new FuzzyCMeansAlgorithm(3, 2.0, efficiencyPoints, 0.01, null);
		MembershipFunctionPieceWiseLinear[] efficiencyMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		//MembershipFunctionPieceWiseLinear[] actionsMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		//time
		cMeans = new FuzzyCMeansAlgorithm(3, 2.0, effectivenessPoints, 0.01, null);
		MembershipFunctionPieceWiseLinear[] effectivenessMembershipFunctionsMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		
		/* Load from 'FCL' file */
		FIS fis = FIS.load(fclPath, true);
		
		/* Update membership functions */
		fis.getFunctionBlock("fuzzyCMeansEftEfc").getVariable("efficiency").add("Low", efficiencyMembershipFunctions[0]);
		fis.getFunctionBlock("fuzzyCMeansEftEfc").getVariable("efficiency").add("Moderate", efficiencyMembershipFunctions[1]);
		fis.getFunctionBlock("fuzzyCMeansEftEfc").getVariable("efficiency").add("High", efficiencyMembershipFunctions[2]);
		
		fis.getFunctionBlock("fuzzyCMeansEftEfc").getVariable("effectiveness").add("Low", effectivenessMembershipFunctionsMembershipFunctions[0]);
		fis.getFunctionBlock("fuzzyCMeansEftEfc").getVariable("effectiveness").add("Moderate", effectivenessMembershipFunctionsMembershipFunctions[1]);
		fis.getFunctionBlock("fuzzyCMeansEftEfc").getVariable("effectiveness").add("High", effectivenessMembershipFunctionsMembershipFunctions[2]);
		
		return fis;
	}
	
	/* FUZZY CMEANS - RESULTADOS DAS TAREFAS */
	
	public static void executeFuzzySystemWithFCMTasks(List<TaskDataMining> tasks, Date init, Date end, boolean debug) {
		FIS fis = createMembershipFunctionsWithFCMTasks(tasks, init, end, FCL_EftEfcSes_CMEANS);
		
		for(TaskDataMining t : tasks){
			EvaluationTaskDataMining e = t.getEvaluationBetweenDates(init, end);
			if(e.getEvalEffectivenessNormalized() == null || e.getEvalEfficiencyNormalized() == null || e.getEvalCountSessionsNormalized() == null){
				e.setEvalFuzzyPriority(0d);
				continue;
			}
			
			/* Set inputs [Importance x Coupling x Complexity] of the class */
			fis.setVariable("efficiency", (1-e.getEvalEfficiencyNormalized()));
			fis.setVariable("effectiveness", (1-e.getEvalEffectivenessNormalized()));
			fis.setVariable("sessions", e.getEvalCountSessionsNormalized());
			
			/* Evaluate */
	        fis.evaluate();
	        double defuzzifiedValue = fis.getVariable("priority").getLatestDefuzzifiedValue();
	        e.setEvalFuzzyPriority(defuzzifiedValue);
	        
	        System.out.println(t.getTitle() + " -> (" + e.getEvalEfficiency() + ", " + e.getEvalEffectiveness() + ", " + e.getEvalCountSessions() + ") = " + e.getEvalFuzzyPriority());
		}
		
		/* Just for debug: Show chart*/ 
		if(debug){
			 JFuzzyChart.get().chart(fis);
		}
	}
	
	public static FIS createMembershipFunctionsWithFCMTasks(List<TaskDataMining> tasks, Date init, Date end, String fclPath){
		//Fuzzy CMeans
		ArrayList<Point> efficiencyPoints = new ArrayList<Point>(), 
				effectivenessPoints = new ArrayList<Point>(), 
				sessionsPoints = new ArrayList<Point>();
		
		long globalCount = 1l;
		for(TaskDataMining t : tasks){
			EvaluationTaskDataMining e = t.getEvaluationBetweenDates(init, end);
			if(e.getEvalEffectivenessNormalized() == null || e.getEvalEfficiencyNormalized() == null || e.getEvalCountSessionsNormalized() == null){
				continue;
			}
			
			efficiencyPoints.add(new Point(globalCount++, (1-e.getEvalEfficiencyNormalized()), 1.0));
			effectivenessPoints.add(new Point(globalCount++, (1-e.getEvalEffectivenessNormalized()), 1.0));
			sessionsPoints.add(new Point(globalCount++, e.getEvalCountSessionsNormalized(), 1.0));
		}
		
		/* Execute FCM Algorithm: actions variable */
		FuzzyCMeansAlgorithm cMeans = new FuzzyCMeansAlgorithm(3, 2.0, efficiencyPoints, 0.01, null);
		MembershipFunctionPieceWiseLinear[] efficiencyMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		//time
		cMeans = new FuzzyCMeansAlgorithm(3, 2.0, effectivenessPoints, 0.01, null);
		MembershipFunctionPieceWiseLinear[] effectivenessMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		//completed
		cMeans = new FuzzyCMeansAlgorithm(3, 2.0, sessionsPoints, 0.01, null);
		MembershipFunctionPieceWiseLinear[] sessionsMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		
		/* Load from 'FCL' file */
		FIS fis = FIS.load(fclPath, true);
		
		/* Update membership functions */
		fis.getFunctionBlock("fuzzyCMeansTasks").getVariable("efficiency").add("Low", efficiencyMembershipFunctions[0]);
		fis.getFunctionBlock("fuzzyCMeansTasks").getVariable("efficiency").add("Moderate", efficiencyMembershipFunctions[1]);
		fis.getFunctionBlock("fuzzyCMeansTasks").getVariable("efficiency").add("High", efficiencyMembershipFunctions[2]);
		
		fis.getFunctionBlock("fuzzyCMeansTasks").getVariable("effectiveness").add("Low", effectivenessMembershipFunctions[0]);
		fis.getFunctionBlock("fuzzyCMeansTasks").getVariable("effectiveness").add("Moderate", effectivenessMembershipFunctions[1]);
		fis.getFunctionBlock("fuzzyCMeansTasks").getVariable("effectiveness").add("High", effectivenessMembershipFunctions[2]);
		
		fis.getFunctionBlock("fuzzyCMeansTasks").getVariable("sessions").add("Low", sessionsMembershipFunctions[0]);
		fis.getFunctionBlock("fuzzyCMeansTasks").getVariable("sessions").add("Moderate", sessionsMembershipFunctions[1]);
		fis.getFunctionBlock("fuzzyCMeansTasks").getVariable("sessions").add("High", sessionsMembershipFunctions[2]);
		
		return fis;
	}
	
	/**
	 * Create Membership functions using the result of Fuzzy C-Means algorithm.
	 * @param points list of points
	 * @param centroids list of centroids
	 * @return three piece wise linear membership functions (low, moderate, high)
	 */
	public static MembershipFunctionPieceWiseLinear[] createMembershipFunctions(List<Point> points, List<double[]> centroids){
		/* Initializing variables: low term */
		Value[] x1 = new Value[points.size()], y1 = new Value[points.size()];
		ArrayList<MembershipFunctionPoint> x1y1 = new ArrayList<Fuzzy.MembershipFunctionPoint>();
		
		/* Initializing variables: moderate term */
		Value[] x2 = new Value[points.size()], y2 = new Value[points.size()];
		ArrayList<MembershipFunctionPoint> x2y2 = new ArrayList<Fuzzy.MembershipFunctionPoint>();
		
		/* Initializing variables: high term */
		Value[] x3 = new Value[points.size()], y3 = new Value[points.size()];
		ArrayList<MembershipFunctionPoint> x3y3 = new ArrayList<Fuzzy.MembershipFunctionPoint>();
		
		/* Identifying the position of the centroids in relation to linguistic terms */
		int size = centroids.size();
		double[] centroidsX = new double[size];
		for(int i = 0; i < size; i++){
			centroidsX[i] = centroids.get(i)[0];
		}
		
		/* Creating a copy of original array */
		double[] centroidsXCopy = Arrays.copyOf(centroidsX, size);
		Arrays.sort(centroidsXCopy);
		
		/* List with correct positions */
		int[] positions = new int[size];
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				if(centroidsXCopy[i] == centroidsX[j]){
					positions[i] = j;
					break;
				}
			}
		}
		
		/* Order points by X */
		Collections.sort(points, new Comparator<Point>() {
			@Override
			public int compare(Point p1, Point p2) {
				return Double.valueOf(p1.getX()).compareTo(Double.valueOf(p2.getX()));
			}
		});
		
		/* For each point, calculate the relevance to each cluster */
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		
			for(int i = 0; i < points.size(); i++){
				try{
					double lowRelevance = Double.valueOf(decimalFormat.format(points.get(i).getRelevance()[positions[0]]).replace(",", "."));
					double moderateRelevance = Double.valueOf(decimalFormat.format(points.get(i).getRelevance()[positions[1]]).replace(",", "."));
					double highRelavance = Double.valueOf(decimalFormat.format(points.get(i).getRelevance()[positions[2]]).replace(",", "."));
					
					x1y1.add(new Fuzzy().new MembershipFunctionPoint(new Value(points.get(i).getX()), new Value(lowRelevance)));
					x2y2.add(new Fuzzy().new MembershipFunctionPoint(new Value(points.get(i).getX()), new Value(moderateRelevance)));
					x3y3.add(new Fuzzy().new MembershipFunctionPoint(new Value(points.get(i).getX()), new Value(highRelavance)));
				}catch(Exception e){
					System.out.println(points.get(i).getRelevance().toString());
					e.printStackTrace();
				}
			}
		
		
		for(int i = 0; i < points.size(); i++){
			x1[i] = x1y1.get(i).getX(); y1[i] = x1y1.get(i).getY();
			x2[i] = x2y2.get(i).getX(); y2[i] = x2y2.get(i).getY();
			x3[i] = x3y3.get(i).getX(); y3[i] = x3y3.get(i).getY();
		}
		
		/* Membership Function Piece Wise Linear */
		MembershipFunctionPieceWiseLinear low = new MembershipFunctionPieceWiseLinear(x1, y1);
		MembershipFunctionPieceWiseLinear moderate = new MembershipFunctionPieceWiseLinear(x2, y2);
		MembershipFunctionPieceWiseLinear high = new MembershipFunctionPieceWiseLinear(x3, y3);
		
		return new MembershipFunctionPieceWiseLinear[]{low, moderate, high};
	}
	
	/**
	 * MembershipFunctionPoint
	 * @author Pedro Almir
	 */
	class MembershipFunctionPoint{
		private Value x;
		private Value y;
		
		/**
		 * @param x
		 * @param y
		 */
		public MembershipFunctionPoint(Value x, Value y) {
			super();
			this.x = x;
			this.y = y;
		}
		/**
		 * @return the x
		 */
		public Value getX() {
			return x;
		}
		/**
		 * @param x the x to set
		 */
		public void setX(Value x) {
			this.x = x;
		}
		/**
		 * @return the y
		 */
		public Value getY() {
			return y;
		}
		/**
		 * @param y the y to set
		 */
		public void setY(Value y) {
			this.y = y;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "MembershipFunctionPoint [x=" + x.getValReal() + ", y=" + y.getValReal() + "]";
		}
	}
}
