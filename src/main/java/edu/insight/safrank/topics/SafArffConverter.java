package edu.insight.safrank.topics;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.insight.safrank.utils.BasicFileTools;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class SafArffConverter {

	private double[] vals;
	private ArrayList<String> tOrfVals;	  
	private ArrayList<Attribute> atts = new ArrayList<Attribute>();
	private Attribute targetSafValueAttribute;
	private String instancesName = "SafRankInstances";
	private int startYear = 2000;
	private int endYear = 2014;
	public Instances overallTrainingData;

	//	private static String doubleSpacesPatternString = "\\s\\s+"; 
	//	private static String weirdCharacterPatternString = "œ|æ|ß|ð|ø|å|ł|þ|�|ï|¿|½|â|€|“|”|™|˜";
	//	private static String weirdPatternString = "\\W\\W\\w+;";
	//	private static String slashNs = "\n";
	//	private static String singleCharAtLineStarts = "\\b\\w\\s+(.*)";
	//	private static String unnecessaryQuote = "\\s+”\\s+";

	public SafArffConverter() {
		tOrfVals = new ArrayList<String>();
		tOrfVals.add("f"); tOrfVals.add("t");
		instancesName = instancesName + ": -C " +  "1"; //WEKA based naming of instances, label class always first
	}

	public Instances getInstancesCollectiveList(List<Map<String, SafTopicData>> listSafData, int targetYearIndex, int pastYears) {
		Instances data = new Instances(instancesName, atts, 0);
		Set<String> inputTopics = new HashSet<String>();
		int startIndex = targetYearIndex-pastYears;
		for(int i=startIndex; i<targetYearIndex; i++){
			Map<String, SafTopicData> yearSafData = listSafData.get(i);
			for(String inputTopic : 	yearSafData.keySet()){
				inputTopics.add(inputTopic);
			}
		}
		for(String topic : inputTopics){
			vals = new double[data.numAttributes()];
			int count = 0;
			SafTopicData targetSafTopicData = listSafData.get(targetYearIndex).get(topic);
			double targetSafVal = 0.5;
			if(targetSafTopicData != null)
				targetSafVal = targetSafTopicData.safVal;
			vals[count++] = targetSafVal;
			for(int i=startIndex; i<targetYearIndex; i++){
				Map<String, SafTopicData> nLastYearSafData = listSafData.get(i);
				SafTopicData nLastYearSafTopicData = nLastYearSafData.get(topic);
				double nLastYearSafVal = 0.5;
				double nLastYearSafRank = 12500;
				if(nLastYearSafTopicData != null){
					nLastYearSafVal = nLastYearSafTopicData.safVal;
					nLastYearSafRank = nLastYearSafTopicData.rankVal;
				}
				vals[count++] = nLastYearSafVal;
				vals[count++] = nLastYearSafRank;
			}		
			vals[count++] = data.attribute("topic").addStringValue(topic);
			Instance instance = new DenseInstance(1.0, vals);				
			data.add(instance);
		}	
		return data;
	}


	public Instances getInstancesCollectiveListRelTopic(List<Map<String, SafTopicData>> listSafData, int targetYearIndex, int pastYears) {
		Instances data = new Instances(instancesName, atts, 0);
		Set<String> inputTopics = new HashSet<String>();
		int startIndex = targetYearIndex-pastYears;
		for(int i=startIndex; i<targetYearIndex; i++){
			Map<String, SafTopicData> yearSafData = listSafData.get(i);
			for(String inputTopic : 	yearSafData.keySet()){
				inputTopics.add(inputTopic);
			}
		}
		for(String topic : inputTopics){
			vals = new double[data.numAttributes()];
			int count = 0;
			SafTopicData targetSafTopicData = listSafData.get(targetYearIndex).get(topic);
			double targetSafVal = 0.5;
			if(targetSafTopicData != null)
				targetSafVal = targetSafTopicData.safVal;
			vals[count++] = targetSafVal;
			for(int i=startIndex; i<targetYearIndex; i++){
				Map<String, SafTopicData> nLastYearSafData = listSafData.get(i);
				SafTopicData nLastYearSafTopicData = nLastYearSafData.get(topic);
				double nLastYearSafVal = 0.5;
				double nLastYearSafRank = 12500;
				if(nLastYearSafTopicData != null){
					nLastYearSafVal = nLastYearSafTopicData.safVal;
					nLastYearSafRank = nLastYearSafTopicData.rankVal;
				}
				vals[count++] = nLastYearSafVal;
				vals[count++] = nLastYearSafRank;
			}		
			vals[count++] = data.attribute("topic").addStringValue(topic);
			Instance instance = new DenseInstance(1.0, vals);				
			data.add(instance);
		}	
		return data;
	}


	public void setAttributes(int pastYears) {
		targetSafValueAttribute = new Attribute("targetSafVal");
		atts.add(targetSafValueAttribute);
		for(int i=0; i<pastYears; i++){
			Attribute pastYearsSafValueAttribute = new Attribute(pastYears - (i) + "lastYearSafVal");
			Attribute pastYearSafRankAttribute = new Attribute(pastYears - (i) + "lastYearSafRank");
			atts.add(pastYearsSafValueAttribute);
			atts.add(pastYearSafRankAttribute);
		}
		Attribute topicAttribute = new Attribute("topic", (ArrayList<String>) null);	
		atts.add(topicAttribute);
	}

	public Instances createInstances(List<Map<String, SafTopicData>> listSafData, int targetYear, int pastYears, String arffFileName) {
		Instances totalInstances = null;
		totalInstances = getInstancesCollectiveList(listSafData, getYearIndex(targetYear), pastYears);
		System.out.println("No.OfInstances: " + totalInstances.size());
		ArffSaver saver = new ArffSaver();		
		try {
			saver.setInstances(totalInstances);
			saver.setFile(new File(arffFileName));		
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return totalInstances;
	}

	public Map<String, Double> output(List<Map<String, SafTopicData>> listSafData, int targetYear, int pastYears) {
		Map<String, Double> targetTopicSafValMap = new HashMap<String, Double>();
		int targetYearIndex = getYearIndex(targetYear);
		Set<String> inputTopics = new HashSet<String>();
		int startIndex = targetYearIndex-pastYears;
		for(int i=startIndex; i<targetYearIndex; i++){
			Map<String, SafTopicData> yearSafData = listSafData.get(i);
			for(String inputTopic : 	yearSafData.keySet()){
				inputTopics.add(inputTopic);
			}
		}

		double[] lrMultiples = new double[]{0.2077, 0.0, 0.2929, 0.0, 0.435, 0.0001};
		double constant = -0.8203;
		for(String topic : inputTopics){
			double targetSafValue = 0.0;
			int count = 0;
			for(int i=startIndex; i<targetYearIndex; i++){
				Map<String, SafTopicData> nLastYearSafData = listSafData.get(i);
				SafTopicData nLastYearSafTopicData = nLastYearSafData.get(topic);
				double nLastYearSafVal = 0.5;
				double nLastYearSafRank = 12500;
				if(nLastYearSafTopicData != null){
					nLastYearSafVal = nLastYearSafTopicData.safVal;
					nLastYearSafRank = nLastYearSafTopicData.rankVal;
				}
				targetSafValue = targetSafValue + lrMultiples[count++] * nLastYearSafVal;
				targetSafValue = targetSafValue + lrMultiples[count++] * nLastYearSafRank;
			}
			targetSafValue = targetSafValue + constant;
			targetTopicSafValMap.put(topic, targetSafValue);
		}
		return targetTopicSafValMap;
	}

	public Map<String, SafTopicData> readData(File file){
		Map<String, SafTopicData> safData = new HashMap<String, SafTopicData>();
		BufferedReader br = BasicFileTools.getBufferedReader(file);
		try {
			String line = br.readLine();
			String[] values = line.split(",");
			int rank = 1;
			while ( values != null ){				
				SafTopicData topicData = new SafTopicData();
				int count = 0;
				String topic = values[count].trim();
				//System.out.println(topic);
				count++;
				topicData.topic = topic;				
				String[] split = values[count].trim().split(";");
				double safVal = Double.parseDouble(split[0].trim());
				boolean authorsStarted = false;
				topicData.safVal = safVal;
				if("Subordinate Clauses Category".equalsIgnoreCase(topic)){
					System.out.println("debug");
				}

				if(split.length==1){
					line = br.readLine();
					values = line.split(",");	
					rank++;
					continue;
				}				
				String relatedTopic = split[1].trim();
				if("".equals(relatedTopic)){
					authorsStarted = true;
				} else {
					count++;
					double relatedTopicScore = Double.parseDouble(values[count].trim());
					topicData.relatedTopics.put(relatedTopic, relatedTopicScore);
					count++;
					while(true) {
						relatedTopic = values[count];
						if(!values[count+1].contains(";")){
							relatedTopicScore = Double.parseDouble(values[count+1]);
							count++;
						} else {
							authorsStarted = true;
							count++;
							break;
						}
						topicData.relatedTopics.put(relatedTopic, relatedTopicScore);
						count++;
					}		
				}
				if(authorsStarted){
					if("".equals(relatedTopic)){ 
						String relatedAuthor = values[count].split(";")[2].trim();
						count++;
						Double relatedAuthorScore = Double.parseDouble(values[count]);
						topicData.relatedAuthors.put(relatedAuthor, relatedAuthorScore);
						count++;						
					} else {
						split = values[count].split(";");
						if(split.length==1){
							line = br.readLine();
							values = line.split(",");		
							continue;
						}								
						String relatedAuthor = values[count].split(";")[1].trim();
						count++;
						Double relatedAuthorScore = Double.parseDouble(values[count]);
						topicData.relatedAuthors.put(relatedAuthor, relatedAuthorScore);
						count++;
					}
					while(true) {
						if(count>=values.length){
							break;
						}
						String relatedAuthor = values[count];
						double relatedAuthorScore = -1.0;
						if(!values[count+1].contains(";")){
							relatedAuthorScore = Double.parseDouble(values[count+1]);
							count++;
						} else {							
							break;
						}
						topicData.relatedAuthors.put(relatedAuthor, relatedAuthorScore);
						count++;
					}
				}			
				line = br.readLine();
				if(line!=null){
					values = line.split(",");	
				} else {
					values = null;
				}
				topicData.rankVal = rank++;
				safData.put(topicData.topic, topicData);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return safData;
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


	public int getYearIndex(int year){
		return (year - startYear) / 2;
	}

	public static void main(String[] args) {
		SafArffConverter arffConverter = new SafArffConverter();
		String topicsDir = "src/main/resources/LREC-topics-relatedTopics-relatedResearchers";
		List<Map<String, SafTopicData>> listSafData = new ArrayList<Map<String, SafTopicData>>();
		File dir = new File(topicsDir);
		for(int i=0; i<=arffConverter.getYearIndex(arffConverter.endYear); i++){
			listSafData.add(null);
		}
		for(File file : dir.listFiles()){
			if(file.isHidden()){
				continue;
			}
			String fileName = file.getName();
			int lrecStartIndex = fileName.indexOf("lrec_") ;
			int year = Integer.parseInt(file.getName().substring(lrecStartIndex + 5, lrecStartIndex + 5 + 4));
			int yearIndex = arffConverter.getYearIndex(year);
			listSafData.set(yearIndex, arffConverter.readData(file));					
			System.out.println(file.getName());
		}
		
		int pastYears = 3;
		int targetYear = 2006;
		int testYear = 2014;
		arffConverter.setAttributes(pastYears);
		arffConverter.overallTrainingData = new Instances(arffConverter.instancesName, arffConverter.atts, 0);
		for(int year=targetYear; year<=arffConverter.endYear; year=year+2){
			String arffFileNameNonFilt = "src/main/resources/arff/SafTarget" + year + ".arff";
			System.out.println(year);
			Instances instances = arffConverter.createInstances(listSafData, year, pastYears, arffFileNameNonFilt);
			if(year!=testYear){
				arffConverter.overallTrainingData.addAll(instances);
			} else {
				System.out.println("test year");
			}
		}
		System.out.println("No. of total training instances: " + arffConverter.overallTrainingData.size());
		ArffSaver saver = new ArffSaver();		
		String totalTrainingArffFileNameNonFilt = "src/main/resources/arff/SafTargetTotal" + ".arff";
		try {
			saver.setInstances(arffConverter.overallTrainingData);
			saver.setFile(new File(totalTrainingArffFileNameNonFilt));		
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}	

		//		int outputYear = 2014;
		//		int previousYears = 3;
		//		Map<String, Double> output = arffConverter.output(listSafData, outputYear, previousYears);
		//		System.out.println("done");
		//		Map<String, Double> sortedMap = sortByComparator(output);
		//
		//		StringBuilder results = new StringBuilder();
		//		int rank = 1;
		//		for(Object topic : sortedMap.keySet()){
		//			String topicc = (String) topic;
		//			topicc = topicc.replaceAll(",", " ").trim();
		//			results.append(topicc + "," + sortedMap.get(topic) + "," + rank++ + "\n");
		//		}
		//		BasicFileTools.writeFile("src/main/resources/arff/results2014.csv", results.toString().trim());
	}

}
