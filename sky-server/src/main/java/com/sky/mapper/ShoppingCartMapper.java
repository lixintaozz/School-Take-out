package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 查询购物车该商品是否存在
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> selectExist(ShoppingCart shoppingCart);

    /**
     * 根据id更新shoppingCart
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateById(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     * @param shoppingCart
     */
    void insert(ShoppingCart shoppingCart);

    /**
     * 查看购物车
     * @param id
     * @return
     */
    @Select("select * from shopping_cart where user_id = #{id}")
    List<ShoppingCart> list(Long id);

    /**
     * 清空购物车
     * @param id
     */
    @Delete("delete from shopping_cart where user_id = #{id}")
    void clean(Long id);

    /**
     * 根据id删除购物车商品
     * @param shoppingCart
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(ShoppingCart shoppingCart);
}
