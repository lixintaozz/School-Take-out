package com.sky.service.impl;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.index.PathBasedRedisIndexDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.PriorityQueue;

@Service
public class SetMealServiceImpl implements SetMealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO selectById(Long id) {
        //1.首先查询套餐信息
        SetmealVO setmealVO= setmealMapper.selectById(id);

        //2.然后查询套餐关联的菜品信息
        List<SetmealDish> setmealDishes = setMealDishMapper.selectBySetMealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void insertWithDishes(SetmealDTO setmealDTO) {
        //1.首先插入套餐的信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);

        Long setmealId = setmeal.getId();

        //2.然后插入套餐关联的菜品信息
        List<SetmealDish> list = setmealDTO.getSetmealDishes();
        list.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
        setMealDishMapper.insertBatch(list);
    }
}
