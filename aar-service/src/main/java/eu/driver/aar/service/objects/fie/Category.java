package eu.driver.aar.service.objects.fie;

import java.util.HashMap;
import java.util.Map;

public class Category {
	
	private String categoryId;
	private Rating baselineRating = new Rating();
	private Map<String, Rating> innovationRatings = new HashMap<String, Rating>();
	
	private String baselineEffortQuestion = null;
	private String baselineResultQuestion = null;
	private String innovationlineEffortQuestion = null;
	private String innovationlineResultQuestion = null;
	
	public Category(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public Rating getBaselineRating() {
		return baselineRating;
	}

	public void setBaselineRating(Rating baselineRating) {
		this.baselineRating = baselineRating;
	}
	
	public Map<String, Rating> getInnovationRatings() {
		return innovationRatings;
	}

	public void setInnovationRatings(Map<String, Rating> innovationRatings) {
		this.innovationRatings = innovationRatings;
	}

	public Rating getInnovationRating(String key) {
		Rating rating = this.innovationRatings.get(key);
		if (rating == null) {
			rating = new Rating();
			this.innovationRatings.put(key, rating);
		}
		return rating;
	}

	public void setInnovationRating(String key, Rating innovationRating) {
		this.innovationRatings.put(key, innovationRating);
	}

	public String getBaselineEffortQuestion() {
		return baselineEffortQuestion;
	}

	public void setBaselineEffortQuestion(String baselineEffortQuestion) {
		this.baselineEffortQuestion = baselineEffortQuestion;
	}

	public String getBaselineResultQuestion() {
		return baselineResultQuestion;
	}

	public void setBaselineResultQuestion(String baselineResultQuestion) {
		this.baselineResultQuestion = baselineResultQuestion;
	}

	public String getInnovationlineEffortQuestion() {
		return innovationlineEffortQuestion;
	}

	public void setInnovationlineEffortQuestion(String innovationlineEffortQuestion) {
		this.innovationlineEffortQuestion = innovationlineEffortQuestion;
	}

	public String getInnovationlineResultQuestion() {
		return innovationlineResultQuestion;
	}

	public void setInnovationlineResultQuestion(String innovationlineResultQuestion) {
		this.innovationlineResultQuestion = innovationlineResultQuestion;
	}
}
