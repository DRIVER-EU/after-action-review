package eu.driver.aar.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
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
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
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

import be.quodlibet.boxable.utils.PDStreamUtils;
import eu.driver.aar.service.constants.AARConstants;
import eu.driver.aar.service.constants.CategoryMapper;
import eu.driver.aar.service.controller.page.FIEOverviewPage;
import eu.driver.aar.service.dto.Record;
import eu.driver.aar.service.dto.Trial;
import eu.driver.aar.service.objects.ChartMapObjects;
import eu.driver.aar.service.objects.fie.Category;
import eu.driver.aar.service.objects.fie.Package;
import eu.driver.aar.service.objects.fie.Rating;
import eu.driver.aar.service.repository.RecordRepository;
import eu.driver.aar.service.repository.TrialRepository;
import eu.driver.aar.service.utils.QuestionInstance;
import eu.driver.aar.service.utils.pdf.MultiLine;
import eu.driver.aar.service.utils.pdf.XYVectorizedRenderer;

@RestController
public class StatisticRESTController {
	
	private static final String DEFAULT_FORMAT = "0.###";
    private static final NumberFormat FORMATTER = new DecimalFormat(DEFAULT_FORMAT);
    
    private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private final String reportFooter1 = "This project has received funding from the "
			+ "European Union’s 7th Framework Programme for Research, Technological "
			+ "Development and";
	private final String reportFooter2 = "Demonstration under Grant Agreement (GA) N° #607798";
	
	private QuestionInstance questioninstance = QuestionInstance.getInstance();
	
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
		this.addOverviewStatistic(document, answerMapMap, obsCountMap, 1);
		
		int paragrahNumber = 2;
		for (Map.Entry<Integer, Map<String, Integer>> entry : answerMapMap.entrySet()) {
			this.addStatisticPage(document, entry.getValue(), commentsMap.get(entry.getKey()), questionMap.get(entry.getKey()), entry.getKey(), paragrahNumber);
			paragrahNumber++;
		};
		
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
				
				Long sendWhen = recordJson.getLong("timeWhen");
				Date sendDateWhen = new Date(sendWhen);
				String key = format.format(sendDateWhen);
				
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
								// get the question from QuestionInstance
								String question = questioninstance.getQuestion(marker);
								if (question == null) {
									question = questionString;
								}
								String packgeId = tokens.nextToken();
								String runType = tokens.nextToken();
								String axis = tokens.nextToken();
								String categoryId = tokens.nextToken();
								
