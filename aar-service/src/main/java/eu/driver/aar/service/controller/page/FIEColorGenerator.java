package eu.driver.aar.service.controller.page;

import java.awt.Color;

public class FIEColorGenerator {
	
	private static FIEColorGenerator aMe = null;
	
	private FIEColorGenerator() {
		
	}
	
	public static synchronized FIEColorGenerator getInstance() {
		if (FIEColorGenerator.aMe == null) {
			FIEColorGenerator.aMe = new FIEColorGenerator();
		}
		return FIEColorGenerator.aMe;
	}
	
	public Color getBLBubbleColor(String catId) {
		Color color = new Color(0, 0, 0);
		
		if (catId.equalsIgnoreCase("1")) {
			// blue
			color = new Color(109, 154, 237);
		} else if (catId.equalsIgnoreCase("2")) {
			// orange
			color = new Color(237, 211, 133);
		} else if (catId.equalsIgnoreCase("3")) {
			// purple
			color = new Color(185, 134, 209);
		} else if (catId.equalsIgnoreCase("4")) {
			// gray
			color = new Color(217, 213, 219);
		} else if (catId.equalsIgnoreCase("5")) {
			// black
			color = new Color(81, 78, 82);
		} else if (catId.equalsIgnoreCase("6")) {
			// green
			color = new Color(159, 237, 161);
		} else if (catId.equalsIgnoreCase("7")) {
			// red
			color = new Color(217, 160, 171);
		}
		
		return color;
	}
	
	public Color getILColor(String catId) {
		Color color = new Color(0, 0, 0);
		
		if (catId.equalsIgnoreCase("1")) {
			// blue
			color = new Color(20, 78, 186);
		} else if (catId.equalsIgnoreCase("2")) {
			// orange
			color = new Color(227, 183, 50);
		} else if (catId.equalsIgnoreCase("3")) {
			// purple
			color = new Color(161, 62, 207);
		} else if (catId.equalsIgnoreCase("4")) {
			// gray
			color = new Color(146, 141, 148);
		} else if (catId.equalsIgnoreCase("5")) {
			// black
			color = new Color(15, 4, 20);
		} else if (catId.equalsIgnoreCase("6")) {
			// green
			color = new Color(20, 201, 26);
		} else if (catId.equalsIgnoreCase("7")) {
			// red
			color = new Color(189, 58, 82);
		}
		
		return color;
	}

}
