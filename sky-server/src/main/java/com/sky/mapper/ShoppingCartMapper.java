package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 获取购物车列表
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> selectShoppingCartList(ShoppingCart shoppingCart);

    /**
     * 更新购物车数据
     * @param cart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart cart);

    /**
     * 插入购物车数据
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            "values (#{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户id删除购物车数据
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    /**
     * 根据id删除购物车内的一条内容
     * @param id
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteByShoppingCartId(Long id);

    /**
     * 将商品数量减一
     * @param id
     */
    @Update("update shopping_cart set number = number - 1 where id = #{id}")
    void updateShoppingCartNumberById(Long id);
}
