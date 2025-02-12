package com.sky.task;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 定时任务类，定时处理订单状态
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 定时处理超时订单
     */
    // 每分钟执行一次
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutOrder() {
        log.info("定时处理超时订单: {}" , LocalDateTime.now());
        
        // 查询超时订单
        // select * from orders where status = ? and order_time < (当前时间 - 15分钟)
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
        
        // 遍历订单列表，修改订单状态为已取消
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                // 修改订单状态为已取消
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("超时未支付");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 定时处理一直处于派送中状态的订单
     */
    // 每天凌晨一点触发一次
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder() {
        log.info("定时处理派送中订单: {}" , LocalDateTime.now());
        
        // 查询派送中订单
        // select * from orders where status = ? and order_time 处于上一个工作日
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        
        // 遍历订单列表，修改订单状态为已完成
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                // 修改订单状态为已完成
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
