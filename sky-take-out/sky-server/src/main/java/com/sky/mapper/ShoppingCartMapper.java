package com.sky.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import com.sky.entity.ShoppingCart;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 动态条件查询购物车(根据dish_id/setmeal_id结合user_id)
     * @param shoppingCart
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据id修改商品数量
     * @param cart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart cart);

    /**
     * 插入购物车数据
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart(name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) values(#{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据id删除购物车数据
     * @param id
    */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    /**
     * 根据user_id删除购物车数据
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    /**
     * 批量插入购物车数据
     *
     * @param shoppingCartList
     */
    void insertBatch(List<ShoppingCart> shoppingCartList);
}
