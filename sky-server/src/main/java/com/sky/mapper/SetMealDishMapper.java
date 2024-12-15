package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    /**
     * 插入套餐关联的菜品信息
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 删除套餐关联的菜品
     * @param setmealIds
     */
    void deleteBatch(List<Long> setmealIds);
}
