package com.appsdeveloperblog.estore.OrdersService.saga;

import com.appsdeveloperblog.estore.OrdersService.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.estore.core.commands.ReverseProductCommand;
import com.appsdeveloperblog.estore.core.events.ProductReservedEvent;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
@Slf4j
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent orderCreatedEvent) {
        ReverseProductCommand reserve =
                ReverseProductCommand.builder()
                        .orderId(orderCreatedEvent.getOrderId())
                        .productId(orderCreatedEvent.getProductId())
                        .quantity(orderCreatedEvent.getQuantity())
                        .userId(orderCreatedEvent.getUserId())
                        .build();

        log.info(String.format("OrderCreatedEvent handled for orderId %s and productId %s ", reserve.getOrderId(), reserve.getProductId()));

        commandGateway.send(reserve, new CommandCallback<ReverseProductCommand, Object>() {
            @Override
            public void onResult(@Nonnull CommandMessage<? extends ReverseProductCommand> commandMessage, @Nonnull CommandResultMessage<?> commandResultMessage) {
                if(commandResultMessage.isExceptional()){
                    // start a compensating transaction
                }
            }
        });
    }


    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent){
        //process user payment
        log.info(String.format("ProductReservedEvent is called for productID [%s] and orderId [%s]", productReservedEvent.getProductId(), productReservedEvent.getOrderId()));
    }

}
