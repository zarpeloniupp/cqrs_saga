package com.appsdeveloperblog.estore.OrdersService.saga;

import com.appsdeveloperblog.estore.OrdersService.command.RejectOrderCommand;
import com.appsdeveloperblog.estore.OrdersService.command.commands.ApproveOrderCommand;
import com.appsdeveloperblog.estore.OrdersService.core.events.OrderApprovedEvent;
import com.appsdeveloperblog.estore.OrdersService.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.estore.OrdersService.core.events.OrderRejectedEvent;
import com.appsdeveloperblog.estore.OrdersService.core.model.OrderSummary;
import com.appsdeveloperblog.estore.OrdersService.query.FindOrderQuery;
import com.appsdeveloperblog.estore.core.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.estore.core.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.estore.core.commands.ReverseProductCommand;
import com.appsdeveloperblog.estore.core.events.PaymentProcessedEvent;
import com.appsdeveloperblog.estore.core.events.ProductReservationCancelledEvent;
import com.appsdeveloperblog.estore.core.events.ProductReservedEvent;
import com.appsdeveloperblog.estore.core.model.User;
import com.appsdeveloperblog.estore.core.query.FetchUserPaymentDetailsQuery;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
@Slf4j
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    @Autowired
    private transient DeadlineManager deadlineManager;

    @Autowired
    private  transient QueryUpdateEmitter queryUpdateEmitter;


    private String deadlineId;

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
                    RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(
                            orderCreatedEvent.getOrderId(),
                            commandResultMessage.exceptionResult().getMessage()
                    );
                    commandGateway.send(rejectOrderCommand);
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
            cancelProductReservation(productReservedEvent,ex.getMessage());
            return;
       }

       if(userPaymentDetails == null){
           // start compensation transaction
           cancelProductReservation(productReservedEvent,"Could not fetch user payment details");
           return;
       }

       log.info(String.format("Successfully fetched user payment details for user [%s] ",userPaymentDetails.getFirstName()));

        deadlineId = deadlineManager.schedule(Duration.of(10, ChronoUnit.SECONDS), "payment-processing-deadline", productReservedEvent);

        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .paymentDetails(userPaymentDetails.getPaymentDetails())
                .paymentId(UUID.randomUUID().toString())
                .build();

        String result = null;

        try {
            result = commandGateway.sendAndWait(processPaymentCommand);
        } catch (Exception ex){
            log.error(ex.getMessage());
            // Start compensating transaction
            cancelProductReservation(productReservedEvent,ex.getMessage());
            return;
        }

        if(result == null){
            log.info("The processPaymentCommand resulted in null, Initiating a compesation transaction");
            // start compensationg transaction
            cancelProductReservation(productReservedEvent,"Could not proccess user payment with provided payment details");
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent) {

        cancelDeadline();

        log.info("The PaymentProcessedEvent is called");

        ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(paymentProcessedEvent.getOrderId());

        commandGateway.send(approveOrderCommand);
    }

    private void cancelDeadline(){
        if(deadlineId != null){
            deadlineManager.cancelSchedule("payment-processing-deadline", deadlineId);
            deadlineId = null;
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent){

        log.info("The OrderApprovedEvent, order is approved. Order saga is complete for orderId "+ orderApprovedEvent.getOrderId());

       // SagaLifecycle.end();
        queryUpdateEmitter.emit(FindOrderQuery.class, query -> true,
                new OrderSummary(orderApprovedEvent.getOrderId(),
                        orderApprovedEvent.getOrderStatus(),
                        ""));

    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationCancelledEvent productReservationCancelledEvent){
        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(
                productReservationCancelledEvent.getOrderId(),
                productReservationCancelledEvent.getReason()
        );
        commandGateway.send(rejectOrderCommand);
    }
    private void cancelProductReservation(ProductReservedEvent productReservedEvent, String reason){

        cancelDeadline();

        CancelProductReservationCommand cancelProductReservationCommand =
                CancelProductReservationCommand.builder()
                        .orderId(productReservedEvent.getOrderId())
                        .productId(productReservedEvent.getProductId())
                        .quantity(productReservedEvent.getQuantity())
                        .userId(productReservedEvent.getUserId())
                        .reason(reason)
                        .build();

        commandGateway.send(cancelProductReservationCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRejectedEvent orderRejectedEvent){
        log.info("Successfully rejected for orderId "+ orderRejectedEvent.getOrderId());
        queryUpdateEmitter.emit(FindOrderQuery.class, query -> true,
                new OrderSummary(orderRejectedEvent.getOrderId(),
                        orderRejectedEvent.getOrderStatus(),
                        orderRejectedEvent.getReason()));
    }

    @DeadlineHandler(deadlineName = "payment-processing-deadline")
    public void handlePaymentDeadline(ProductReservedEvent productReservedEvent){
        log.info("Payment processing deadline took place. Seding a compensating command to cancel the product reservation");
        cancelProductReservation(productReservedEvent, "Payment timeout");
    }

}