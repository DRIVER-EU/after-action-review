package eu.driver.aar.service.objects.fie;

import java.util.ArrayList;
import java.util.List;

public class Rating {
	
	private List<String> comments = new ArrayList<String>();
	private List<Integer> effortRating = new ArrayList<Integer>();
	private List<Integer> resultRating = new ArrayList<Integer>();
	
	private Double effortAvr = 0d;
	private Double resultAvr = 0d;
	
	private int effortCount = 0;
	private Double effortSquares = 0d;
	private Double effortSum = 0d;
	
	private int resultCount = 0;
	private Double resultSquares = 0d;
	private Double resultSum = 0d;
	
	public Rating() {
		
	}

	public List<String> getComments() {
		return comments;
	}

	public void setComments(List<String> comments) {
		this.comments = comments;
	}
	
	public void addComment(String comment) {
		this.comments.add(comment);
	}

	public List<Integer> getEffortRating() {
		return effortRating;
	}

	public void setEffortRating(List<Integer> effortRating) {
		this.effortRating = effortRating;
	}
	
	public void addEffortRating(Integer rating) {
		this.effortRating.add(rating);
		
		effortSum += rating;
        effortSquares += rating * rating;
        effortCount = this.effortRating.size();
        
		this.effortAvr = effortSum/effortCount;
	}

	public List<Integer> getResultRating() {
		return resultRating;
	}

	public void setResultRating(List<Integer> resultRating) {
		this.resultRating = resultRating;
	}
	
	public void addResultRating(Integer rating) {
		this.resultRating.add(rating);

		resultSum += rating;
        resultSquares += rating * rating;
        resultCount = this.resultRating.size();

		this.resultAvr = resultSum/resultCount;
	}

	public Double getEffortAvr() {
		return effortAvr;
	}

	public void setEffortAvr(Double effortAvr) {
		this.effortAvr = effortAvr;
	}

	public Double getResultAvr() {
		return resultAvr;
	}

	public void setResultAvr(Double resultAvr) {
		this.resultAvr = resultAvr;
	}
	
	public double getEffortVariance() {
        double variance = 0.0;
        if (effortCount > 1D) {
            variance = (effortSquares-(double)effortSum*effortSum/effortCount)/(effortCount-1);
        }
        return variance;
    }
	
	public double getEffortStdDev() {
        return Math.sqrt(this.getEffortVariance());
    }
	
	public double getResultVariance() {
        double variance = 0.0;
        if (resultCount > 1D) {
            variance = (resultSquares-(double)resultSum*resultSum/resultCount)/(resultCount-1);
        }
        return variance;
    }
	
	public double getResultStdDev() {
        return Math.sqrt(this.getResultVariance());
    }
	
	

}
