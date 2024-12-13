package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    @Transactional       //这里需要操作两张表的数据，为了保证数据库的一致性，需要加入事务管理
    public void save(DishDTO dishDTO) {
        //往Dish表插入新菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        //获取菜品的id
        Long id = dish.getId();

        //往口味表插入新口味
        List<DishFlavor> list = dishDTO.getFlavors();
        if (list != null && !list.isEmpty()) {
            //需要先设置口味中的菜品id
            list.forEach(dishFlavor -> {dishFlavor.setDishId(id);});
            dishFlavorMapper.insertBatch(list);
        }
    }
}
