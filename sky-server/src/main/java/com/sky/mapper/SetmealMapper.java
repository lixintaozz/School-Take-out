package com.sky.mapper;

import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 根据id查询套餐信息
     * @param id
     * @return
     */
    @Select("select setmeal.*, category.name category_name from " +
            "setmeal left join category on setmeal.category_id = category.id where setmeal.id = #{id}")
    SetmealVO selectById(Long id);
}
