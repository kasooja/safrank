package edu.insight.safrank.topics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import au.com.bytecode.opencsv.CSVReader;

public class TopicsCollector {

	public static void main(String[] args) {
		String topicsDir = "src/main/resources/lrec-topics";
		File dir = new File(topicsDir);
		for(File file : dir.listFiles()){
			if(file.isHidden()){
				continue;
			}
			try {
				//				CSVReader reader = new CSVReader(new InputStreamReader(
				//						new FileInputStream(file.getAbsolutePath()), "UTF-8"), 
				//						';', '\'', 1); // it is not clear what arguments means 
				CSVReader reader = new CSVReader(new InputStreamReader(
						new FileInputStream(file.getAbsolutePath()), "UTF-8")); // it is not clear what arguments means 
				try {
					String[] values = reader.readNext();
					while ( values != null ) {
						//System.out.println(Arrays.asList(values));
						String topic = values[1];
						String safScore = values[2];
						System.out.println(topic);
						System.out.println(safScore);
						values = reader.readNext();
					}
				} finally {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}

	}	

}
