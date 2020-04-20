package com.kobi.example.demo.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kobi.example.demo.repository.ReportRepository;
import com.kobi.example.demo.service.client.ReportSchedulerClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
public class ApplicationConfiguration {

    @Bean
    ReportSchedulerClient reportSchedulerClient(@Value("${NUM_OF_TASKS:2}") Integer numOfTasks,
                                                ReportRepository reportRepository,
                                                @Value("${FILE:log.txt}") String fileName) {
        return new ReportSchedulerClient(reportRepository,
                Executors.newFixedThreadPool(numOfTasks), fileName, new ObjectMapper());
    }

}
