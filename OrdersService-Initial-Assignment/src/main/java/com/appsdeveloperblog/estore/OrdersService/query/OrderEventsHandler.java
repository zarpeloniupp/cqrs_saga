/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appsdeveloperblog.estore.OrdersService.query;

import com.appsdeveloperblog.estore.OrdersService.core.data.OrderEntity;
import com.appsdeveloperblog.estore.OrdersService.core.data.OrdersRepository;
import com.appsdeveloperblog.estore.OrdersService.core.events.OrderApprovedEvent;
import com.appsdeveloperblog.estore.OrdersService.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.estore.OrdersService.core.events.OrderRejectedEvent;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("order-group")
@Slf4j
public class OrderEventsHandler {
    
    private final OrdersRepository ordersRepository;
    
    public OrderEventsHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @EventHandler
    public void on(OrderCreatedEvent event) throws Exception {

        log.info(String.format("OrderCreatedEvent is called for orderId [%s] or productId [%s]", event.getOrderId(), event.getProductId()));


        OrderEntity orderEntity = new OrderEntity();
        BeanUtils.copyProperties(event, orderEntity);
 
        this.ordersRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderApprovedEvent orderApprovedEvent) {
        log.info(String.format("OrderApprovedEvent is called for orderId [%s]", orderApprovedEvent.getOrderId()));

        Optional<OrderEntity> order = ordersRepository.findByOrderId(orderApprovedEvent.getOrderId());

       if (order.isEmpty()){
           // TODO: do something about it
           return;
       }

       order.get().setOrderStatus(orderApprovedEvent.getOrderStatus());

       ordersRepository.save(order.get());
    }

    @EventHandler
    public void on(OrderRejectedEvent orderRejectedEvent) {
        log.info(String.format("OrderRejectedEvent is called for orderId [%s]", orderRejectedEvent.getOrderId()));

        Optional<OrderEntity> orderEntity = ordersRepository.findByOrderId(orderRejectedEvent.getOrderId());

        orderEntity.ifPresent(entity -> entity.setOrderStatus(orderRejectedEvent.getOrderStatus()));

        ordersRepository.save(orderEntity.get());
    }


}
