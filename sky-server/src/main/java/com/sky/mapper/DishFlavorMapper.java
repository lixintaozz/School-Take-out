package com.sky.mapper;

import com.sky.entity.DishFlavor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入菜品口味
     * @param list
     */
    public void insertBatch(List<DishFlavor> list);

    /**
     * 删除菜品关联的口味
     * @param dishIds
     */
    void deleteFlavorWithDish(List<Long> dishIds);

    /**
     * 根据菜品id查询关联的口味
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> selectByDishId(Long dishId);
}
