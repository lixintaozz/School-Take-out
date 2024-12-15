package com.sky.service;

import com.sky.vo.SetmealVO;

public interface SetMealService {
    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    SetmealVO selectById(Long id);
}
