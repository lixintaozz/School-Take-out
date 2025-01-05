package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReportMapper {
    /**
     * 查询销量排名前十的菜品和对应的销量
     * @param beginTime
     * @param endTime
     * @return
     */
    List<GoodsSalesDTO> selectTop10(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 查询指定时间范围内的用户总数
     * @param beginTime
     * @param endTime
     * @return
     */
    Integer selectUserCount(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 查询指定时间范围内的营业额
     * @param beginTime
     * @param endTime
     * @param status
     * @return
     */
    BigDecimal selectTurnover(LocalDateTime beginTime, LocalDateTime endTime, Integer status);
}
