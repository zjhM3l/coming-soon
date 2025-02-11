package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.sky.entity.Orders;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    // 需要主键回显，后面订单明细表需要用到
    void insert(Orders orders);

}
