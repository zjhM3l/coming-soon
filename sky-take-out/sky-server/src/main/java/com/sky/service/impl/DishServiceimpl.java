package com.sky.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DishServiceimpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    // 涉及到多表操作，开启事务，确保数据一致性原子性
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 1. 向菜品表插入一条数据（注入DishMapper）
        // 且不需要把DTO传进去，因为DTO同时包含了口味表的内容，我们只需要Dish内容
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        // 主键回填获取insert后生成的主键
        Long dishId = dish.getId();

        // 2. 口味可能会有多个，向口味表插入n条数据(注入DishFlavorMapper)
        // 通过DTO取得口味表集合数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            // 批量插入
            // 这里注意一点，插入口味需要根据dishId来插入
            // 但是现在是新增菜品，dishId还没有，这里需要主键回填技术
            flavors.forEach(dishFlavor ->{
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
