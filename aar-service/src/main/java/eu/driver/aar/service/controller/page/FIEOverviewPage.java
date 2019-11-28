package eu.driver.aar.service.controller.page;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import eu.driver.aar.service.constants.CategoryMapper;
import eu.driver.aar.service.objects.fie.Category;
import eu.driver.aar.service.objects.fie.Package;
import eu.driver.aar.service.objects.fie.Rating;
import eu.driver.aar.service.utils.pdf.MultiLine;
import eu.driver.aar.service.utils.pdf.XYVectorizedRenderer;

public class FIEOverviewPage {
	
	private Logger log = Logger.getLogger(this.getClass());
	private static final String DEFAULT_FORMAT = "0.###";
	private static final NumberFormat FORMATTER = new DecimalFormat(DEFAULT_FORMAT);
	
	private PDPageContentStream contentStream = null;
	
	public FIEOverviewPage() {
		
	}
	
	public void addFIEOverviewGraphPage(PDDocument document, String packageId,  Package pckg, int paragrahNumber) {
		PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
		document.addPage(page);
		float height = page.getMediaBox().getHeight();
		
		try {
			contentStream = new PDPageContentStream(document, page);
			float endY = MultiLine.drawMultiLineText(paragrahNumber + ") " + CategoryMapper.getInstance().getHeadingforQuestion(packageId),
					20, height-40, 500, page, contentStream, PDType1Font.TIMES_BOLD, 14, new Color(0, 72, 126));
			
			endY = MultiLine.drawMultiLineText(paragrahNumber + ".1) Graph:",
					30, endY-10, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 12, new Color(0, 72, 126));
			
			XYSeriesCollection dataset = new XYSeriesCollection( );
			for (int i = 1; i <= 7; i++)  {
				Category category = pckg.getPackageCategoryMap().get(Integer.toString(i));
				if (category != null) {
					XYSeries base = new XYSeries("BL" + category.getCategoryId(), false );
				    base.add( category.getBaselineRating().getEffortAvr() , category.getBaselineRating().getResultAvr() );
				    dataset.addSeries( base ); 
					Map<String, Rating> treeMap = new TreeMap<>(category.getInnovationRatings());
					for (String key : treeMap.keySet()) {
				    	Rating rating = treeMap.get(key);
				    	XYSeries innovation = new XYSeries(category.getCategoryId() + "-" +  key, false );          
				        innovation.add( rating.getEffortAvr() , rating.getResultAvr());
				          
				        XYSeries change = new XYSeries( "Ch-" + category.getCategoryId() + "-" + key, false ); 
				        change.add( category.getBaselineRating().getEffortAvr() , category.getBaselineRating().getResultAvr() );    
				        change.add( rating.getEffortAvr() , rating.getResultAvr());
				        
				        dataset.addSeries( innovation );
				        dataset.addSeries( change );
				    };
				}
			}
			
			try {
				JFreeChart xylineChart = ChartFactory.createXYLineChart(
				         "Change Graph" ,
				         "Effort" ,
				         "Result" ,
				         dataset,
				         PlotOrientation.VERTICAL ,
				         true , false , false);
				
				ChartPanel chartPanel = new ChartPanel( xylineChart );
				chartPanel.setPreferredSize( new java.awt.Dimension( 420 , 250 ) );
				XYPlot plot = xylineChart.getXYPlot( );
				NumberAxis numberaxis = ( NumberAxis )plot.getDomainAxis( );                 
			    numberaxis.setLowerBound(0.0);
			    numberaxis.setUpperBound(10.0);
			    NumberAxis numberaxis1 = ( NumberAxis )plot.getRangeAxis( );                 
			    numberaxis1.setLowerBound(0.0);                 
			    numberaxis1.setUpperBound(10.0);
				
			    int seriesCount = 0;
				XYVectorizedRenderer renderer = new XYVectorizedRenderer(false, true );
				for (int i = 1; i <= 7; i++)  {
					Category category = pckg.getPackageCategoryMap().get(Integer.toString(i));
					if (category != null) {
						renderer.setSeriesShape(seriesCount, new Ellipse2D.Double(-7D, -7D, 14D, 14D));
						renderer.setSeriesPaint(seriesCount, FIEColorGenerator.getInstance().getBLBubbleColor(category.getCategoryId()));
						renderer.setSeriesStroke(seriesCount, new BasicStroke( 4.0f ) );
						seriesCount++;
						Map<String, Rating> treeMap = new TreeMap<>(category.getInnovationRatings());
						int treeSetId = 1;
						for (String key : treeMap.keySet()) {
							renderer.setSeriesShape(seriesCount, new Ellipse2D.Double(-5D, -5D, 10D, 10D));
							renderer.setSeriesShape(seriesCount+1, new Ellipse2D.Double(-5D, -5D, 10D, 10D));
							renderer.setSeriesLinesVisible(seriesCount+1, true);
							renderer.setSeriesShapesVisible(seriesCount+1, false);
							if (treeSetId == 1) {
								renderer.setSeriesStroke( seriesCount+1 , new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );	
							} else {
								float dash1[] = {5.0f};
								renderer.setSeriesStroke( seriesCount+1 , new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 5.0f, dash1, 0.0f) );
							}
							
							renderer.setSeriesPaint( seriesCount , FIEColorGenerator.getInstance().getILColor(category.getCategoryId()));
							renderer.setSeriesPaint( seriesCount+1 , FIEColorGenerator.getInstance().getILColor(category.getCategoryId()) );
							seriesCount+=2;
							treeSetId++;
						}
					}
				}
				
				try {
					plot.setRenderer( renderer );
					
					LegendItemCollection legentItems = new LegendItemCollection();
					legentItems.add(new LegendItem(CategoryMapper.getInstance().getCategory("1"),
							null, null, null, new Ellipse2D.Double(-5D, -5D, 10D, 10D), FIEColorGenerator.getInstance().getILColor("1")));
					legentItems.add(new LegendItem(CategoryMapper.getInstance().getCategory("2"),
							null, null, null, new Ellipse2D.Double(-5D, -5D, 10D, 10D), FIEColorGenerator.getInstance().getILColor("2")));
					legentItems.add(new LegendItem(CategoryMapper.getInstance().getCategory("3"),
							null, null, null, new Ellipse2D.Double(-5D, -5D, 10D, 10D), FIEColorGenerator.getInstance().getILColor("3")));
					legentItems.add(new LegendItem(CategoryMapper.getInstance().getCategory("4"),
							null, null, null, new Ellipse2D.Double(-5D, -5D, 10D, 10D), FIEColorGenerator.getInstance().getILColor("4")));
					legentItems.add(new LegendItem(CategoryMapper.getInstance().getCategory("5"),
							null, null, null, new Ellipse2D.Double(-5D, -5D, 10D, 10D), FIEColorGenerator.getInstance().getILColor("5")));
					legentItems.add(new LegendItem(CategoryMapper.getInstance().getCategory("6"),
							null, null, null, new Ellipse2D.Double(-5D, -5D, 10D, 10D), FIEColorGenerator.getInstance().getILColor("6")));
					legentItems.add(new LegendItem(CategoryMapper.getInstance().getCategory("7"),
							null, null, null, new Ellipse2D.Double(-5D, -5D, 10D, 10D), FIEColorGenerator.getInstance().getILColor("7")));
					plot.setFixedLegendItems(legentItems);
					
				    OutputStream out = new FileOutputStream("./charts/graph.png");
				    ChartUtils.writeChartAsPNG(out,
				    		xylineChart,
				    		700,
				    		450);
				    out.close();
				} catch (IOException ex) {
				    log.error(ex);
				}
			
				PDImageXObject pdImage = PDImageXObject.createFromFile("./charts/graph.png",document);
				contentStream.drawImage(pdImage, 20, endY-460,700, 450);
			} catch (Exception e) {
				log.error("Error creating the graph!", e);
			}
			endY-=510;
		} catch (Exception e) {
			log.error("Error creating the addFIEOverviewGraphPage!", e);
		} finally {
			if (contentStream != null) {
				try {
					contentStream.close();
				} catch (Exception e) {
					log.error("Error closing the write stream");
				}
			}
		}
		
