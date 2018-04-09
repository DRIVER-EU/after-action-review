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
		cisAdapter = CISAdapter.getInstance(false);
		
		cisAdapter.addLogCallback(recordController);
		cisAdapter.addCallback(topicInviteController, TopicConstants.TOPIC_INVITE_TOPIC);
		
		Log logMsg = new Log(cisAdapter.getClientID(), (new Date()).getTime(), Level.INFO, "The AARService is up!" );
		cisAdapter.addLogEntry(logMsg);
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
