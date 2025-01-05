package com.sky.service;

import com.sky.vo.SalesTop10ReportVO;

import java.time.LocalDate;

public interface ReportService {
    /**
     * 查询销量top10
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO selectTop10(LocalDate begin, LocalDate end);
}
