package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理15分钟内未付款的超时订单
     * 每分钟查询一次
     */
    @Scheduled(cron = "0 * * * * ? ")
    public void timeOut(){
        log.info("定时处理15分钟内未付款的超时订单：{}", LocalDateTime.now());
        //获得对应订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> list = orderMapper.getTaskOrder(Orders.PENDING_PAYMENT,time);

        //更新对应订单
        for (Orders orders : list) {
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("订单超时，自动取消");
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.update(orders);
        }

    }

    /**
     * 每天定时处理状态一直为派送中的订单
     * 打烊时间（1am）查询一次
     */
    @Scheduled(cron = "0 0 1 * * ? ")
    public void delivering(){
        log.info("定时处理状态一直为派送中的订单：{}", LocalDateTime.now());
        //获得对应订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> list = orderMapper.getTaskOrder(Orders.DELIVERY_IN_PROGRESS,time);

        //更新对应订单
        for (Orders orders : list) {
            orders.setStatus(Orders.COMPLETED);
            orderMapper.update(orders);
        }
    }
}