								String answerStr = answer.getString("answer");
								int minusIdx = answerStr.indexOf("-");
								if (minusIdx != -1) {
									answerStr = answerStr.substring(0, minusIdx);
								}
								Integer rating = Integer.parseInt(answerStr);
								
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
										if (category.getBaselineEffortQuestion().equalsIgnoreCase("")) {
											category.setBaselineEffortQuestion(question);
										}
									} else {
										catRating.addResultRating(this.reversRating(rating));
										if (category.getBaselineResultQuestion().equalsIgnoreCase("")) {
											category.setBaselineResultQuestion(question);
										}
									}
								} else {
									catRating = category.getInnovationRating(key);
									if (axis.equalsIgnoreCase("E")) {
										catRating.addEffortRating(rating);
										if (category.getInnovationlineEffortQuestion().equalsIgnoreCase("")) {
											category.setInnovationlineEffortQuestion(question);
										}
									} else {
										catRating.addResultRating(this.reversRating(rating));
										if (category.getInnovationlineResultQuestion().equalsIgnoreCase("")) {
											category.setInnovationlineResultQuestion(question);
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
		pckg = packageMap.get("Q2");
		if (pckg != null) {
			for (Map.Entry<String, Category> entry : pckg.getPackageCategoryMap().entrySet()) {
				this.addFIEGraphPage(document, pckg.getPackageId(), entry.getValue(), paragrahNumber);
				paragrahNumber++;
			};
		}
		pckg = packageMap.get("Q3");
		if (pckg != null) {
			for (Map.Entry<String, Category> entry : pckg.getPackageCategoryMap().entrySet()) {
				this.addFIEGraphPage(document, pckg.getPackageId(), entry.getValue(), paragrahNumber);
				paragrahNumber++;
			};
		}
		pckg = packageMap.get("Q4");
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
	
	@ApiOperation(value = "createOverviewFIEPDFReport", nickname = "createOverviewFIEPDFReport")
	@RequestMapping(value = "/AARService/createOverviewFIEPDFReport", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = byte[].class),
			@ApiResponse(code = 400, message = "Bad Request", response = byte[].class),
			@ApiResponse(code = 500, message = "Failure", response = byte[].class) })
	public ResponseEntity<byte[]> createOverviewFIEPDFReport() {
		log.info("-->createOverviewFIEPDFReport");
		String docName = "./overviewFIE.pdf";
		byte[] fileContent = null;
		if(Files.exists(Paths.get(docName))) {
			try {
				Files.delete(Paths.get(docName));
			} catch (IOException e) {
				log.error("Error delete the old overviewFIE.pdf file!", e);
				fileContent = "Could not delete the old overviewFIE file, new report was not generated!".getBytes();
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.TEXT_HTML);
			    log.info("createOverviewFIEPDFReport-->");
			    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		
		try {
			this.cleanUpChartDirectoy();
		} catch (Exception e) {
			fileContent = e.getMessage().getBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_HTML);
		    log.info("createOverviewFIEPDFReport-->");
		    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		Map<String, Package> packageMap = new HashMap<String, Package>();
		
		Trial trial = trialRepo.findActualTrial();
		if (trial == null) {
			fileContent = "Could not load the actual trial information!".getBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_HTML);
		    log.info("createOverviewFIEPDFReport-->");
		    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		List<Record> records = recordRepo.findObjectsByRecordTypeAndRunType("ObserverToolAnswer", AARConstants.RECORD_RUN_TYPE_FIE);
		for (Record record : records) {
			try {
				JSONObject recordJson = new JSONObject(record.getRecordJson());
				
				Long sendWhen = recordJson.getLong("timeWhen");
				Date sendDateWhen = new Date(sendWhen);
				String key = format.format(sendDateWhen);
				
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
								// get the question from QuestionInstance
								String question = questioninstance.getQuestion(marker);
								if (question == null) {
									question = questionString;
								}
								String packgeId = tokens.nextToken();
								String runType = tokens.nextToken();
								String axis = tokens.nextToken();
								String categoryId = tokens.nextToken();
								
								String answerStr = answer.getString("answer");
								int minusIdx = answerStr.indexOf("-");
								if (minusIdx != -1) {
									answerStr = answerStr.substring(0, minusIdx);
								}
								Integer rating = Integer.parseInt(answerStr);
								
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
										if (category.getBaselineEffortQuestion().equalsIgnoreCase("")) {
											category.setBaselineEffortQuestion(question);
										}
									} else {
										catRating.addResultRating(this.reversRating(rating));
										if (category.getBaselineResultQuestion().equalsIgnoreCase("")) {
											category.setBaselineResultQuestion(question);
										}
									}
								} else {
									catRating = category.getInnovationRating(key);
									if (axis.equalsIgnoreCase("E")) {
										catRating.addEffortRating(rating);
										if (category.getInnovationlineEffortQuestion().equalsIgnoreCase("")) {
											category.setInnovationlineEffortQuestion(question);
										}
									} else {
										catRating.addResultRating(this.reversRating(rating));
										if (category.getInnovationlineResultQuestion().equalsIgnoreCase("")) {
											category.setInnovationlineResultQuestion(question);
										}
									}
								}
								
								if (!answer.isNull("comment") && answer.getString("comment").length() > 0) {
									catRating.addComment(recordJson.getString("observationTypeName") + ": " + answer.getString("comment"));	
								}
								
								packageMap.put(packgeId, pckg);
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("Error parsing the answer!", e);
			}
		}
		
		PDDocument document = new PDDocument();
		this.addStartPage(document, trial.getTrialName(), "Generated First Impression Evaluation Overview Report");
		FIEOverviewPage overviewFIEPageGen = new FIEOverviewPage();
		Package pckg = packageMap.get("Q1");
		int paragrahNumber = 1;
		if (pckg != null) {
			overviewFIEPageGen.addFIEOverviewGraphPage(document, pckg.getPackageId(), pckg, paragrahNumber);
			/*
			for (Map.Entry<String, Category> entry : pckg.getPackageCategoryMap().entrySet()) {
				this.addFIEGraphPage(document, pckg.getPackageId(), entry.getValue(), paragrahNumber);
				paragrahNumber++;
			};*/
		}
		pckg = packageMap.get("Q2");
		if (pckg != null) {
			overviewFIEPageGen.addFIEOverviewGraphPage(document, pckg.getPackageId(), pckg, paragrahNumber);
			/*for (Map.Entry<String, Category> entry : pckg.getPackageCategoryMap().entrySet()) {
				this.addFIEGraphPage(document, pckg.getPackageId(), entry.getValue(), paragrahNumber);
				paragrahNumber++;
			};*/
		}
		pckg = packageMap.get("Q3");
		if (pckg != null) {
			overviewFIEPageGen.addFIEOverviewGraphPage(document, pckg.getPackageId(), pckg, paragrahNumber);
			/*for (Map.Entry<String, Category> entry : pckg.getPackageCategoryMap().entrySet()) {
				this.addFIEGraphPage(document, pckg.getPackageId(), entry.getValue(), paragrahNumber);
				paragrahNumber++;
			};*/
		}
		pckg = packageMap.get("Q4");
		if (pckg != null) {
			overviewFIEPageGen.addFIEOverviewGraphPage(document, pckg.getPackageId(), pckg, paragrahNumber);
			/*for (Map.Entry<String, Category> entry : pckg.getPackageCategoryMap().entrySet()) {
				this.addFIEGraphPage(document, pckg.getPackageId(), entry.getValue(), paragrahNumber);
				paragrahNumber++;
			};*/
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
		    log.info("createOverviewFIEPDFReport-->");
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
		    log.info("createOverviewFIEPDFReport-->");
		    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("attachment", "overviewFIE.pdf"); 
	    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
	    log.info("createFIEcreateOverviewFIEPDFReportPDFReport-->");
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
		//Map<Integer, String> questionMap = returnObjects.getQuestionMap();
		
		if (returnObjects != null) {
			answerMapMap.forEach((answerId,answerCountMap)->{
				JFreeChart chart = null;
				if (pieType.equalsIgnoreCase("PIE")) {
					DefaultPieDataset dataset = new DefaultPieDataset();
					answerCountMap.forEach((question, count)->{
						dataset.setValue(question + " = " + count, new Double(count));
					});
					
					chart = createPieChart3D(dataset);
					chart.setTitle("Answer statistic for: " + answerId);
				} else {
					DefaultCategoryDataset dataset = new DefaultCategoryDataset( );  
					answerCountMap.forEach((question, count)->{
						dataset.addValue(new Double(count), question + " = " + count, question + " = " + count);
					});
					chart = ChartFactory.createBarChart(
							"Answer statistic for: " + answerId,
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
		
		List<Record> records = null;
		if (runType == null) {
			records = recordRepo.findObjectsByRecordType("ObserverToolAnswer");	
		} else {
			records = recordRepo.findObjectsByRecordTypeAndRunType("ObserverToolAnswer", runType);
		}
		
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
	
	private void addOverviewStatistic(PDDocument document, 
			Map<Integer, Map<String, Integer>> answerMapMap, Map<String, Integer> obsCountMap, 
			int paragrahNumber) {
		PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
		document.addPage(page);
		float height = page.getMediaBox().getHeight();
		//float width = page.getMediaBox().getWidth();
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
		    if (obsCountMap.size() >= 1) {
				OutputStream out = new FileOutputStream("./charts/obs_count_overview.png");
			    ChartUtils.writeChartAsPNG(out,
			    		chart,
			    		600,
			    		obsCountMap.size()*30);
			    out.close();
		    }
		} catch (IOException ex) {
		    log.error(ex);
		}
		
		PDPageContentStream contents = null;
		try {
			contents = new PDPageContentStream(document, page);
			float endY = MultiLine.drawMultiLineText(paragrahNumber + ") Overview of recorded Observations",
					20, height-40, 500, page, contents, PDType1Font.TIMES_BOLD, 20, new Color(0, 72, 126));
			endY = MultiLine.drawMultiLineText(obsCountMap.size() + " Observer reported " + totalCount + " answers.",
					30, endY-5, 500, page, contents, PDType1Font.TIMES_ROMAN, 16, new Color(0, 0, 0));
			if (obsCountMap.size() >= 1) {
				PDImageXObject pdImage = PDImageXObject.createFromFile("./charts/obs_count_overview.png",document);
				contents.drawImage(pdImage, 20, endY-655, 520, 650);
			}
		    
		    endY = MultiLine.drawMultiLineText("Observation answers overview",
					250, endY-660, 500, page, contents, PDType1Font.TIMES_ITALIC, 8, new Color(0, 0, 0));
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
			Integer answerId, int paragrahNumber) {
		PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
		document.addPage(page);
		float height = page.getMediaBox().getHeight();
		//float width = page.getMediaBox().getWidth();
		
		DefaultPieDataset dataset = new DefaultPieDataset();
		answerMap.forEach((answwer, count)->{
			dataset.setValue(answwer + " = " + count, new Double(count));
		});
		
		JFreeChart chart = createPieChart3D(dataset);
		chart.setTitle("Answer statistic for: " + answerId);
		//chart.setTitle(question);
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
			float endY = MultiLine.drawMultiLineText(paragrahNumber + ") Question Nr. " + answerId + ": " + question,
					20, height-40, 500, page, contents, PDType1Font.TIMES_BOLD, 20, new Color(0, 72, 126));
			
		    PDImageXObject pdImage = PDImageXObject.createFromFile("./charts/chart" + answerId + ".png",document);
		    contents.drawImage(pdImage, 20, endY-310, 520, 300);

		    endY = MultiLine.drawMultiLineText("Answers for Question: " + answerId,
					250, endY-315, 500, page, contents, PDType1Font.TIMES_ITALIC, 8, new Color(0, 0, 0));
		    
		    endY = MultiLine.drawMultiLineText("Comments reported by Observers:",
					30, endY-10, 500, page, contents, PDType1Font.TIMES_BOLD_ITALIC, 16, new Color(0, 0, 0));
		    if (comments.size() > 0) {
			    for (String comment : comments) {
			    	try {
			    		endY = MultiLine.drawMultiLineText("* " + comment,
								40, endY-5, 500, page, contents, PDType1Font.TIMES_ROMAN, 12, new Color(0, 0, 0));
			    	} catch (Exception e) {
			    		log.error("Error adding the Observer comment.");
			    	}
			    }
		    } else {
		    	endY = MultiLine.drawMultiLineText("No Observer comments for question: " + answerId,
						40, endY-5, 500, page, contents, PDType1Font.TIMES_ROMAN, 12, new Color(0, 0, 0));
		    }
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
		
		try {
			contentStream = new PDPageContentStream(document, page);
			float endY = MultiLine.drawMultiLineText(paragrahNumber + ") " + 
					CategoryMapper.getInstance().getHeadingforCategory(packageId, category.getCategoryId()),
					20, height-40, 500, page, contentStream, PDType1Font.TIMES_BOLD, 14, new Color(0, 72, 126));
			
			endY = MultiLine.drawMultiLineText(paragrahNumber + ".1) Following question identifying Effort & Result for Baseline has be asked:",
					30, endY-20, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 12, new Color(0, 72, 126));
			endY = MultiLine.drawMultiLineText(category.getBaselineEffortQuestion(),
					40, endY-5, 490, page, contentStream, PDType1Font.TIMES_ROMAN, 10, new Color(0, 0, 0));
			endY = MultiLine.drawMultiLineText(category.getBaselineResultQuestion(),
					40, endY-5, 490, page, contentStream, PDType1Font.TIMES_ROMAN, 10, new Color(0, 0, 0));
			
			endY = MultiLine.drawMultiLineText(paragrahNumber + ".2) Following question identifying Effort & Result for Innovationline has be asked:",
					30, endY-10, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 12, new Color(0, 72, 126));
			endY = MultiLine.drawMultiLineText(category.getInnovationlineEffortQuestion(),
					40, endY-5, 490, page, contentStream, PDType1Font.TIMES_ROMAN, 10, new Color(0, 0, 0));
			endY = MultiLine.drawMultiLineText(category.getInnovationlineResultQuestion(),
					40, endY-5, 490, page, contentStream, PDType1Font.TIMES_ROMAN, 10, new Color(0, 0, 0));
			
			// the chart
			endY = MultiLine.drawMultiLineText(paragrahNumber + ".3) Graph:",
					30, endY-15, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 12, new Color(0, 72, 126));
			
			Map<String, Rating> treeMap = new TreeMap<>(category.getInnovationRatings());
			
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
		      
			    XYVectorizedRenderer renderer = new XYVectorizedRenderer(false, true );
			    // series 0 baseline point
				renderer.setSeriesShape(0, new Ellipse2D.Double(-7D, -7D, 14D, 14D));
				renderer.setSeriesPaint(0, new Color(255, 178, 102) );
				renderer.setSeriesStroke(0, new BasicStroke( 4.0f ) );
				//renderer.setSeriesLinesVisible(0, false);
				
				// series i = innovation point
				// series i+1 = change from baseline to innovationline
				int seriesCount = 1;				
				// check the change
				
				for (String key : treeMap.keySet()) {
					Rating rating = treeMap.get(key);
					Color color = null;
					if (blEffAvr.compareTo(rating.getEffortAvr()) >= 0 && blResAvr.compareTo(rating.getResultAvr()) <= 0) {
						color = new Color(0, 102, 0);
					} else if (blEffAvr.compareTo(rating.getEffortAvr()) < 0 && blResAvr.compareTo(rating.getResultAvr()) < 0) {
						/* check for 45°
						 * < 45° red, = 45° grey & > 45° green
						*/
						if ((rating.getEffortAvr().doubleValue() - blEffAvr.doubleValue()) == (rating.getResultAvr().doubleValue()-blResAvr.doubleValue())) { //45°
							color = new Color(160, 160, 160);
						} else if ((rating.getEffortAvr().doubleValue() - blEffAvr.doubleValue()) > (rating.getResultAvr().doubleValue()-blResAvr.doubleValue())) { // < 45°
							color = new Color(204, 0, 0);
						} else {
							color = new Color(0, 102, 0) ;
						}
					} else if (blEffAvr.compareTo(rating.getEffortAvr()) > 0 && blResAvr.compareTo(rating.getResultAvr()) > 0) {
						/* check for -45°
						 * > -45° red, = -45° grey & < -45° green
						*/
						
						if ((blEffAvr.doubleValue() - rating.getEffortAvr().doubleValue()) == (blResAvr.doubleValue()-rating.getResultAvr().doubleValue())) { //45°
							color = new Color(160, 160, 160);
						} else if ((blEffAvr.doubleValue() - rating.getEffortAvr().doubleValue()) < (blResAvr.doubleValue()-rating.getResultAvr().doubleValue())) { // < -45°
							color = new Color(204, 0, 0);
						} else {
							color = new Color(0, 102, 0);
						}
					} else {
						color = new Color(204, 0, 0);
					}
					if (seriesCount == 1) {
						renderer.setSeriesShape(seriesCount, new Ellipse2D.Double(-5D, -5D, 10D, 10D));
						renderer.setSeriesShape(seriesCount+1, new Ellipse2D.Double(-5D, -5D, 10D, 10D));
					} else {
						renderer.setSeriesShape(seriesCount, new Rectangle2D.Double(-5D, -5D, 10D, 10D));
						renderer.setSeriesShape(seriesCount+1, new Rectangle2D.Double(-5D, -5D, 10D, 10D));
					}
					renderer.setSeriesLinesVisible(seriesCount+1, true);
					renderer.setSeriesShapesVisible(seriesCount+1, false);
					//renderer.setSeriesStroke( seriesCount , new BasicStroke( 2.0f ) );
					renderer.setSeriesStroke( seriesCount+1 , new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
					
					renderer.setSeriesPaint( seriesCount , color );
					renderer.setSeriesPaint( seriesCount+1 , color );
					seriesCount+=2;
				}
				
				plot.setRenderer( renderer );
				LegendTitle legend = new LegendTitle(plot.getRenderer());
				Font font = new Font("Arial",0,10);
				legend.setItemFont(font);
				legend.setPosition(RectangleEdge.BOTTOM);
				xylineChart.addLegend(legend);
				
				try {
				    OutputStream out = new FileOutputStream("./charts/graph.png");
				    ChartUtils.writeChartAsPNG(out,
				    		xylineChart,
				    		350,
				    		350);
				    out.close();
				} catch (IOException ex) {
				    log.error(ex);
				}
			
				PDImageXObject pdImage = PDImageXObject.createFromFile("./charts/graph.png",document);
				contentStream.drawImage(pdImage, 40, endY-360,350, 350);
			} catch (Exception e) {
				log.error("Error creating the graph!", e);
			}
			endY-=360;
			
			// Detailed result
			endY = MultiLine.drawMultiLineText(paragrahNumber + ".4) The Result in Details:",
					30, endY-10, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 10, new Color(0, 72, 126));
			
			endY = MultiLine.drawMultiLineText("Baseline:",
					40, endY-5, 480, page, contentStream, PDType1Font.TIMES_ITALIC, 8, new Color(0, 0, 0));
			endY = MultiLine.drawMultiLineText("Nr of received answers for rating the Effort: " + 
					category.getBaselineRating().getEffortRating().size() + 
					" which results in a average of: " + category.getBaselineRating().getEffortAvr() + 
					" (stand. deviation: " + FORMATTER.format(category.getBaselineRating().getEffortStdDev()) + ")",
					50, endY-5, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(0, 0, 0));
			endY = MultiLine.drawMultiLineText("Nr of received answers for rating the Result: " + 
					category.getBaselineRating().getResultRating().size() + 
					" which results in a average of: " + category.getBaselineRating().getResultAvr() + 
					" (stand. deviation: " + FORMATTER.format(category.getBaselineRating().getResultStdDev()) + ")",
					50, endY-5, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(0, 0, 0));
			
			for (String key : treeMap.keySet()) {
				try {
					Rating rating = treeMap.get(key);
					endY = MultiLine.drawMultiLineText("Innovationline: " + key,
							40, endY-5, 480, page, contentStream, PDType1Font.TIMES_ITALIC, 10, new Color(0, 0, 0));
					endY = MultiLine.drawMultiLineText("Nr of received answers for rating the Effort: " + 
							rating.getEffortRating().size() + 
							" which results in a average of: " + rating.getEffortAvr() + 
							" (stand. deviation: " + FORMATTER.format(rating.getEffortStdDev()) + ")",
							50, endY-5, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(0, 0, 0));
					if (blEffAvr.compareTo(rating.getEffortAvr()) == 0) {
						endY = MultiLine.drawMultiLineText("-> same average of Effort: " + blEffAvr,
								50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(160, 160, 160));
					} else if (blEffAvr.compareTo(rating.getEffortAvr()) >= 0) {
						endY = MultiLine.drawMultiLineText("-> average decrease of Effort by: " + (blEffAvr-rating.getEffortAvr()),
								50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(0, 102, 0));
					} else {
						endY = MultiLine.drawMultiLineText("-> average increase of Effort by: " + (rating.getEffortAvr()-blEffAvr),
								50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(204, 0, 0));
					}
					endY = MultiLine.drawMultiLineText("Nr of received answers for rating the Result: " + 
							rating.getResultRating().size() + 
							" which results in a average of: " + rating.getResultAvr() + 
							" (stand. deviation: " + FORMATTER.format(rating.getResultStdDev()) + ")",
							50, endY-5, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(0, 0, 0));
					if (rating.getResultAvr().compareTo(blResAvr) == 0) {
						endY = MultiLine.drawMultiLineText("-> same average of Result: " + rating.getResultAvr(),
								50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(160, 160, 160));
					} else if (rating.getResultAvr().compareTo(blResAvr) > 0) {
						endY = MultiLine.drawMultiLineText("-> average increase of Result by: " + (rating.getResultAvr()-blResAvr),
								50, endY-1, 480, page, contentStream, PDType1Font.TIMES_ROMAN, 8, new Color(0, 102, 0));
					} else {
						endY = MultiLine.drawMultiLineText("-> average decrease of Result by: " + (blResAvr-rating.getResultAvr()),
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
				if (contentStream != null) {
					try {
						contentStream.close();
					} catch (Exception e) {
						log.error("Error closing the write stream");
					}
				}
				page = new PDPage(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
				document.addPage(page);
				height = page.getMediaBox().getHeight();
				contentStream = new PDPageContentStream(document, page);
				
				// comments
				endY = MultiLine.drawMultiLineText(paragrahNumber + ".5) Following comments have been reported for Baseline:",
						30, height-40, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 10, new Color(0, 0, 0));
				for (String comment : category.getBaselineRating().getComments()) {
					endY = MultiLine.drawMultiLineText("* " +comment,
							40, endY-5, 480, page, contentStream, PDType1Font.TIMES_ITALIC, 8, new Color(0, 0, 0));
				}
				
				endY = MultiLine.drawMultiLineText(paragrahNumber + ".6) Following comments have been reported for Innovationline:",
						30, endY-20, 490, page, contentStream, PDType1Font.TIMES_BOLD_ITALIC, 10, new Color(0, 0, 0));
				for (String key : treeMap.keySet()) {
					Rating rating = treeMap.get(key);
					for (String comment : rating.getComments()) {
						endY = MultiLine.drawMultiLineText("* " +comment,
								40, endY-5, 480, page, contentStream, PDType1Font.TIMES_ITALIC, 8, new Color(0, 0, 0));
					}	
				};
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
        	float widh = fpage.getMediaBox().getWidth();
            PDPageContentStream contentStream = new PDPageContentStream(document, fpage, AppendMode.APPEND, true);
            
            contentStream.setStrokingColor(0, 73, 126);
            contentStream.moveTo(0, 30);
            contentStream.lineTo(widh - 110, 30);
            contentStream.stroke();
            
            PDStreamUtils.write(contentStream, "Page " + (i+1) + " of " + (numberOfPages), PDType1Font.HELVETICA_BOLD, 8, widh-80, 34, new Color(0, 73, 126));
            contentStream.close();
        }
    }
	
	private Integer reversRating(Integer rating) {
		Integer revers = 10;
		
		revers = 11 - rating;
		
		return revers;
	}
	
	private XYDataset createCategoryDataset(Category category ) {
		final XYSeriesCollection dataset = new XYSeriesCollection( ); 
		Map<String, Rating> treeMap = new TreeMap<>(category.getInnovationRatings());
		
	    XYSeries base = new XYSeries( "Baseline", false );
	    base.add( category.getBaselineRating().getEffortAvr() , category.getBaselineRating().getResultAvr() );
	    dataset.addSeries( base ); 
	    
	    for (String key : treeMap.keySet()) {
	    	Rating rating = treeMap.get(key);
	    	XYSeries innovation = new XYSeries( key, false );          
	        innovation.add( rating.getEffortAvr() , rating.getResultAvr());
	          
	        XYSeries change = new XYSeries( "Change " + key, false ); 
	        change.add( category.getBaselineRating().getEffortAvr() , category.getBaselineRating().getResultAvr() );    
	        change.add( rating.getEffortAvr() , rating.getResultAvr());
	        
	        dataset.addSeries( innovation );
	        dataset.addSeries( change );
	    };
        
      return dataset;
   }
}
