package eu.driver.aar.service.objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartMapObjects {

	private Map<Integer, Map<String, Integer>> answerMapMap = new HashMap<Integer, Map<String, Integer>>();
	private Map<Integer, List<String>> commentsMap = new HashMap<Integer, List<String>>();
	private Map<Integer, String> questionMap = new HashMap<Integer, String>();
	private Map<String, Integer> obsCountMap = new HashMap<String, Integer>();

	public ChartMapObjects() {

	}

	public ChartMapObjects(Map<Integer, Map<String, Integer>> answerMapMap,
			Map<Integer, List<String>> commentsMap,
			Map<Integer, String> questionMap, Map<String, Integer> obsCountMap) {
		this.answerMapMap = answerMapMap;
		this.commentsMap = commentsMap;
		this.questionMap = questionMap;
		this.obsCountMap = obsCountMap;
	}

	public Map<Integer, Map<String, Integer>> getAnswerMapMap() {
		return answerMapMap;
	}

	public void setAnswerMapMap(Map<Integer, Map<String, Integer>> answerMapMap) {
		this.answerMapMap = answerMapMap;
	}

	public Map<Integer, List<String>> getCommentsMap() {
		return commentsMap;
	}

	public void setCommentsMap(Map<Integer, List<String>> commentsMap) {
		this.commentsMap = commentsMap;
	}

	public Map<Integer, String> getQuestionMap() {
		return questionMap;
	}

	public void setQuestionMap(Map<Integer, String> questionMap) {
		this.questionMap = questionMap;
	}

	public Map<String, Integer> getObsCountMap() {
		return obsCountMap;
	}

	public void setObsCountMap(Map<String, Integer> obsCountMap) {
		this.obsCountMap = obsCountMap;
	}
}
