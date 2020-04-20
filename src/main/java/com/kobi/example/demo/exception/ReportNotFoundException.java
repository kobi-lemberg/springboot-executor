package com.kobi.example.demo.exception;


import lombok.Getter;

public class ReportNotFoundException extends RuntimeException {

    @Getter
    private final int reportId;

    public ReportNotFoundException(int reportId) {
        super(String.format("Report %d not found", reportId));
        this.reportId = reportId;
    }
}
