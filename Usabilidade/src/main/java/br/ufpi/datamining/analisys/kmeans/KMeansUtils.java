package br.ufpi.datamining.analisys.kmeans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import br.ufpi.datamining.models.auxiliar.SessionResultDataMining;
import br.ufpi.datamining.utils.StatisticsUtils;

public class KMeansUtils {

	public static List<PointKMeans> generatePoints(List<SessionResultDataMining> sessions) {
		List<PointKMeans> points = new ArrayList<PointKMeans>();
		for (SessionResultDataMining session : sessions) {
			points.add(new PointKMeans(session.getId(), session.getEffectiveness(), session.getEfficiency()));
		}
		return points;
	}
	
	public static double calcEclidianDistance (PointKMeans a, PointKMeans b) {
		return Math.sqrt(Math.pow(a.getX()-b.getX(), 2) + Math.pow(a.getY()-b.getY(), 2));
	}
	
	public static ResultKMeans kmeansFindBestUsers(List<PointKMeans> points, int numClusters, double thresholdClusters) throws Exception {
		int maxNumClusters = numClusters+15;
		double distanceClusters = thresholdClusters-1;
		PointKMeans bestPoint = new PointKMeans("_", 100d, 100d);
		
		Instances instances = KMeansUtils.generateInstances(points);
		KMeans kmeans = null;
		Integer bestCluster = null;
		
		while (numClusters < maxNumClusters && distanceClusters < thresholdClusters) {
			kmeans = new KMeans(10, numClusters, points);
	        kmeans.calculate(instances);
	        
	        //calc euclician distance
	        List<PointKMeans> centroids = kmeans.getCentroids();
	        for (PointKMeans c : centroids) {
	        	c.setEucDist(KMeansUtils.calcEclidianDistance(c, bestPoint));
	        }
	        
	        //sort desc
	        Collections.sort(centroids, new Comparator<PointKMeans>() {
			    public int compare(PointKMeans a, PointKMeans b) {
			        return b.getEucDist().compareTo(a.getEucDist());
			    }
			});
	        
	        if (centroids.size() >= 2) {
	        	PointKMeans closer = centroids.get(centroids.size()-1);
	        	PointKMeans closerButOne = centroids.get(centroids.size()-2);
	        	distanceClusters = closerButOne.getEucDist() - closer.getEucDist();
	        	bestCluster = closer.getCluster();
	        	
	        	System.out.println("##############");
	        	System.out.printf("Clusters: " + numClusters + " | Distance: " + closerButOne.getEucDist() +
	        			" - " + closer.getEucDist() + " = " + distanceClusters);
	        	System.out.println();
	        }
	        	
	        numClusters++;
		}
		
		List<PointKMeans> roundPoints = KMeansUtils.roundPoints(kmeans.getPoints(), 2);
		List<PointKMeans> roundCentroids = KMeansUtils.roundPoints(kmeans.getCentroids(), 2);
		
		List<ClusterKMeans> clusters = KMeansUtils.groupOnClusters(roundPoints, bestCluster);
		
		return new ResultKMeans(
				numClusters-1, 
				StatisticsUtils.round(distanceClusters, 2), 
				roundCentroids, 
				roundPoints,
				clusters
		);
	}
	
	public static Instances generateInstances(List<PointKMeans> points) throws Exception {
		
		FastVector atts = new FastVector(2);
		atts.addElement(new Attribute("X"));
        atts.addElement(new Attribute("Y"));
        //atts.addElement(new Attribute("content", (FastVector)null));
        //atts.addElement(new Attribute("@@class@@", fvNominalVal));
        
        Instances dataRaw = new Instances("KMeansInstances", atts, 0);

        for (PointKMeans p : points) {
        	addDataRaw(dataRaw, atts, new Object[]{p.getX(),p.getY()});
        }
        
        return dataRaw;
	}
	
	private static void addDataRaw(Instances dataRaw, FastVector atts, Object[] values) {
		Instance iExample = new Instance(atts.size());
        
        for (int i = 0; i < atts.size(); i++) {
        	if (atts.elementAt(i).toString().indexOf("numeric") != -1) {
        		iExample.setValue((Attribute)atts.elementAt(i), (Double) values[i]);
        	} else if (atts.elementAt(i).toString().indexOf("string") != -1) {
        		iExample.setValue((Attribute)atts.elementAt(i), (String) values[i]);
        	}
        }

        dataRaw.add(iExample);
	}
	
	public static List<PointKMeans> convertSessionsToPoints(List<SessionResultDataMining> sessions) {
		List<PointKMeans> points = new ArrayList<PointKMeans>();
		for (SessionResultDataMining s : sessions) {
			points.add(new PointKMeans(s.getId(), s.getEffectiveness(), s.getEfficiency()));
		}
		return points;
	}
	
	public static List<PointKMeans> roundPoints(List<PointKMeans> points, int round) {
		List<PointKMeans> newPoints = new ArrayList<PointKMeans>();
		for (PointKMeans p : points) {
			PointKMeans newPoint = new PointKMeans(
					p.getId(), 
					StatisticsUtils.round(p.getX(), round),
					StatisticsUtils.round(p.getY(), round));
			newPoint.setEucDist(StatisticsUtils.round(p.getEucDist(), round));
			newPoint.setCluster(p.getCluster());
			newPoints.add(newPoint);
		}
		return newPoints;
	}
	
	public static List<ClusterKMeans> groupOnClusters(List<PointKMeans> points, Integer bestCluster) {
		Map<Integer, List<PointKMeans>> clustersMap = new HashMap<Integer, List<PointKMeans>>();
		for (PointKMeans p : points) {
			List<PointKMeans> list = null;
			if (clustersMap.get(p.getCluster()) != null) {
				list = clustersMap.get(p.getCluster());
				
			} else {
				list = new ArrayList<PointKMeans>();
			}
			list.add(p);
			clustersMap.put(p.getCluster(), list);
		}
		
		List<ClusterKMeans> clusters = new ArrayList<ClusterKMeans>();
		Set<Integer> keySet = clustersMap.keySet();
		for (Integer key : keySet) {
			boolean isBest = bestCluster.equals(key) ? true : false;
			clusters.add(new ClusterKMeans(key, clustersMap.get(key), isBest));
		}
		
		return clusters;
	}
	
	
	public static void main(String[] args) throws Exception {
		List<PointKMeans> points = new ArrayList<PointKMeans>();
		points.add(new PointKMeans("1", 1, 1));
		points.add(new PointKMeans("2", 0, 0.1));
		points.add(new PointKMeans("3", 0.1, 0.15));
		points.add(new PointKMeans("4", 50.2, 0.15));
		points.add(new PointKMeans("5", 90, 90.55));
		points.add(new PointKMeans("6", 95, 98.15));
		
		ResultKMeans resultKMeans = KMeansUtils.kmeansFindBestUsers(points, 2, 10d);
		
		System.out.println("\n######################");
		System.out.println("CLUSTERS AND DISTANCE:");
		System.out.println(resultKMeans.getNumClusters());
		System.out.println(resultKMeans.getDistanceThreshold());
		System.out.println("POINTS:");
		for (PointKMeans p : resultKMeans.getPoints()) {
			System.out.println(p);
		}
		System.out.println("CENTROIDS:");
		for (PointKMeans c : resultKMeans.getCentroids()) {
			System.out.println(c);
		}
    }
}
