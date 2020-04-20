package com.kobi.example.demo.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kobi.example.demo.entity.Report;
import com.kobi.example.demo.repository.ReportRepository;
import com.kobi.example.demo.utils.ExceptionUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.kobi.example.demo.utils.ExceptionUtils.swallowException;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

@Slf4j
@AllArgsConstructor
@Service
public class ReportSchedulerClient {

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(1);

    private final ReportRepository repository;
    private final ExecutorService executorService;
    @Getter
    private final String fileName;
    private final ObjectMapper om;

    @PostConstruct
    public void init() {
        recreateFile();
        //For some reason @scheduled didn't work =\
        SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(()-> {
            log.info("Trying to find appropriate report");
            repository.findAllByOrderByIdAsc().forEach(this::scheduleReport);
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void recreateFile() {
        swallowException(() -> { //We don't want to fail the application if we got exception, we will try to create it every time the scheduler invokes
            Files.deleteIfExists(Paths.get(fileName));
            Files.createFile(Paths.get(fileName));
        }, "Failed to create file");
    }

    private void scheduleReport(Report report) {
        swallowException(() -> {
            if(shouldRun(report)) {
                log.info("Got report {} to schedule", report);
                logToFile(report);
                executorService.submit(report::generate);
            }
        }, String.format( "Got error while executing report %s, we will try again next time", report));
    }

    //This is critical section, hence need to be synchronized
    private synchronized void logToFile(Report report) throws IOException {
        Files.write(Paths.get(fileName), Collections.singletonList(om.writeValueAsString(report)), UTF_8, APPEND, CREATE);
    }

    private boolean shouldRun(Report report) {
        LocalTime now = LocalTime.now();
        return now.getHour() == report.getHourToRun() &&
                now.getMinute() == report.getMinutesToRun();
    }
}
