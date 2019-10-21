package eu.driver.aar.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.ShapeList;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import be.quodlibet.boxable.Paragraph;
import be.quodlibet.boxable.utils.PDStreamUtils;
import eu.driver.aar.service.constants.AARConstants;
import eu.driver.aar.service.constants.CategoryMapper;
import eu.driver.aar.service.dto.Record;
import eu.driver.aar.service.dto.Trial;
import eu.driver.aar.service.objects.ChartMapObjects;
import eu.driver.aar.service.objects.fie.Category;
import eu.driver.aar.service.objects.fie.Package;
import eu.driver.aar.service.objects.fie.Rating;
import eu.driver.aar.service.repository.RecordRepository;
import eu.driver.aar.service.repository.TrialRepository;
import eu.driver.aar.service.utils.pdf.MultiLine;

@RestController
public class StatisticRESTController {
	
	private static final String DEFAULT_FORMAT = "0.###";
    private static final NumberFormat FORMATTER = new DecimalFormat(DEFAULT_FORMAT);
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private final String reportFooter1 = "This project has received funding from the "
			+ "European Union’s 7th Framework Programme for Research, Technological "
			+ "Development and";
	private final String reportFooter2 = "Demonstration under Grant Agreement (GA) N° #607798";
	
	@Autowired
	RecordRepository recordRepo;
	
	@Autowired
	TrialRepository trialRepo;
	
