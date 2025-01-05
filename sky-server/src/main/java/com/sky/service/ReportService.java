package com.sky.service;

import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {
    /**
     * 查询销量top10
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO selectTop10(LocalDate begin, LocalDate end);

    /**
     * 用户统计接口
     * @param begin
     * @param end
     * @return
     */
    UserReportVO userCount(LocalDate begin, LocalDate end);

    /**
     * 营业额统计接口
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO turnoverCount(LocalDate begin, LocalDate end);
}
