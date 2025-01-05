package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;
    /**
     * 查询销量top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO selectTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        //先查询出来销量排名前十的菜品和对应的销量
        List<GoodsSalesDTO> goodsSalesDTOList = reportMapper.selectTop10(beginTime, endTime);


        List<String> dishes = new ArrayList<>();
        List<Integer> sales = new ArrayList<>();

        for (GoodsSalesDTO goodsSalesDTO : goodsSalesDTOList) {
            dishes.add(goodsSalesDTO.getName());
            sales.add(goodsSalesDTO.getNumber());
        }

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(dishes, ","))
                .numberList(StringUtils.join(sales, ","))
                .build();

    }

    /**
     * 用户统计接口
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userCount(LocalDate begin, LocalDate end) {
        //1.准备日期数据
        List<LocalDate> localDateList = new ArrayList<>();
        while (!begin.equals(end))
        {
            localDateList.add(begin);
            begin = begin.plusDays(1);
        }
        localDateList.add(end);

        //2.准备新增用户数据
        List<Integer> userAddCount = new ArrayList<>();
        //3.准备总用户数据
        List<Integer> userTotalCount = new ArrayList<>();

        for (LocalDate localDate : localDateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            userTotalCount.add(reportMapper.selectUserCount(null, endTime));
            userAddCount.add(reportMapper.selectUserCount(beginTime, endTime));
        }


        return UserReportVO.builder()
                .newUserList(StringUtils.join(userAddCount, ","))
                .totalUserList(StringUtils.join(userTotalCount, ","))
                .dateList(StringUtils.join(localDateList, ","))
                .build();
    }
}