	@ApiOperation(value = "createStatistic", nickname = "createStatistic")
	@RequestMapping(value = "/AARService/createStatistic", method = RequestMethod.GET)
	@ApiImplicitParams({ 
		@ApiImplicitParam(name = "runType", value = "the runType for which the charts should be created", required = false, dataType = "string", paramType = "query", allowableValues=""),
		@ApiImplicitParam(name = "pieType", value = "the type of the chart", required = true, dataType = "string", paramType = "query", allowableValues="PIE,BAR")})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = byte[].class),
			@ApiResponse(code = 400, message = "Bad Request", response = byte[].class),
			@ApiResponse(code = 500, message = "Failure", response = byte[].class) })
	public ResponseEntity<byte[]> createStatistic(@QueryParam("runType")String runType, @QueryParam("pieType")String pieType) {
		log.info("-->createStatistic");
		byte[] fileContent = null;
		
		try {
			this.cleanUpChartDirectoy();
		} catch (Exception e) {
			fileContent = e.getMessage().getBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_HTML);
		    log.info("createStatistic-->");
		    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		this.createChartsAndComments(runType, pieType);
		
		try {
			this.pack("./charts", "./charts.zip");
			File file = new File("./charts.zip");
			
			try {
				fileContent = Files.readAllBytes(file.toPath());
			} catch (IOException e) {
				log.error("Error loading the file!", e);
				fileContent = "The zip file containing the charts and comments could not be loaded!".getBytes();
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.TEXT_HTML);
			    log.info("createStatistic-->");
			    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
		} catch (Exception e) {
			log.error("Error creating the zip file!", e);
			fileContent = "The zip file containing the charts and comments could not be created!".getBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_HTML);
		    log.info("createStatistic-->");
		    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("application/zip"));
		headers.setContentDispositionFormData("attachment", "charts.zip"); 
	    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
	    log.info("createStatistic-->");
	    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
	}
	
	@ApiOperation(value = "createPDFStatisticReport", nickname = "createPDFStatisticReport")
	@RequestMapping(value = "/AARService/createPDFStatisticReport", method = RequestMethod.GET)
	@ApiImplicitParams({ 
		@ApiImplicitParam(name = "runType", value = "the runType for which the charts should be created", required = false, dataType = "string", paramType = "query", allowableValues=""),
		@ApiImplicitParam(name = "pieType", value = "the type of the chart", required = true, dataType = "string", paramType = "query", allowableValues="PIE,BAR")})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = byte[].class),
			@ApiResponse(code = 400, message = "Bad Request", response = byte[].class),
			@ApiResponse(code = 500, message = "Failure", response = byte[].class) })
	public ResponseEntity<byte[]> createPDFStatisticReport(@QueryParam("runType")String runType, @QueryParam("pieType")String pieType) {
		log.info("-->createPDFStatisticReport: " + runType);
		byte[] fileContent = null;
		
		if(Files.exists(Paths.get("./report.pdf"))) {
			try {
				Files.delete(Paths.get("./report.pdf"));
			} catch (IOException e) {
				log.error("Error delete the old report.pdf file!", e);
				fileContent = "Could not delete the old report file, new report was not generated!".getBytes();
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.TEXT_HTML);
			    log.info("createPDFStatisticReport-->");
			    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		
		try {
			this.cleanUpChartDirectoy();
		} catch (Exception e) {
			fileContent = e.getMessage().getBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_HTML);
		    log.info("createPDFStatisticReport-->");
		    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		ChartMapObjects returnObjects = this.createChartAndCommentsMaps(runType, pieType);
		Map<Integer, Map<String, Integer>> answerMapMap = returnObjects.getAnswerMapMap();
		Map<Integer, List<String>> commentsMap = returnObjects.getCommentsMap();
		Map<Integer, String> questionMap = returnObjects.getQuestionMap();
		Map<String, Integer> obsCountMap = returnObjects.getObsCountMap();
		
		Trial trial = trialRepo.findActualTrial();
		
		PDDocument document = new PDDocument();
		this.addStartPage(document, trial.getTrialName(), "Generated Report of Observer Answers");
		this.addOverviewStatistic(document, answerMapMap, obsCountMap);
		
		answerMapMap.forEach((answerId, answerMap) -> {
			this.addStatisticPage(document, answerMap, commentsMap.get(answerId), questionMap.get(answerId), answerId);
		});
		
		try {
			document.save("./report.pdf");
	        document.close();	
		} catch (IOException ie) {
			log.error("Error creating the report.pdf", ie);
			fileContent = "Could not write the report file!".getBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_HTML);
		    log.info("createPDFStatisticReport-->");
		    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		File file = new File("./report.pdf");
		try {
			fileContent = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			log.error("Error loading the file!", e);
			fileContent = "Report file could not be found!".getBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_HTML);
		    log.info("createPDFStatisticReport-->");
		    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("attachment", "report.pdf"); 
	    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
	    log.info("createPDFStatisticReport-->");
	    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
	}
	
	@ApiOperation(value = "createFIEPDFReport", nickname = "createFIEPDFReport")
	@RequestMapping(value = "/AARService/createFIEPDFReport", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = byte[].class),
			@ApiResponse(code = 400, message = "Bad Request", response = byte[].class),
			@ApiResponse(code = 500, message = "Failure", response = byte[].class) })
	public ResponseEntity<byte[]> createFIEPDFReport() {
		log.info("-->createFIEPDFReport");
		String docName = "./reportFIE.pdf";
		byte[] fileContent = null;
		if(Files.exists(Paths.get(docName))) {
			try {
				Files.delete(Paths.get(docName));
			} catch (IOException e) {
				log.error("Error delete the old reportFIE.pdf file!", e);
				fileContent = "Could not delete the old report file, new report was not generated!".getBytes();
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.TEXT_HTML);
			    log.info("createFIEPDFReport-->");
			    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		
		try {
			this.cleanUpChartDirectoy();
		} catch (Exception e) {
			fileContent = e.getMessage().getBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_HTML);
		    log.info("createFIEPDFReport-->");
		    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		Map<String, Package> packageMap = new HashMap<String, Package>();
		
		Trial trial = trialRepo.findActualTrial();
		if (trial == null) {
			fileContent = "Could not load the actual trial information!".getBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_HTML);
		    log.info("createFIEPDFReport-->");
		    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		List<Record> records = recordRepo.findObjectsByRecordTypeAndRunType("ObserverToolAnswer", AARConstants.RECORD_RUN_TYPE_FIE);
		for (Record record : records) {
			try {
				JSONObject recordJson = new JSONObject(record.getRecordJson());
				
				JSONArray questions = recordJson.getJSONArray("questions");
				int count = questions.length();
				for (int i = 0; i < count; i++) {
					JSONObject answer = questions.getJSONObject(i);
					if (answer.getString("typeOfQuestion").equalsIgnoreCase("radiobutton")) {
						String questionString = answer.getString("name");
						
						int idx = questionString.indexOf(":");
						if (idx > 0) {
							String marker = questionString.substring(0, idx);
							StringTokenizer tokens = new StringTokenizer(marker, "/");
							if (tokens.countTokens() == 4) {
								String packgeId = tokens.nextToken();
								String runType = tokens.nextToken();
								String axis = tokens.nextToken();
								String categoryId = tokens.nextToken();
								
								Integer rating = Integer.parseInt(answer.getString("answer"));
								
								Package pckg = packageMap.get(packgeId);
								if (pckg == null) {
									pckg = new Package(packgeId);
								}
								Category category = pckg.getCategory(categoryId);
								Rating catRating = null;
								if (runType.equalsIgnoreCase("BL")) {
									catRating = category.getBaselineRating();
									if (axis.equalsIgnoreCase("E")) {
										catRating.addEffortRating(rating);
										if (category.getBaselineEffortQuestion() == null) {
											category.setBaselineEffortQuestion(questionString);
										}
									} else {
										catRating.addResultRating(this.reversRating(rating));
										if (category.getBaselineResultQuestion() == null) {
											category.setBaselineResultQuestion(questionString);
										}
									}
								} else {
									catRating = category.getInnovationRating();
									if (axis.equalsIgnoreCase("E")) {
										catRating.addEffortRating(rating);
										if (category.getInnovationlineEffortQuestion() == null) {
											category.setInnovationlineEffortQuestion(questionString);
										}
									} else {
										catRating.addResultRating(this.reversRating(rating));
										if (category.getInnovationlineResultQuestion() == null) {
											category.setInnovationlineResultQuestion(questionString);
										}
									}
								}
								
								if (!answer.isNull("comment") && answer.getString("comment").length() > 0) {
									catRating.addComment(recordJson.getString("observationTypeName") + ": " + answer.getString("comment"));	
								}
								
								packageMap.put(packgeId, pckg);
							} else {
								log.error("Message key not correct: " + marker);
							}
							
						}
					}
				}
			} catch (Exception e) {
				log.error("Error parsing the answer!", e);
			}
		}
		
		PDDocument document = new PDDocument();
		this.addStartPage(document, trial.getTrialName(), "Generated First Impression Evaluation Report");
		
		Package pckg = packageMap.get("Q1");
		int paragrahNumber = 1;
		if (pckg != null) {
			for (Map.Entry<String, Category> entry : pckg.getPackageCategoryMap().entrySet()) {
				this.addFIEGraphPage(document, pckg.getPackageId(), entry.getValue(), paragrahNumber);
				paragrahNumber++;
			};
		}
		
		try {
			this.addFooter(document);	
		} catch (Exception e) {
			log.error("Error creating the Footer on the!");
		}
		
		try {
			document.save(docName);
	        document.close();	
		} catch (IOException ie) {
			log.error("Error creating the report.pdf", ie);
			fileContent = "Could not write the report file!".getBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_HTML);
		    log.info("createFIEPDFReport-->");
		    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		File file = new File(docName);
		try {
			fileContent = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			log.error("Error loading the file!", e);
			fileContent = "Report file could not be found!".getBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_HTML);
		    log.info("createFIEPDFReport-->");
		    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("attachment", "reportFIE.pdf"); 
	    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
	    log.info("createFIEPDFReport-->");
	    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
	}
	
	private void cleanUpChartDirectoy() throws Exception {
		log.info("-->cleanUpChartDirectoy");
		
		if (!(Files.exists(Paths.get("./charts")))) {
            try {
				Files.createDirectories(Paths.get("./charts"));
			} catch (IOException e) {
				log.error("Error creating the ./charts directory!", e);
				throw new Exception("Error creating the ./charts directory!", e);
			}
        } else {
        	try {
        		Files.walk(Paths.get("./charts"))
        			.sorted(Comparator.reverseOrder())
        			.map(Path::toFile)
        			.forEach(File::delete);
				Files.createDirectories(Paths.get("./charts"));
			} catch (IOException e) {
				log.error("Error creating the ./charts directory!", e);
				throw new Exception("Error deleting the ./charts directory content!", e);
			}
        }
		
		log.info("cleanUpChartDirectoy-->");
	}
	private void createChartsAndComments(String runType, String pieType) {
		log.info("-->createChartsAndComments");
		
		ChartMapObjects returnObjects = this.createChartAndCommentsMaps(runType, pieType);
		Map<Integer, Map<String, Integer>> answerMapMap = returnObjects.getAnswerMapMap();
		Map<Integer, List<String>> commentsMap = returnObjects.getCommentsMap();
		Map<Integer, String> questionMap = returnObjects.getQuestionMap();
		
		if (returnObjects != null) {
			answerMapMap.forEach((answerId,answerCountMap)->{
				JFreeChart chart = null;
				if (pieType.equalsIgnoreCase("PIE")) {
					DefaultPieDataset dataset = new DefaultPieDataset();
					answerCountMap.forEach((question, count)->{
						dataset.setValue(question + " = " + count, new Double(count));
					});
					
					chart = createPieChart3D(dataset);
					chart.setTitle(questionMap.get(answerId));
				} else {
					DefaultCategoryDataset dataset = new DefaultCategoryDataset( );  
					answerCountMap.forEach((question, count)->{
						dataset.addValue(new Double(count), question + " = " + count, question + " = " + count);
					});
					chart = ChartFactory.createBarChart(
							questionMap.get(answerId),
				            "Question",
				            "Answer count",
				            dataset,
				            PlotOrientation.VERTICAL,
				            true, true, false);
				}
				
				try {
					if (pieType.equalsIgnoreCase("PIE")) {
						Files.write(Paths.get("./charts/chart " + answerId + ".svg"), this.getSvgXML(chart, 600, 400).getBytes());	
					} else {
						Files.write(Paths.get("./charts/chart " + answerId + ".svg"), this.getSvgXML(chart, answerCountMap.size()*200, 400).getBytes());
					}
					
				} catch (Exception e) {
					log.error("Error writing ./charts/chart " + answerId + ".svg file.", e);
				}
			});
			
			commentsMap.forEach((answerId, comments)->{
				String chartComments = "";
				for (String comment : comments) {
					chartComments += comment + "\r\n";
				}
				if (chartComments.length() > 0) {
					try {
						Files.write(Paths.get("./charts/chart " + answerId + ".txt"), chartComments.getBytes());
					} catch (Exception e) {
						log.error("Error writing ./charts/chart " + answerId + ".txt file.", e);
					}
				}
			});
		}
		
		log.info("createChartsAndComments-->");
	}
	
	private ChartMapObjects createChartAndCommentsMaps(String runType, String pieType) {
		log.info("-->createChartAndCommentsMaps");
		
		Map<Integer, Map<String, Integer>> answerMapMap = new HashMap<Integer, Map<String, Integer>>();
		Map<Integer, List<String>> commentsMap = new HashMap<Integer, List<String>>();
		Map<Integer, String> questionMap = new HashMap<Integer, String>();
		Map<String, Integer> obsCountMap = new HashMap<String, Integer>();
		
		List<Record> records = recordRepo.findObjectsByRecordType("ObserverToolAnswer");
		
		for (Record record : records) {
			try {
				JSONObject recordJson = new JSONObject(record.getRecordJson());
				
				JSONArray questions = recordJson.getJSONArray("questions");
				int count = questions.length();
				for (int i = 0; i < count; i++) {
					JSONObject answer = questions.getJSONObject(i);
					Integer answerId = answer.getInt("id");
					
					if (answer.getString("typeOfQuestion").equalsIgnoreCase("radiobutton")) {
						Map<String, Integer> answerCountMap = answerMapMap.get(answerId);
						if (answerCountMap == null) {
							answerCountMap = new HashMap<String, Integer>();
						}
						String questionString = questionMap.get(answerId);
						if (questionString == null) {
							questionString = answer.getString("name");
							questionMap.put(answerId, questionString);
						}
						
						String answerString = answer.getString("answer");
						Integer answerCount = answerCountMap.get(answerString);
						if (answerCount == null) {
							answerCount = 1;
						} else {
							answerCount++;
						}
						
						List<String> comments = commentsMap.get(answerId);
						if (comments == null) {
							comments = new ArrayList<String>();
						}
						if (!answer.isNull("comment") && answer.getString("comment").length() > 0) {
							comments.add(recordJson.getString("observationTypeName") + ": " + answer.getString("comment"));	
						}
						
						Integer obsCount = obsCountMap.get(recordJson.getString("observationTypeName"));
						if (obsCount == null) {
							obsCount = 1;
						} else {
							obsCount++;
						}
						obsCountMap.put(recordJson.getString("observationTypeName"), obsCount);
						commentsMap.put(answerId, comments);
						
						answerCountMap.put(answerString, answerCount);
						answerMapMap.put(answerId, answerCountMap);
					}
				}
				
			} catch (JSONException e) {
				log.error("Error parsing the record json!", e);
			}
		}
		
		ChartMapObjects returnObjects = new ChartMapObjects(answerMapMap, commentsMap, questionMap, obsCountMap);

		
		log.info("createChartAndCommentsMaps-->");
		return returnObjects;
		
	}
	
	private String getSvgXML(JFreeChart pieChart, int widthOfSVG, int heightOfSVG){
	    SVGGraphics2D svg2d = new SVGGraphics2D(widthOfSVG, heightOfSVG);

	    pieChart.draw(svg2d,new Rectangle2D.Double(0, 0, widthOfSVG, heightOfSVG));

	    return svg2d.getSVGElement();
	}
	
	private void pack(String sourceDirPath, String zipFilePath) throws IOException {
		Files.deleteIfExists(Paths.get(zipFilePath));
	    Path p = Files.createFile(Paths.get(zipFilePath));
	    try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
	        Path pp = Paths.get(sourceDirPath);
	        Files.walk(pp)
	          .filter(path -> !Files.isDirectory(path))
	          .forEach(path -> {
	              ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
	              try {
	                  zs.putNextEntry(zipEntry);
	                  Files.copy(path, zs);
	                  zs.closeEntry();
	            } catch (IOException e) {
	                System.err.println(e);
	            }
	          });
	    }
	}
	
	
	private JFreeChart createPieChart3D(PieDataset dataset) {
        return ChartFactory.createPieChart3D("Pie Chart", dataset);
    }
	
	private void addStartPage(PDDocument document, String headling, String subTitle) {
		PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
		document.addPage(page);
		float height = page.getMediaBox().getHeight();
		float width = page.getMediaBox().getWidth();
		PDPageContentStream contents = null;
		try {
			contents = new PDPageContentStream(document, page);
			PDImageXObject pdImage = PDImageXObject.createFromFile("./config/report/DriverLogo.png",document);
		    contents.drawImage(pdImage, 20, height-180,300,150);
		    pdImage = PDImageXObject.createFromFile("./config/report/ReportOverviewPicture.png",document);
		    contents.drawImage(pdImage, width-510, height-660,450,500);
		    PDStreamUtils.write(contents, headling, PDType1Font.TIMES_BOLD, 22, 30, height-680, new Color(0, 72, 126));
		    PDStreamUtils.write(contents, subTitle, PDType1Font.TIMES_ROMAN, 16, 30, height-705, new Color(243, 179, 41));
		    pdImage = PDImageXObject.createFromFile("./config/report/EU.jpg",document);
		    contents.drawImage(pdImage, 30, 32 , 30, 18);
            PDStreamUtils.write(contents, reportFooter1, PDType1Font.HELVETICA, 8, 75, 50, new Color(102, 102, 102));
            PDStreamUtils.write(contents, reportFooter2, PDType1Font.HELVETICA, 8, 75, 40, new Color(102, 102, 102));
		} catch (Exception e) {
			log.error("Error creating the main page!", e);
		} finally {
			if (contents != null) {
				try {
					contents.close();
				} catch (Exception e) {
					log.error("Error closing the write stream");
				}
			}
		}
	}
	
	private void addOverviewStatistic(PDDocument document, Map<Integer, Map<String, Integer>> answerMapMap, Map<String, Integer> obsCountMap) {
		PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
		document.addPage(page);
		float height = page.getMediaBox().getHeight();
		float width = page.getMediaBox().getWidth();
		int totalCount = 0;
		
		DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
		for (Map.Entry<String, Integer> entry : obsCountMap.entrySet()) {
            dataset.addValue(new Double(entry.getValue()),
            		entry.getKey() + " = " + entry.getValue(),
            		entry.getKey() + " = " + entry.getValue());
			totalCount += entry.getValue();
        }
		JFreeChart chart = ChartFactory.createBarChart(
				"Reports sent by observers",
	            "Observer",
	            "Answers",
	            dataset,
	            PlotOrientation.HORIZONTAL,
	            true, true, false);
		chart.removeLegend();
		
		try {
		    OutputStream out = new FileOutputStream("./charts/obs_count_overview.png");
		    ChartUtils.writeChartAsPNG(out,
		    		chart,
		    		600,
		    		obsCountMap.size()*30);
		    out.close();
		} catch (IOException ex) {
		    log.error(ex);
		}
		
		PDPageContentStream contents = null;
		try {
			contents = new PDPageContentStream(document, page);
			contents.beginText();
		    contents.setNonStrokingColor(0, 72, 126);
		    contents.setFont(PDType1Font.TIMES_BOLD, 20);
		    contents.newLineAtOffset(20, height-40);
		    contents.showText("1.) Overview of recorded Observations");
		    contents.setFont(PDType1Font.TIMES_ROMAN, 16);
		    contents.setNonStrokingColor(0, 0, 0);
		    contents.newLineAtOffset(30, -25);
		    contents.showText(obsCountMap.size() + " Observer reported " + totalCount + " answers.");
		    contents.endText();
		    PDImageXObject pdImage = PDImageXObject.createFromFile("./charts/obs_count_overview.png",document);
		    contents.drawImage(pdImage, 20, height-730, 580, 650);
		    contents.beginText();
		    contents.setFont(PDType1Font.TIMES_ITALIC, 8);
		    contents.setNonStrokingColor(0, 0, 0);
		    contents.newLineAtOffset(250, height-740);
		    contents.showText("Observation answers overview");
		    contents.endText();
			
		} catch (Exception e) {
			log.error("Error creating the main page!", e);
		} finally {
			if (contents != null) {
				try {
					contents.close();
				} catch (Exception e) {
					log.error("Error closing the write stream");
				}
			}
		}
	}
	
	private void addStatisticPage(PDDocument document,
			Map<String, Integer> answerMap,
			List<String> comments,
			String question,
			Integer answerId) {
		PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
		document.addPage(page);
		float height = page.getMediaBox().getHeight();
		float width = page.getMediaBox().getWidth();
		
		DefaultPieDataset dataset = new DefaultPieDataset();
		answerMap.forEach((answwer, count)->{
			dataset.setValue(answwer + " = " + count, new Double(count));
		});
		
		JFreeChart chart = createPieChart3D(dataset);
		chart.setTitle(question);
		chart.removeLegend();
		
		try {
		    OutputStream out = new FileOutputStream("./charts/chart" + answerId + ".png");
		    ChartUtils.writeChartAsPNG(out,
		    		chart,
		    		580,
		    		300);
		    out.close();
		} catch (IOException ex) {
		    log.error(ex);
		}
		
		PDPageContentStream contents = null;
		try {
			contents = new PDPageContentStream(document, page);
			contents.beginText();
		    contents.setNonStrokingColor(0, 72, 126);
		    contents.setFont(PDType1Font.TIMES_BOLD, 20);
		    contents.newLineAtOffset(20, height-40);
		    contents.showText("Question Nr. " + answerId);
		    contents.endText();
		    PDImageXObject pdImage = PDImageXObject.createFromFile("./charts/chart" + answerId + ".png",document);
		    contents.drawImage(pdImage, 20, height-350, 580, 300);
		    contents.beginText();
		    contents.setFont(PDType1Font.TIMES_ITALIC, 8);
		    contents.setNonStrokingColor(0, 0, 0);
		    contents.newLineAtOffset(250, height-360);
		    contents.showText("Answers for Question: " + answerId);
		    
	    	contents.setFont(PDType1Font.TIMES_BOLD_ITALIC, 16);
		    contents.newLineAtOffset(-230, -20);
		    contents.showText("Comments reported by Observers:");
		    if (comments.size() > 0) {
			    contents.setFont(PDType1Font.TIMES_ROMAN, 12);
			    contents.setNonStrokingColor(0, 0, 0);
			    for (String comment : comments) {
			    	contents.newLineAtOffset(0, -17);
			    	try {
			    		contents.showText(java.net.URLDecoder.decode("* " + comment, "UTF-8"));
			    	} catch (Exception e) {
			    		log.error("Error adding the Observer comment.");
			    	}
			    }
		    } else {
		    	contents.setFont(PDType1Font.TIMES_ROMAN, 12);
			    contents.setNonStrokingColor(0, 0, 0);
			    contents.newLineAtOffset(0, -17);
			    contents.showText("No Observer comments for question: " + answerId);
		    }
		    contents.endText();
		} catch (Exception e) {
			log.error("Error creating the specific chart page!", e);
		} finally {
			if (contents != null) {
				try {
					contents.close();
				} catch (Exception e) {
					log.error("Error closing the write stream");
				}
			}
		}
	}
	
	private void addFIEGraphPage(PDDocument document, String packageId,  Category category, int paragrahNumber) {
		PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
		document.addPage(page);
		float height = page.getMediaBox().getHeight();
		PDPageContentStream contentStream = null;
		
		Double blEffAvr = category.getBaselineRating().getEffortAvr();
		Double blResAvr = category.getBaselineRating().getResultAvr();
		Double ilEffAvr = category.getInnovationRating().getEffortAvr();
		Double ilResAvr = category.getInnovationRating().getResultAvr();
		
		
		
		try {
			contentStream = new PDPageContentStream(document, page);
			float endY = MultiLine.drawMultiLineText(paragrahNumber + ".) " + 
					CategoryMapper.getInstance().getHeadingforCategory(packageId, category.getCategoryId()),
					20, height-40, 500, page, contentStream, PDType1Font.TIMES_BOLD, 16, new Color(0, 72, 126));
			
			endY = MultiLine.drawMultiLineText(paragrahNumber + ".1) Following question identifying Effort & Result for Baseline has be asked:",
					30, endY-20, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 12, new Color(0, 72, 126));
			endY = MultiLine.drawMultiLineText(category.getBaselineEffortQuestion(),
					40, endY-5, 490, page, contentStream, PDType1Font.TIMES_ROMAN, 12, new Color(0, 0, 0));
			endY = MultiLine.drawMultiLineText(category.getBaselineResultQuestion(),
					40, endY-5, 490, page, contentStream, PDType1Font.TIMES_ROMAN, 12, new Color(0, 0, 0));
			
			endY = MultiLine.drawMultiLineText(paragrahNumber + ".2) Following question identifying Effort & Result for Innovationline has be asked:",
					30, endY-10, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 12, new Color(0, 72, 126));
			endY = MultiLine.drawMultiLineText(category.getInnovationlineEffortQuestion(),
					40, endY-5, 490, page, contentStream, PDType1Font.TIMES_ROMAN, 12, new Color(0, 0, 0));
			endY = MultiLine.drawMultiLineText(category.getInnovationlineResultQuestion(),
					40, endY-5, 490, page, contentStream, PDType1Font.TIMES_ROMAN, 12, new Color(0, 0, 0));
			
			// the chart
			endY = MultiLine.drawMultiLineText(paragrahNumber + ".3) Graph:",
					30, endY-15, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 12, new Color(0, 72, 126));
			
			
			try {
				JFreeChart xylineChart = ChartFactory.createXYLineChart(
				         "Change Graph" ,
				         "Effort" ,
				         "Result" ,
				         createCategoryDataset(category) ,
				         PlotOrientation.VERTICAL ,
				         false , false , false);
				
				ChartPanel chartPanel = new ChartPanel( xylineChart );
				chartPanel.setPreferredSize( new java.awt.Dimension( 420 , 250 ) );
				XYPlot plot = xylineChart.getXYPlot( );
				NumberAxis numberaxis = ( NumberAxis )plot.getDomainAxis( );                 
			    numberaxis.setLowerBound(0.0);
			    numberaxis.setUpperBound(10.0);
			    NumberAxis numberaxis1 = ( NumberAxis )plot.getRangeAxis( );                 
			    numberaxis1.setLowerBound(0.0);                 
			    numberaxis1.setUpperBound(10.0);
		      
				XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
				renderer.setSeriesShape(0, new Ellipse2D.Double(-5D, -5D, 10D, 10D));
				renderer.setSeriesPaint(0, new Color(255, 178, 102) );
				renderer.setSeriesStroke(0, new BasicStroke( 4.0f ) );

				renderer.setSeriesShape(1, new Ellipse2D.Double(-5D, -5D, 10D, 10D));
				renderer.setSeriesShape(2, new Ellipse2D.Double(-5D, -5D, 10D, 10D));
				renderer.setSeriesShapesVisible(2, false);
				renderer.setSeriesStroke( 1 , new BasicStroke( 4.0f ) );
				renderer.setSeriesStroke( 2 , new BasicStroke( 3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
				
				// check the change
				if ((blEffAvr > ilEffAvr && blResAvr <= ilResAvr) || (blEffAvr >= ilEffAvr && blResAvr < ilResAvr)) {
					renderer.setSeriesPaint( 1 , new Color(0, 102, 0) );
					renderer.setSeriesPaint( 2 , new Color(0, 102, 0) );
				} else if (blEffAvr < ilEffAvr && blResAvr < ilResAvr) {
					renderer.setSeriesPaint( 1 , new Color(160, 160, 160) );
					renderer.setSeriesPaint( 2 , new Color(160, 160, 160) );
				} else {
					renderer.setSeriesPaint( 1 , new Color(204, 0, 0) );
					renderer.setSeriesPaint( 2 , new Color(204, 0, 0) );
				}
				
				plot.setRenderer( renderer );
				try {
				    OutputStream out = new FileOutputStream("./charts/graph.png");
				    ChartUtils.writeChartAsPNG(out,
				    		xylineChart,
				    		470,
				    		250);
				    out.close();
				} catch (IOException ex) {
				    log.error(ex);
				}
			
				PDImageXObject pdImage = PDImageXObject.createFromFile("./charts/graph.png",document);
				contentStream.drawImage(pdImage, 20, endY-260, 470, 250);
			} catch (Exception e) {
				log.error("Error creating the graph!", e);
			}
			endY-=260;
			
			// Detailed result
			endY = MultiLine.drawMultiLineText(paragrahNumber + ".4) The Result in Details:",
					30, endY-10, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 12, new Color(0, 72, 126));
			
			endY = MultiLine.drawMultiLineText("Baseline:",
					40, endY-5, 480, page, contentStream, PDType1Font.TIMES_ITALIC, 10, new Color(0, 0, 0));
			endY = MultiLine.drawMultiLineText("Nr of received answers for rating the Effort: " + 
					category.getBaselineRating().getEffortRating().size() + 
					" which results in a average of: " + category.getBaselineRating().getEffortAvr() + 
					" (variance: " + FORMATTER.format(category.getBaselineRating().getEffortVariance()) + ")",
					50, endY-5, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 10, new Color(0, 0, 0));
			endY = MultiLine.drawMultiLineText("Nr of received answers for rating the Result: " + 
					category.getBaselineRating().getResultRating().size() + 
					" which results in a average of: " + category.getBaselineRating().getResultAvr() + 
					" (variance: " + FORMATTER.format(category.getBaselineRating().getResultVariance()) + ")",
					50, endY-5, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 10, new Color(0, 0, 0));
			
			endY = MultiLine.drawMultiLineText("Innovationline:",
					40, endY-5, 480, page, contentStream, PDType1Font.TIMES_ITALIC, 10, new Color(0, 0, 0));
			endY = MultiLine.drawMultiLineText("Nr of received answers for rating the Effort: " + 
					category.getInnovationRating().getEffortRating().size() + 
					" which results in a average of: " + category.getInnovationRating().getEffortAvr() + 
					" (variance: " + FORMATTER.format(category.getInnovationRating().getEffortVariance()) + ")",
					50, endY-5, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 10, new Color(0, 0, 0));
			if (blEffAvr == ilEffAvr) {
				endY = MultiLine.drawMultiLineText("-> same average of Effort: " + blEffAvr,
						50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 10, new Color(160, 160, 160));
			} else if (blEffAvr > ilEffAvr) {
				endY = MultiLine.drawMultiLineText("-> average decrease of Effort by: " + (blEffAvr-ilEffAvr),
						50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 10, new Color(0, 102, 0));
			} else {
				endY = MultiLine.drawMultiLineText("-> average increase of Effort by: " + (ilEffAvr-blEffAvr),
						50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 10, new Color(204, 0, 0));
			}
			endY = MultiLine.drawMultiLineText("Nr of received answers for rating the Result: " + 
					category.getInnovationRating().getResultRating().size() + 
					" which results in a average of: " + category.getInnovationRating().getResultAvr() + 
					" (variance: " + FORMATTER.format(category.getInnovationRating().getResultVariance()) + ")",
					50, endY-5, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 10, new Color(0, 0, 0));
			if (ilResAvr == blResAvr) {
				endY = MultiLine.drawMultiLineText("-> same average of Result: " + ilResAvr,
						50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 10, new Color(160, 160, 160));
			} else if (ilResAvr > blResAvr) {
				endY = MultiLine.drawMultiLineText("-> average increase of Result by: " + (ilResAvr-blResAvr),
						50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 10, new Color(0, 102, 0));
			} else {
				endY = MultiLine.drawMultiLineText("-> average decrease of Result by: " + (blResAvr-ilResAvr),
						50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 10, new Color(204, 0, 0));
			}
			
			// comments
			endY = MultiLine.drawMultiLineText(paragrahNumber + ".5) Following comments have been reported for Baseline:",
					30, endY-20, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 12, new Color(0, 0, 0));
			for (String comment : category.getBaselineRating().getComments()) {
				endY = MultiLine.drawMultiLineText("* " +comment,
						40, endY-5, 480, page, contentStream, PDType1Font.TIMES_ITALIC, 10, new Color(0, 0, 0));
				page = this.getPage(document, page, 60, endY);
			}
			
			endY = MultiLine.drawMultiLineText(paragrahNumber + ".6) Following comments have been reported for Innovationline:",
					30, endY-20, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 12, new Color(0, 0, 0));
			for (String comment : category.getInnovationRating().getComments()) {
				endY = MultiLine.drawMultiLineText("* " +comment,
						40, endY-5, 480, page, contentStream, PDType1Font.TIMES_ITALIC, 10, new Color(0, 0, 0));
				page = this.getPage(document, page, 60, endY);
			}
		} catch (Exception e) {
			log.error("Error creating the addFIEGraphPage!", e);
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
	
	private void addFooter(PDDocument document) throws IOException {
        int numberOfPages = document.getNumberOfPages();
        for (int i = 1; i < numberOfPages; i++) {
        	PDPage fpage = document.getPage(i);
            PDPageContentStream contentStream = new PDPageContentStream(document, fpage, AppendMode.APPEND, true);
            
            contentStream.setStrokingColor(0, 73, 126);
            contentStream.moveTo(0, 30);
            contentStream.lineTo(470, 30);
            contentStream.stroke();
            
            PDStreamUtils.write(contentStream, "Page " + (i+1) + " of " + (numberOfPages), PDType1Font.HELVETICA_BOLD, 8, 500, 34, new Color(0, 73, 126));
            contentStream.close();
        }
    }
	
	private Integer reversRating(Integer rating) {
		Integer revers = 10;
		
		revers = 11 - rating;
		
		return revers;
	}
	
	private PDPage getPage(PDDocument document, PDPage currentPage, float minHeight, float currentY) {
		if (currentY <= minHeight) {
			return new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
		} 
		return currentPage;
	}
	
	private XYDataset createCategoryDataset(Category category ) {
	      final XYSeries base = new XYSeries( "Baseline" );          
	      base.add( category.getBaselineRating().getEffortAvr() , category.getBaselineRating().getResultAvr() );          
	      
	      final XYSeries innovation = new XYSeries( "Innovationline" );          
	      innovation.add( category.getInnovationRating().getEffortAvr() , category.getInnovationRating().getResultAvr());
	      
	      final XYSeries change = new XYSeries( "Change" ); 
	      change.add( category.getBaselineRating().getEffortAvr() , category.getBaselineRating().getResultAvr() );    
	      change.add( category.getInnovationRating().getEffortAvr() , category.getInnovationRating().getResultAvr());
	      
	      final XYSeriesCollection dataset = new XYSeriesCollection( );          
	      dataset.addSeries( base );          
	      dataset.addSeries( innovation );  
	      dataset.addSeries( change );    
	      return dataset;
	   }

}
