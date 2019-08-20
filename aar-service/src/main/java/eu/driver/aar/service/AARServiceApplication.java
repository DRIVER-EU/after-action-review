package eu.driver.aar.service;

import static springfox.documentation.builders.PathSelectors.regex;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import eu.driver.aar.service.controller.RecordRESTController;
import eu.driver.aar.service.controller.TopicInviteController;
import eu.driver.adapter.constants.TopicConstants;
import eu.driver.adapter.core.CISAdapter;
import eu.driver.adapter.excpetion.CommunicationException;
import eu.driver.adapter.properties.ClientProperties;
import eu.driver.model.core.Level;
import eu.driver.model.core.Log;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@ComponentScan
@EnableSwagger2
@SpringBootApplication
public class AARServiceApplication {

	private Logger log = Logger.getLogger(this.getClass());
	private CISAdapter cisAdapter;
	
	public AARServiceApplication() throws Exception {
		log.info("Init. AARServiceApplication");
	}
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(AARServiceApplication.class, args);
    }
	
	@Autowired
	RecordRESTController recordController;
	
	@Autowired
	TopicInviteController topicInviteController;
	
	@PostConstruct
	public void init() {
		Boolean connectTB = Boolean.parseBoolean(ClientProperties.getInstance().getProperty("connect.testbed", "true"));
		if (System.getenv().get("TESTBED_CONNECTION") != null) {
			connectTB = Boolean.parseBoolean(System.getenv().get("TESTBED_CONNECTION"));
		}
		if (connectTB) {
			cisAdapter = CISAdapter.getInstance(false);
			
			//cisAdapter.addCallback(topicInviteController, TopicConstants.TOPIC_INVITE_TOPIC);
			cisAdapter.addCallback(recordController, TopicConstants.TIMING_CONTROL_TOPIC);
			cisAdapter.addCallback(recordController, TopicConstants.OST_ANSWER_TOPIC);
			cisAdapter.addCallback(recordController, TopicConstants.SESSION_MGMT_TOPIC);
			cisAdapter.addCallback(recordController, TopicConstants.PHASE_MESSAGE_TOPIC);
			cisAdapter.addCallback(recordController, TopicConstants.ROLE_PLAYER_TOPIC);
			cisAdapter.addLogCallback(recordController);
			
			Log logMsg = new Log(cisAdapter.getClientID(), (new Date()).getTime(), Level.INFO, "The AARService is up!" );
			try {
				cisAdapter.addLogEntry(logMsg);	
			} catch(CommunicationException cEx) {
				log.error("Error sending the log entry to the topic!", cEx);
			}
			
			cisAdapter.addCallback(recordController, "photo_geojson");
			cisAdapter.addCallback(recordController, "assign_info");
			cisAdapter.addCallback(recordController, "large_data_update");
			cisAdapter.addCallback(recordController, "crowd-tasker_info");
			cisAdapter.addCallback(recordController, "map_layer_update");
			/*cisAdapter.addCallback(recordController, "standard_cap");
			cisAdapter.addCallback(recordController, "flood_prediction_netcdf");
			cisAdapter.addCallback(recordController, "flood_prediction_geojson");
			cisAdapter.addCallback(recordController, "flood_actual");
			cisAdapter.addCallback(recordController, "lcms_plots");
			cisAdapter.addCallback(recordController, "crisissuite_htm_plots");
			cisAdapter.addCallback(recordController, "crisissuite_stedin_plots");*/
		}
	}
	
	@Bean
    public Docket newsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("AARService")
                .apiInfo(apiInfo())
                .select()
                .paths(regex("/AARService.*"))
                .build();
    }
	
	private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("AARServiceApplication REST Interface API Doc.")
                .description("This is the AARServiceApplication REST Interface API Documentation made with Swagger.")
                .version("1.0")
                .build();
    }

	public RecordRESTController getRecordController() {
		return recordController;
	}

	public void setRecordController(RecordRESTController recordController) {
		this.recordController = recordController;
	}

	public TopicInviteController getTopicInviteController() {
		return topicInviteController;
	}

	public void setTopicInviteController(TopicInviteController topicInviteController) {
		this.topicInviteController = topicInviteController;
	}
}
