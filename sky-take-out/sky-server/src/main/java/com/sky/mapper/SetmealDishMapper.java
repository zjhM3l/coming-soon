package com.sky.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import com.sky.entity.SetmealDish;

@Mapper
public interface SetmealDishMapper {

    /** 
     * 根据菜品id查询套餐id，多对多关系
     * @param dishId
     * @return
     */
    // select setmeal_id from setmeal_dish where dish_id in (1, 2, 3)
    // 动态sql，xml
    List<Long> getSetmealIdsByDishId(List<Long> dishIds);

    /**
     * 批量保存套餐和菜品的关联关系
     * @param setmealDishes
    */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id删除套餐和菜品的关联关系
     * @param setmealId
    */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);

}
