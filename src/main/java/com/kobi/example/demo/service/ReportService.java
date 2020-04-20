package com.kobi.example.demo.service;

import com.kobi.example.demo.dto.ReportDto;
import com.kobi.example.demo.entity.Report;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

public interface ReportService {
    /**
     * @return all existing reports at the system
     */
    List<Report> getAllReports();

    /**
     * Get report by its id
     * @param id the id if the report
     * @return report with the same id
     */
    Report getReportById(int id);

    /**
     * Create new report in the system
     * @param report the report to create
     */
    void create(ReportDto report);

    /**
     * Delete existing report in the system
     * @param reportId of the report to delete
     */
    void delete(int reportId);

    /**
     * Create new report in the system
     * @param report the report to update
     */
    void update(@Valid Report report);
}
