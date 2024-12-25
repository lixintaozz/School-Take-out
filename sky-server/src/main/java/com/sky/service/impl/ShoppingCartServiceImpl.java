package com.sky.service.impl;


import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.privilege.SetAccessibleAction;
import org.apache.poi.xssf.binary.XSSFBHyperlinksTable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;


    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //1.先查询此商品在当前用户的购物车中是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.selectExist(shoppingCart);
        if (list != null && !list.isEmpty())
        {
            ShoppingCart shoppingCart1 = list.get(0);
            //2.如果存在，那我们就将它的数量加1即可
            shoppingCart1.setNumber(shoppingCart1.getNumber() + 1);
            shoppingCartMapper.updateById(shoppingCart1);
        }else
        {
            //3.否则，我们需要向购物车表中插入一条数据
            //3.1 如果需要插入的数据为套餐，那就去填充套餐的图片，名字等信息
            if (shoppingCart.getSetmealId() != null)
            {
                SetmealVO setmealVO = setmealMapper.selectById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmealVO.getName());
                shoppingCart.setImage(setmealVO.getImage());
                shoppingCart.setAmount(setmealVO.getPrice());
            }else
            {
                //3.2 否则去填充菜品的图片，名字等信息
                Dish dish = dishMapper.selectById(shoppingCart.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }

            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);
            //3.3 最后将填充完毕的购物车数据插入数据库表即可
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        Long id = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(id);
        return shoppingCartList;
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        Long id = BaseContext.getCurrentId();
        shoppingCartMapper.clean(id);
    }

    /**
     * 删除购物车中的一个商品
     * @param shoppingCartDTO
     */
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        //1. 获取要删除的商品的数据
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long id = BaseContext.getCurrentId();
        shoppingCart.setUserId(id);

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectExist(shoppingCart);
        ShoppingCart shoppingCart1 = shoppingCartList.get(0);
        //2. 如果商品数量减一后为零，那么从表中删除商品
        if (shoppingCart1.getNumber() == 1)
            shoppingCartMapper.deleteById(shoppingCart1);
        else {
            //3. 否则将商品数量减一
            shoppingCart1.setNumber(shoppingCart1.getNumber() - 1);
            shoppingCartMapper.updateById(shoppingCart1);
        }
    }
}
