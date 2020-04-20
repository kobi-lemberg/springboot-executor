package com.kobi.example.demo.repository;

import com.kobi.example.demo.entity.Report;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends CrudRepository<Report, Integer> {
    List<Report> findAllByOrderByIdAsc();
}