		page = new PDPage(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
		document.addPage(page);
		height = page.getMediaBox().getHeight();
		contentStream = null;
		
		try {
			contentStream = new PDPageContentStream(document, page);
			float endY = MultiLine.drawMultiLineText(paragrahNumber + ".2) The Result in Details:",
					30, height-40, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 12, new Color(0, 72, 126));
			
			for (int i = 1; i <= 7; i++)  {
				Category category = pckg.getPackageCategoryMap().get(Integer.toString(i));
				if (category != null) {
					Double blEffAvr = category.getBaselineRating().getEffortAvr();
					Double blResAvr = category.getBaselineRating().getResultAvr();
					endY = checkPage(document, height, endY);
					endY = MultiLine.drawMultiLineText(paragrahNumber + ".2." + i + ") " + CategoryMapper.getInstance().getHeadingforCategory(packageId, category.getCategoryId()),
							30, endY-20, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 12, new Color(0, 72, 126));
					endY = MultiLine.drawMultiLineText("Baseline:",
							40, endY-5, 480, page, contentStream, PDType1Font.TIMES_ITALIC, 8, new Color(0, 0, 0));
					endY = MultiLine.drawMultiLineText("Nr of received answers for rating the Effort: " + 
							category.getBaselineRating().getEffortRating().size() + 
							" which results in a average of: " + FORMATTER.format(category.getBaselineRating().getEffortAvr()) + 
							" (stand. deviation: " + FORMATTER.format(category.getBaselineRating().getEffortStdDev()) + ")",
							50, endY-5, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(0, 0, 0));
					endY = MultiLine.drawMultiLineText("Nr of received answers for rating the Result: " + 
							category.getBaselineRating().getResultRating().size() + 
							" which results in a average of: " + FORMATTER.format(category.getBaselineRating().getResultAvr()) + 
							" (stand. deviation: " + FORMATTER.format(category.getBaselineRating().getResultStdDev()) + ")",
							50, endY-5, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(0, 0, 0));
					
					Map<String, Rating> treeMap = new TreeMap<>(category.getInnovationRatings());
					for (String key : treeMap.keySet()) {
						try {
							Rating rating = treeMap.get(key);
							endY = checkPage(document, height, endY);
							endY = MultiLine.drawMultiLineText("Innovationline: " + key,
									40, endY-5, 480, page, contentStream, PDType1Font.TIMES_ITALIC, 10, new Color(0, 0, 0));
							endY = MultiLine.drawMultiLineText("Nr of received answers for rating the Effort: " + 
									rating.getEffortRating().size() + 
									" which results in a average of: " + FORMATTER.format(rating.getEffortAvr()) + 
									" (stand. deviation: " + FORMATTER.format(rating.getEffortStdDev()) + ")",
									50, endY-5, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(0, 0, 0));
							if (blEffAvr.compareTo(rating.getEffortAvr()) == 0) {
								endY = MultiLine.drawMultiLineText("-> same average of Effort: " + FORMATTER.format(blEffAvr),
										50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(160, 160, 160));
							} else if (blEffAvr.compareTo(rating.getEffortAvr()) >= 0) {
								endY = MultiLine.drawMultiLineText("-> average decrease of Effort by: " + FORMATTER.format((blEffAvr-rating.getEffortAvr())),
										50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(0, 102, 0));
							} else {
								endY = MultiLine.drawMultiLineText("-> average increase of Effort by: " + FORMATTER.format((rating.getEffortAvr()-blEffAvr)),
										50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(204, 0, 0));
							}
							endY = MultiLine.drawMultiLineText("Nr of received answers for rating the Result: " + 
									rating.getResultRating().size() + 
									" which results in a average of: " + FORMATTER.format(rating.getResultAvr()) + 
									" (stand. deviation: " + FORMATTER.format(rating.getResultStdDev()) + ")",
									50, endY-5, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(0, 0, 0));
							if (rating.getResultAvr().compareTo(blResAvr) == 0) {
								endY = MultiLine.drawMultiLineText("-> same average of Result: " + FORMATTER.format(rating.getResultAvr()),
										50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(160, 160, 160));
							} else if (rating.getResultAvr().compareTo(blResAvr) > 0) {
								endY = MultiLine.drawMultiLineText("-> average increase of Result by: " + FORMATTER.format((rating.getResultAvr()-blResAvr)),
										50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(0, 102, 0));
							} else {
								endY = MultiLine.drawMultiLineText("-> average decrease of Result by: " + FORMATTER.format((blResAvr-rating.getResultAvr())),
										50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(204, 0, 0));
							}
						} catch (IOException e) {
							
						}
					}
					
					// check if comments are available
					boolean showComments = false;
					if (category.getBaselineRating().getComments() != null && category.getBaselineRating().getComments().size() > 0) {
						showComments = true;
					} else {
						for (String key : treeMap.keySet()) {
							Rating rating = treeMap.get(key);
							if (rating.getComments() != null && rating.getComments().size() > 0) {
								showComments = true;
							}
						}
					}
					
					if (showComments) {
						endY = checkCommentPage(document, height, endY);
						
						// comments
						endY = MultiLine.drawMultiLineText(paragrahNumber + ".3) Following comments have been reported for Baseline:",
								30, endY-15, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 10, new Color(0, 0, 0));
						for (String comment : category.getBaselineRating().getComments()) {
							endY = checkCommentPage(document, height, endY);
							endY = MultiLine.drawMultiLineText("* " +comment,
									40, endY-5, 480, page, contentStream, PDType1Font.TIMES_ITALIC, 8, new Color(0, 0, 0));
						}
						
						endY = checkPage(document, height, endY);
						endY = MultiLine.drawMultiLineText(paragrahNumber + ".4) Following comments have been reported for Innovationline:",
								30, endY-20, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 10, new Color(0, 0, 0));
						for (String key : treeMap.keySet()) {
							Rating rating = treeMap.get(key);
							for (String comment : rating.getComments()) {
								endY = checkCommentPage(document, height, endY);
								endY = MultiLine.drawMultiLineText("* " +comment,
										40, endY-5, 480, page, contentStream, PDType1Font.TIMES_ITALIC, 8, new Color(0, 0, 0));
							}	
						};
					}
				}
			}
		} catch (Exception e) {
			log.error("Error creating the addFIEOverviewGraphPage!", e);
		} finally {
			if (contentStream != null) {
				try {
					contentStream.close();
				} catch (Exception e) {
					log.error("Error closing the write stream");
				}
			}
		}
	}
	
	private float checkPage(PDDocument document, float hight, float endy) {
		if ((endy - 200) < 0) {
			endy = hight - 40;
			if (contentStream != null) {
				try {
					contentStream.close();
				} catch (Exception e) {
					log.error("Error closing the write stream");
				}
			}
			PDPage page = page = new PDPage(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
			document.addPage(page);
			
			try {
				contentStream = new PDPageContentStream(document, page);
			} catch (Exception e) {
				log.error("Error creating the new Page!", e);
			}
			
		}
		return endy;
	}
	
	private float checkCommentPage(PDDocument document, float hight, float endy) {
		if ((endy - 40) < 0) {
			endy = hight - 40;
			if (contentStream != null) {
				try {
					contentStream.close();
				} catch (Exception e) {
					log.error("Error closing the write stream");
				}
			}
			PDPage page = page = new PDPage(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
			document.addPage(page);
			
			try {
				contentStream = new PDPageContentStream(document, page);
			} catch (Exception e) {
				log.error("Error creating the new Page!", e);
			}
			
		}
		return endy;
	}
}
