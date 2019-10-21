package eu.driver.aar.service.constants;

public class CategoryMapper {
	
	private static CategoryMapper aMe = null;
	
	private CategoryMapper() {
		
	}
	
	public static synchronized CategoryMapper getInstance() {
		if (CategoryMapper.aMe == null) {
			CategoryMapper.aMe = new CategoryMapper();
		}
		return CategoryMapper.aMe;
	}
	
	public String getHeadingforCategory(String packageId, String id) {
		String heading = null;
		
		if (packageId.equalsIgnoreCase("Q1")) {
			heading = "Q1: ERCC briefing materials - ";
		} else if (packageId.equalsIgnoreCase("Q2")) {
			heading = "Q2: EUCPT Daily SitRep - ";
		} else if (packageId.equalsIgnoreCase("Q3")) {
			heading = "Q3: EUCPT briefing for in-coming modules - ";
		} else if (packageId.equalsIgnoreCase("Q4")) {
			heading = "Q4: Modules' status update - ";
		}
		
		if (id.equalsIgnoreCase("1")) {
			// usability
			heading += "Result for category: 1-USABILITY";
		} else if (id.equalsIgnoreCase("2")) {
			// editability
			heading += "Result for category: 2-EDITABILITY";
		} else if (id.equalsIgnoreCase("3")) {
			// formatting
			heading += "Result for category: 3-FORMATTING";
		} else if (id.equalsIgnoreCase("4")) {
			// searchability
			heading += "Result for category: 4-SEARCHABLILITY";
		} else if (id.equalsIgnoreCase("5")) {
			// structure
			heading += "Result for category: 5-STRUCTURE";
		} else if (id.equalsIgnoreCase("6")) {
			// visualization
			heading += "Result for category: 6-VISUALIZATION";
		}  else if (id.equalsIgnoreCase("7")) {
			// relevance
			heading += "Result for category: 7-RELEVANCE";
		} else {
			// unknown
			heading += "Result for category: UNKNOWN";
		}
		
		return heading;
	}

}
