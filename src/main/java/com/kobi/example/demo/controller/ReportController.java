package com.kobi.example.demo.controller;

import com.kobi.example.demo.dto.ReportDto;
import com.kobi.example.demo.entity.Report;
import com.kobi.example.demo.service.ReportService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(value = "Report Controller", description = "Controller for Report operation", authorizations = {
        @Authorization(value="basicAuth")
})
@RestController
public class ReportController {

    private final static String TAG = "reports";

    private final ReportService reportService;

    @Autowired //Field injection is not recommended
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @ApiOperation(value = "get all reports", tags = TAG, response = Report.class, responseContainer = "List")
    @ResponseBody
    @GetMapping(value = "/v1/reports")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "All reports were returned"),
        @ApiResponse(code = 500, message = "Error occurred while fetching the reports")
    })
    public ResponseEntity<List<Report>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(reportService.getAllReports());
    }

    @ApiOperation(value = "get report by id", tags = TAG, response = Report.class)
    @ResponseBody
    @GetMapping(value = "/v1/report/{id}")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Report was fetched"),
            @ApiResponse(code = 404, message = "Report not found"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the report")
    })
    public ResponseEntity<Report> getReportById(@PathVariable("id") Integer id) {
        Report report = reportService.getReportById(id);
        return ResponseEntity.status(HttpStatus.OK).body(report);
    }

    @ApiOperation(value = "Create new report", tags = TAG)
    @PostMapping(value = "/v1/report")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Report was created"),
            @ApiResponse(code = 500, message = "Error occurred while creating the report")
    })
    public ResponseEntity register(@RequestBody @Valid ReportDto report) {
        reportService.create(report);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiOperation(value = "Update or register new report", tags = TAG)
    @PutMapping(value = "/v1/report")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Report was updated"),
            @ApiResponse(code = 500, message = "Error occurred while creating the report")
    })
    public ResponseEntity update(@RequestBody @Valid Report report) {
        reportService.update(report);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ApiOperation(value = "Delete existing report", tags = TAG)
    @DeleteMapping(value = "/v1/report/{id}")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Report was removed"),
            @ApiResponse(code = 404, message = "Report id not exists"),
            @ApiResponse(code = 500, message = "Error occurred while removing the report")
    })
    public ResponseEntity delete(@PathVariable("id") Integer id) {
        reportService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
