package com.sky.controller.admin;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aliyuncs.ecs.model.v20140526.AttachKeyPairResponse;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("菜品信息：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        // 新增菜品成功后，清除redis缓存
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);
        
        return Result.success();
    }

    /**
     * 菜品分页
     * @param DishPageQueryDTO
     * @return
     */
    // query请求参数不是json，不需要@RequestBody
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询条件：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /** 
     * 菜品批量删除
     * @param ids
     * @return
     */
    // 接受query参数的String，形如1,2,3，数字表示菜品id
    // 由MCV框架解析字符串封装入List<Long> ids也可以，但是要记得加注解
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        dishService.deleteBatch(ids);

        // 批量删除菜品成功后，清除redis缓存
        // 这里for循环查找每个菜品的categoryId的话本末倒置了，直接全部清除
        cleanCache("dish_*");
        
        return Result.success();
    }

    /**
     * 根据菜品id获得菜品信息（包括Dish的基本信息以及口味信息）
     * @param id
     * @return 
     */
    // 返回的Result泛型是DishVO，因为DishDTO不满足要求，需要额外的口味信息
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }
    
    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        dishService.updateWithFlavor(dishDTO);

        // 修改菜品成功后，清除redis缓存
        // 这里要考虑一个特殊情况，如果修改的是菜品的分类id，那么需要清除原来的分类id对应的缓存
        // 其实这里也直接全清也行，毕竟业务一般不会有这么频繁的修改
        cleanCache("dish_*");
        
        return Result.success();
    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     * @return
    */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id){
        dishService.startOrStop(status,id);

        // 菜品起售停售成功后，清除redis缓存
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
    */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    /**
     * 统一清理缓存数据
     * @param pattern 
     * @return
     */
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
