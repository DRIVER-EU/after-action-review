package eu.driver.aar.service.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class QuestionInstance {
	private Logger log = Logger.getLogger(this.getClass());
	private static QuestionInstance aMe = null;
	
	private Map<String, String> questionMap = new ConcurrentHashMap<String, String>();
	
	
	private QuestionInstance() {
		try {
			JSONArray questionArray = new JSONArray(readLineByLineJava8("config/questions.json"));
			for (int i = 0; i < questionArray.length(); i++) {
	            JSONObject jsonObj = questionArray.getJSONObject(i);
	            String questionString = jsonObj.getString("title");
	            int idx = questionString.indexOf(":");
				if (idx > 0) {
					String marker = questionString.substring(0, idx);
					StringTokenizer tokens = new StringTokenizer(marker, "/");
					if (tokens.countTokens() == 4) {
						log.info("Adding: " + marker + ", " + questionString);
						questionMap.put(marker, questionString);
					}
				}
	        }
		} catch (Exception e) {
			
		}
		
	}
	
	public static synchronized QuestionInstance getInstance() {
		if (QuestionInstance.aMe == null) {
			QuestionInstance.aMe = new QuestionInstance();
		}
		return QuestionInstance.aMe;
	}
	
	public String getQuestion(String indent) {
		return questionMap.get(indent);
	}
	
	private String readLineByLineJava8(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
 
        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8)) 
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
 
        return contentBuilder.toString();
    }
}
