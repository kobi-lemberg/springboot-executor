package com.kobi.example.demo.entity;


import com.kobi.example.demo.dto.ReportDto;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalTime;
import java.util.Date;

@Entity
@Table(schema = "reports_schema", name = "reports")
@EqualsAndHashCode
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED) /* For hibernate only. */
@NoArgsConstructor(access = AccessLevel.PROTECTED) /* For hibernate only. */
public class Report {

    public Report(String name, int hourToRun, int minutesToRun) {
        this.name = name;
        this.hourToRun = hourToRun;
        this.minutesToRun = minutesToRun;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "report_name")
    @Length(max = 50, min = 1, message = "Length of report name should be between 1 to 50")
    private String name;

    @Min(0)
    @Max(24)
    @Column(name = "report_time_hour")
    private int hourToRun;

    @Min(0)
    @Max(60)
    @Column(name = "report_time_minuets")
    private int minutesToRun;

    public void generate() {
        //here we need to implement
    }
}
