
package edu.insight.safrank.topics;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import edu.insight.finlaw.utils.BasicFileTools;
import edu.insight.safrank.curvefitting.GaussianCurveFitterTest;

public class TopicwiseCurveFitter {

	private static int startYear = 2000;
	private static int targetYear = 2014;
	private static int yearDiff = 2;

	public static void linearCurveFitter() {
		SafDataReader sReader = new SafDataReader();
		Map<String, Map<Integer, SafTopicData>> topicYearSafDataMap = sReader.getTopicYearSafDataMap(targetYear);
		Map<String, Double> topicPredicted = new HashMap<String, Double>();
		for(String topic : topicYearSafDataMap.keySet()){
			Map<Integer, SafTopicData> map = topicYearSafDataMap.get(topic);
			SimpleRegression simpleRegression = new SimpleRegression(true);
			for(int sY=startYear; sY<targetYear; sY = sY + yearDiff){
				double x = sY;
				double y = 0.0;
				if(map.get(sY)!=null){
					SafTopicData safData = map.get(sY);
					System.out.println(topic + " " + sY + " " + safData.totalSafVal);
					y = safData.totalSafVal;
				}
				simpleRegression.addData(x, y);
			}
			double predictSafVal = simpleRegression.predict(2014);
			topicPredicted.put(topic, predictSafVal);
		}
		writeFile(topicPredicted);
	}

	public static void gaussianCurveFitter() {
		SafDataReader sReader = new SafDataReader();
		Map<String, Map<Integer, SafTopicData>> topicYearSafDataMap = sReader.getTopicYearSafDataMap(targetYear);
		Map<String, Double> topicPredicted = new HashMap<String, Double>();
		int counter = 0;
		for(String topic : topicYearSafDataMap.keySet()){
			System.out.println(counter+++"/"+topicYearSafDataMap.size());
			WeightedObservedPoints obs = new WeightedObservedPoints();
			Map<Integer, SafTopicData> map = topicYearSafDataMap.get(topic);
			for(int sY=startYear; sY<targetYear; sY = sY + yearDiff){
				double x = sY;
				double y = 0.0;
				if(map.get(sY)!=null){
					SafTopicData safData = map.get(sY);
				//	System.out.println(topic + " " + sY + " " + safData.safVal);
					y = safData.safVal;
				}
				obs.add(x, y);
			}
			Double predictSafVal = GaussianCurveFitterTest.getVal(obs, targetYear);
			topicPredicted.put(topic, predictSafVal);
		}
		writeFile(topicPredicted);
	}

	private static void writeFile(Map<String, Double> topicPredicted){
		Map<String, Double> sortedMap = sortByComparator(topicPredicted);
		StringBuilder results = new StringBuilder();
		int rank = 1;
		for(Object topic : sortedMap.keySet()){
			String topicc = (String) topic;
			topicc = topicc.replaceAll(",", " ").trim();
			results.append(topicc + "," + sortedMap.get(topic) + "," + rank++ + "\n");
		}
		BasicFileTools.writeFile("src/main/resources/arff/results2014.csv", results.toString().trim());

	}

	private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {
		// Convert Map to List
		List<Map.Entry<String, Double>> list = 
				new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1,
					Map.Entry<String, Double> o2) {
				int compareTo = o1.getValue().compareTo(o2.getValue());
				return (-1 * compareTo);
			}
		});

		// Convert sorted map back to a Map
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	public static void main(String[] args) {
		gaussianCurveFitter();
	}

}
