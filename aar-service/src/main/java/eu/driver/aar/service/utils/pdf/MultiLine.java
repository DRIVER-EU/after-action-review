package eu.driver.aar.service.utils.pdf;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import be.quodlibet.boxable.utils.PDStreamUtils;

public class MultiLine {
	
	/**
	 * @param text The text to write on the page.
	 * @param x The position on the x-axis.
	 * @param y The position on the y-axis.
	 * @param allowedWidth The maximum allowed width of the whole text (e.g. the width of the page - a defined margin).
	 * @param page The page for the text.
	 * @param contentStream The content stream to set the text properties and write the text.
	 * @param font The font used to write the text.
	 * @param fontSize The font size used to write the text.
	 * @throws IOException
	 */
	public static float drawMultiLineText(String text, float x, float y, int allowedWidth, PDPage page, PDPageContentStream contentStream, PDFont font, int fontSize, Color fontColor) throws IOException {
		text = text.replace("\n", "").replace("\r", "");
		List<String> lines = new ArrayList<String>();
	    String myLine = "";
	    String[] words = text.split(" ");
	    for(String word : words) {

	        if(!myLine.isEmpty()) {
	            myLine += " ";
	        }
	        int size = (int) (fontSize * font.getStringWidth(myLine + word) / 1000);
	        if(size > allowedWidth) {
	            lines.add(myLine);
	            myLine = word;
	        } else {
	            myLine += word;
	        }
	    }
	    lines.add(myLine);

	    for(String line : lines) {
	    	PDStreamUtils.write(contentStream, line, font, fontSize, x, y, fontColor);
	        y -= (fontSize+2);
	    }
	    return y;
	}
}
