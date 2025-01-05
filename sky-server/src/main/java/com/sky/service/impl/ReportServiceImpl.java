package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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

    @Autowired
    private WorkspaceService workspaceService;
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

    /**
     * 营业额统计接口
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverCount(LocalDate begin, LocalDate end) {
        //1. 准备日期数据
        List<LocalDate> localDateList = new ArrayList<>();
        while (!begin.equals(end))
        {
            localDateList.add(begin);
            begin = begin.plusDays(1);
        }
        localDateList.add(end);

        List<BigDecimal> bigDecimals = new ArrayList<>();
        //2. 查询当天的营业额
        for (LocalDate localDate : localDateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            BigDecimal turnover = reportMapper.selectTurnover(beginTime, endTime, Orders.COMPLETED);
            bigDecimals.add(turnover == null ? BigDecimal.valueOf(0.0) : turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(localDateList, ","))
                .turnoverList(StringUtils.join(bigDecimals, ","))
                .build();
    }

    /**
     * 订单统计接口
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO orderCount(LocalDate begin, LocalDate end) {
        //1. 准备日期数据
        List<LocalDate> localDateList = new ArrayList<>();
        while (!begin.equals(end))
        {
            localDateList.add(begin);
            begin = begin.plusDays(1);
        }
        localDateList.add(end);

        //2.准备订单数据
        List<Integer> totalOrders = new ArrayList<>();
        List<Integer> validOrders = new ArrayList<>();
        Integer total = 0;
        Integer valid = 0;
        for (LocalDate localDate : localDateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Integer orderCount = reportMapper.selectOrderCount(beginTime, endTime, null);
            total += orderCount;
            totalOrders.add(orderCount == null ? 0 : orderCount);
            Integer validNum = reportMapper.selectOrderCount(beginTime, endTime, Orders.COMPLETED);
            validOrders.add(validNum == null ? 0 : validNum);
            valid += validNum;
        }

        Double validRate = 0.0;
        if (total != 0)
            validRate = valid.doubleValue() / total;
        return OrderReportVO.builder()
                .dateList(StringUtils.join(localDateList, ","))
                .orderCountList(StringUtils.join(totalOrders, ","))
                .validOrderCountList(StringUtils.join(validOrders, ","))
                .totalOrderCount(total)
                .validOrderCount(valid)
                .orderCompletionRate(validRate)
                .build();
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2. 通过POI将数据写入到Excel文件中

        //Todo:Java通过这种方法来获取当前项目中其他文件夹下的文件位置貌似返回的是文件系统的路径，适用于开发环境中，在其他场景下不一定适用（如打包为jar包）
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
