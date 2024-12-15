package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    /**
     * 根据菜品ids查询套餐ids
     * @param dishIds
     * @return
     */
    List<Long> selectByDishIds(List<Long> dishIds);

    /**
     * 根据套餐id查询其关联的菜品
     * @param setMealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setMealId}")
    List<SetmealDish> selectBySetMealId(Long setMealId);
}
