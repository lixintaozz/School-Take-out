package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetMealService {
    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    SetmealVO selectById(Long id);

    /**
     * 新增套餐
     * @param setmealDTO
     */
    void insertWithDishes(SetmealDTO setmealDTO);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteWithDishes(List<Long> ids);

    /**
     * 起售/停售套餐
     * @param status
     * @param id
     */
    void startOrStop(Integer status, long id);
}
