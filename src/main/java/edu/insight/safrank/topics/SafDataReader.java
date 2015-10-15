package edu.insight.safrank.topics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.insight.finlaw.utils.BasicFileTools;

public class SafDataReader {

	private int startYear = 2000;
	private int endYear = 2014;

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

		int nRelTopics = 5;
		for(String topic: safData.keySet()){
			SafTopicData safTopicData = safData.get(topic);
			double safVal = safTopicData.safVal;
			safTopicData.totalSafVal = safVal;
			int counter = 0;
			for(String relTopic : safTopicData.relatedTopics.keySet()){
				Double relatedness = safTopicData.relatedTopics.get(relTopic);
				double relTopicSafScore = 0.0;
				if(safData.get(relTopic)!=null){
					SafTopicData safRelatedTopicData = safData.get(relTopic);	
					relTopicSafScore = safRelatedTopicData.safVal;
				}	
				safTopicData.totalSafVal = safTopicData.totalSafVal + relatedness * relTopicSafScore;
				counter++;
				if(counter>nRelTopics){
					break;
				}
			}
		}
		return safData;
	}

	//	private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {
	//		// Convert Map to List
	//		List<Map.Entry<String, Double>> list = 
	//				new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());
	//
	//		// Sort list with comparator, to compare the Map values
	//		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
	//			public int compare(Map.Entry<String, Double> o1,
	//					Map.Entry<String, Double> o2) {
	//				int compareTo = o1.getValue().compareTo(o2.getValue());
	//				return (-1 * compareTo);
	//			}
	//		});
	//
	//		// Convert sorted map back to a Map
	//		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
	//		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
	//			Map.Entry<String, Double> entry = it.next();
	//			sortedMap.put(entry.getKey(), entry.getValue());
	//		}
	//		return sortedMap;
	//	}


	public int getYearIndex(int year){
		return (year - startYear) / 2;
	}

	public Map<String, Map<Integer, SafTopicData>> getTopicYearSafDataMap(int targetYear) {
		SafDataReader arffConverter = new SafDataReader();
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

		//newly added below
		Map<String, Map<Integer, SafTopicData>> topicAllYearSafDataMap = new HashMap<String, Map<Integer, SafTopicData>>();
		for(int startYear = arffConverter.startYear; startYear<targetYear; startYear = startYear + 2){
			Map<String, SafTopicData> safDataMap = listSafData.get(arffConverter.getYearIndex(startYear));
			for(String topic : safDataMap.keySet()){
				SafTopicData safTopicData = safDataMap.get(topic);
				if(!topicAllYearSafDataMap.containsKey(topic)){
					topicAllYearSafDataMap.put(topic, new HashMap<Integer, SafTopicData>());
				}
				topicAllYearSafDataMap.get(topic).put(startYear, safTopicData);
			}
		}		
		//newly added above
		return topicAllYearSafDataMap;
	}

}
