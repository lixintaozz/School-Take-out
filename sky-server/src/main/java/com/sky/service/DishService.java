package com.sky.service;

import com.sky.dto.DishDTO;

public interface DishService {
    /**
     * 新增菜品接口
     * @param dishDTO
     */
    public void save(DishDTO dishDTO);
}
