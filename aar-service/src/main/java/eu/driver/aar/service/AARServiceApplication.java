package eu.driver.aar.service;

import static springfox.documentation.builders.PathSelectors.regex;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

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
	
	public AARServiceApplication() throws Exception {
		log.info("Init. AARServiceApplication");
	}
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(AARServiceApplication.class, args);
    }
	
	@PostConstruct
	public void init() {
		
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
}
