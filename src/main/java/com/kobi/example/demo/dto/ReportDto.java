package com.kobi.example.demo.dto;

import lombok.Value;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalTime;

@Value
public class ReportDto {
    @Length(max = 50, min = 1, message = "Length of report name should be between 1 to 50")
    private final String name;

    @Min(0)
    @Max(24)
    private int hourToRun;

    @Min(0)
    @Max(60)
    private int minutesToRun;
}
