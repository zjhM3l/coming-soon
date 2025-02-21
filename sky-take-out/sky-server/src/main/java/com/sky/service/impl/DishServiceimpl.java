package com.sky.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aliyuncs.ecs.model.v20140526.AttachKeyPairResponse;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DishServiceimpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

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

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    // 还是基于PageHelper
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    public void deleteBatch(List<Long> ids) {
        // 1. 菜品是否能够删除——是否起售？
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 2. 菜品是否能够删除——是否关联套餐？
        // 菜品和套餐多对多关系，中间关系表setmeal_dish
        // 注入SetmealDishMapper
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 3. 批量删除菜品表中的菜品数据
        // for (Long id : ids) {
        //     dishMapper.deleteById(id);
        //     // 4. 批量删除口味表中的菜品关联的口味数据
        //     // 不管有没有口味数据，直接删除
        //     dishFlavorMapper.deleteByDishId(id);
        // }

        // 5. 优化：批量删除菜品表中的菜品数据，不用for遍历两步sql
        // 动态拼接(id1, id2, id3)即可，flavor同理

        // 根据菜品id集合批量删除菜品表中的菜品数据
        // delete from dish where id in (?, ?, ?)
        dishMapper.deleteByIds(ids);

        // 根据菜品id集合批量删除口味表中的菜品关联的口味数据
        // delete from dish_flavor where dish_id in (?, ?, ?)
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id查询菜品和对应的口味用于修改菜品页面回显
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id) {
        // 1. 查询菜品表中的菜品数据
        Dish dish = dishMapper.getById(id);

        // 2. 查询口味表中的菜品关联的口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        // 3. 封装DishVO对象
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    /**
     * 根据dishId修改菜品基本信息和对应的口味
     * @param dishDTO
     */
    public void updateWithFlavor(DishDTO dishDTO) {
        // 1. 修改菜品表中的菜品数据
        // DTO里面还包含了口味表的内容，我们只需要Dish内容
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        // 2. 修改口味表中的菜品关联的口味数据(先删除原有口味，再插入)
        // 这里讨巧了，因为如果试图修改口味表中的菜品关联的口味数据，情况很复杂

        // 2.1 先删除原有口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        // 2.2 再插入新的口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor ->{
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品起售停售
     *
     * @param status
     * @param id
    */
    @Transactional
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
            .id(id)
            .status(status)
            .build();
        dishMapper.update(dish);

        if (status == StatusConstant.DISABLE) {
            // 如果是停售操作，还需要将包含当前菜品的套餐也停售
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            // select setmeal_id from setmeal_dish where dish_id in (?,?,?)
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);
            if (setmealIds != null && setmealIds.size() > 0) {
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder()
                        .id(setmealId)
                        .status(StatusConstant.DISABLE)
                        .build();
                    setmealMapper.update(setmeal);
                }
            }
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
    */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
            .categoryId(categoryId)
            .status(StatusConstant.ENABLE)
            .build();
        return dishMapper.list(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
