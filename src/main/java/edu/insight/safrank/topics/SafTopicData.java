package edu.insight.safrank.topics;

import java.util.HashMap;
import java.util.Map;

public class SafTopicData {
	
	public String topic = null;
	public Map<String, Double> relatedTopics = new HashMap<String, Double>();
	public Map<String, Double> relatedAuthors = new HashMap<String, Double>();
	public double safVal = -1.0;
	public double rankVal = -1.0;
	
}
