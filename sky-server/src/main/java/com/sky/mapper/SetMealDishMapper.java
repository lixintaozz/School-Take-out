package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    /**
     * 根据菜品ids查询套餐ids
     * @param dishIds
     * @return
     */
    List<Long> selectByDishIds(List<Long> dishIds);
}
