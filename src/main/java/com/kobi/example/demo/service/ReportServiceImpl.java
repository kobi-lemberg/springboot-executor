package com.kobi.example.demo.service;

import com.google.common.collect.Lists;
import com.kobi.example.demo.dto.ReportDto;
import com.kobi.example.demo.entity.Report;
import com.kobi.example.demo.exception.ReportNotFoundException;
import com.kobi.example.demo.repository.ReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public List<Report> getAllReports() {
        return Lists.newArrayList(reportRepository.findAll());
    }

    @Override
    public Report getReportById(int id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException(id));
    }

    @Override
    public void create(ReportDto report) {
        Report newReport = new Report(report.getName(), report.getHourToRun(), report.getMinutesToRun());
        reportRepository.save(newReport);
    }

    @Override
    public void delete(int reportId) {
        try {
            reportRepository.deleteById(reportId);
        } catch (EmptyResultDataAccessException e) {
            throw new ReportNotFoundException(reportId);
        }
    }

    @Override
    public void update(@Valid Report report) {
        reportRepository.save(report);
    }
}
