package br.ufpi.datamining.analisys;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import br.ufpi.datamining.analisys.fuzzy.FuzzyCMeansAlgorithm;
import br.ufpi.datamining.analisys.fuzzy.Point;
import br.ufpi.datamining.models.aux.UserResultDataMining;
import br.ufpi.util.ApplicationPath;
import net.sourceforge.jFuzzyLogic.FIS;
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
//			fis.setVariable("actions", u.getActionsAvarageNormalized());
//			fis.setVariable("times", u.getActionsAvarageNormalized());
			
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
//			actionsPoints.add(new Point(globalCount++, u.getActionsAvarageNormalized(), 1.0));
//			timePoints.add(new Point(globalCount++, u.getTimesAvarageNormalized(), 1.0));
			completedPoints.add(new Point(globalCount++, u.getUncompleteNormalized(), 1.0));
		}
		
		/* Execute FCM Algorithm: actions variable */
		FuzzyCMeansAlgorithm cMeans = new FuzzyCMeansAlgorithm(3, 2.0, actionsPoints, 0.01, null);
		MembershipFunctionPieceWiseLinear[] actionsMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		//time
		cMeans = new FuzzyCMeansAlgorithm(3, 2.0, timePoints, 0.001, null);
		MembershipFunctionPieceWiseLinear[] timeMembershipFunctions = createMembershipFunctions(cMeans.executeFCM(), cMeans.getCentroids());
		//completed
		cMeans = new FuzzyCMeansAlgorithm(3, 2.0, completedPoints, 0.001, null);
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
		
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		for(int i = 0; i < points.size(); i++){
			double lowRelevance = Double.valueOf(decimalFormat.format(points.get(i).getRelevance()[positions[0]]).replace(",", "."));
			double moderateRelevance = Double.valueOf(decimalFormat.format(points.get(i).getRelevance()[positions[1]]).replace(",", "."));
			double highRelavance = Double.valueOf(decimalFormat.format(points.get(i).getRelevance()[positions[2]]).replace(",", "."));
			
			x1y1.add(new Fuzzy().new MembershipFunctionPoint(new Value(points.get(i).getX()), new Value(lowRelevance)));
			x2y2.add(new Fuzzy().new MembershipFunctionPoint(new Value(points.get(i).getX()), new Value(moderateRelevance)));
			x3y3.add(new Fuzzy().new MembershipFunctionPoint(new Value(points.get(i).getX()), new Value(highRelavance)));
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
