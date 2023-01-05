package com.appsdeveloperblog.estore.OrdersService.saga;

import com.appsdeveloperblog.estore.OrdersService.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.estore.core.commands.ReverseProductCommand;
import com.appsdeveloperblog.estore.core.events.ProductReservedEvent;
import com.appsdeveloperblog.estore.core.model.User;
import com.appsdeveloperblog.estore.core.query.FetchUserPaymentDetailsQuery;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
@Slf4j
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent orderCreatedEvent) {

        ReverseProductCommand reserveProductCommand =
                ReverseProductCommand.builder()
                        .orderId(orderCreatedEvent.getOrderId())
                        .productId(orderCreatedEvent.getProductId())
                        .quantity(orderCreatedEvent.getQuantity())
                        .userId(orderCreatedEvent.getUserId())
                        .build();

        log.info(String.format("OrderCreatedEvent handled for orderId %s and productId %s ", reserveProductCommand.getOrderId(), reserveProductCommand.getProductId()));

        commandGateway.send(reserveProductCommand, new CommandCallback<ReverseProductCommand, Object>() {
            @Override
            public void onResult(@Nonnull CommandMessage<? extends ReverseProductCommand> commandMessage, @Nonnull CommandResultMessage<?> commandResultMessage) {
                if (commandResultMessage.isExceptional()) {
                    log.info(String.format("commandResultMessage.isExceptional() %s ",  commandResultMessage.toString()));
                }
            }
        });
    }


    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent) {
        //process user payment
        log.info(String.format("ProductReservedEvent is called for productID [%s] and orderId [%s]", productReservedEvent.getProductId(), productReservedEvent.getOrderId()));

        FetchUserPaymentDetailsQuery fetchUserPaymentDetailsQuery =
                new FetchUserPaymentDetailsQuery(productReservedEvent.getUserId());

        User userPaymentDetails = null;

       try{
            userPaymentDetails = queryGateway.query(fetchUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class)).join();
       }catch (Exception ex){
            log.error(ex.getMessage());
            // start compensation transaction
            return;
       }

       if(userPaymentDetails == null){
           // start compensation transaction
           return;
       }

       log.info(String.format("Successfully fetched user payment details for user [%s] ",userPaymentDetails.getFirstName()));
    }

//    @SagaEventHandler(associationProperty = "paymentId")
//    public void handle(PaymentProcessedEvent paymentProcessedEvent){}
//    @EndSaga
//    @SagaEventHandler(associationProperty = "orderId")
//    public void handle(OrderApprovedEvent orderApprovedEvent){}

}
