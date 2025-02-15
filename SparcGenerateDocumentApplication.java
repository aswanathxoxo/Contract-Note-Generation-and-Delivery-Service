package com.gtl;

import com.gtl.config.GlobalConfigurationReader;
import com.gtl.process.ProcessExecuterClient;
import com.gtl.utils.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.function.Consumer;

@SpringBootApplication(scanBasePackages = "com.gtl")
@Log4j2
@EnableScheduling
public class SparcGenerateDocumentApplication {

	@Autowired
	GlobalConfigurationReader globalConfigurationReader;
	@Autowired
	FileUtils fileUtils;

	@Autowired
	private ProcessExecuterClient execClient;

	public static void main(String[] args) {
		SpringApplication.run(SparcGenerateDocumentApplication.class, args);
	}

	@Bean
	public Consumer<GenericRecord> consumer(){
		return message -> {
			log.info("SparcGenerateDocumentApplication : consumer : Consumed message {} ", message);
			Utf8 topicUtf8 = (Utf8) message.get("Topic");
			String topic = topicUtf8.toString();
			log.info("Topic :::: " + topic);
			try {
				if (!topic.matches(globalConfigurationReader.getTopicRegex())) {
					throw new RuntimeException("Topic does not match expected pattern: " + topic);
				}
				log.info("Processing valid topic: {}", topic);
				execClient.execute(fileUtils.createRequest(message));
			} catch (Exception e) {
				log.error(" Exception: {}", e.getMessage());
			}
		};
	}
}
