package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
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
    @Autowired
    private SetMealDishMapper setmealDishMapper;

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

    /**
     * 菜品分页查询接口
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //1. 获取分页查询得到的dish列表
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        //这里SQL依然查询了所有的属性，然后将其赋给了对应的vo属性
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        //1.检查菜品是否处于起售状态，如果是，则抛异常
        for (Long id : ids) {
            Dish dish = dishMapper.selectById(id);
            if (dish.getStatus().equals(StatusConstant.ENABLE))
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
        //2. 检查菜品是否关联了套餐，如果是，则抛异常
        List<Long> setMealIds = setmealDishMapper.selectByDishIds(ids);
        if (setMealIds != null && !setMealIds.isEmpty())
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);

        //3. 根据ids批量删除菜品
        dishMapper.deleteBatch(ids);

        //4. 根据ids批量删除菜品关联的口味
        dishFlavorMapper.deleteFlavorWithDish(ids);
    }
}
