package com.sky.Task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class CheckOrderOverTime {

    @Autowired
    OrderMapper orderMapper;

    /**
     * 处理超时订单的方法
     */
    @Scheduled(cron = "0 * * * * ?")    //每分钟执行一次
    public void checkOutOfTime()
    {
        log.info("定时处理超时订单: {}", LocalDateTime.now());
        List<Orders> ordersList = orderMapper.check(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));

        if (ordersList != null && !ordersList.isEmpty())
        {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时未支付");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理派送中订单的方法
     */
    @Scheduled(cron = "0 0 1 * * ?")    //每天1点执行一次
    public void checkAlreadyFinished()
    {
        log.info("定时处理已完成的订单: {}", LocalDateTime.now());
        List<Orders> ordersList = orderMapper.check(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));
        if (ordersList != null && !ordersList.isEmpty())
        {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
