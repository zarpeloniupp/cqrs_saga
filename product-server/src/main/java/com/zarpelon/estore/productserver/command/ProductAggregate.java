package com.zarpelon.estore.productserver.command;


import com.appsdeveloperblog.estore.core.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.estore.core.commands.ReverseProductCommand;
import com.appsdeveloperblog.estore.core.events.ProductReservationCancelledEvent;
import com.appsdeveloperblog.estore.core.events.ProductReservedEvent;
import com.zarpelon.estore.productserver.command.model.CreateProductCommand;
import com.zarpelon.estore.productserver.command.model.ProductCreatedEvent;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@Aggregate(snapshotTriggerDefinition = "productSnapshotTriggerDefinition")
@Slf4j
public class ProductAggregate {

    @AggregateIdentifier
    private String productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;

    public ProductAggregate(){}

    @CommandHandler
    public ProductAggregate(CreateProductCommand createProductCommand){
        // validate create product command

        log.info(String.format("CreateProductCommand is called for productId [%s] ", createProductCommand.getProductId()));


        if (createProductCommand.getPrice().compareTo(BigDecimal.ZERO) <=0) {
            throw new IllegalArgumentException("price cannot be less opr equal than zero");
        }

        if (createProductCommand.getTitle() == null
                || createProductCommand.getTitle().isBlank()) {
            throw new IllegalArgumentException("title cannot be empty");
        }

        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent();

        BeanUtils.copyProperties(createProductCommand, productCreatedEvent);

        AggregateLifecycle.apply(productCreatedEvent);
    }

    @CommandHandler
    public void handle(ReverseProductCommand reserveProductCommand){

        log.info(String.format("ReverseProductCommand is called for productId [%s] ", reserveProductCommand.getProductId()));


        if(quantity < reserveProductCommand.getQuantity()){
            throw new IllegalArgumentException("Insufficient number of items in stock");
        }

        ProductReservedEvent productReservedEvent = ProductReservedEvent.builder()
                .orderId(reserveProductCommand.getOrderId())
                .productId(reserveProductCommand.getProductId())
                .quantity(reserveProductCommand.getQuantity())
                .userId(reserveProductCommand.getUserId())
                .build();

        AggregateLifecycle.apply(productReservedEvent);
    }

    @CommandHandler
    public void handle(CancelProductReservationCommand cancelProductReservationCommand) {

        log.info(String.format("CancelProductReservationCommand is called for productId [%s] ", cancelProductReservationCommand.getProductId()));


        ProductReservationCancelledEvent productReservationCancelledEvent = ProductReservationCancelledEvent.builder()
                .orderId(cancelProductReservationCommand.getOrderId())
                .productId(cancelProductReservationCommand.getProductId())
                .quantity(cancelProductReservationCommand.getQuantity())
                .reason(cancelProductReservationCommand.getReason())
                .userId(cancelProductReservationCommand.getUserId())
                .build();

        AggregateLifecycle.apply(productReservationCancelledEvent);
    }

    @EventSourcingHandler
    public void on(ProductReservationCancelledEvent productReservationCancelledEvent) {

        log.info(String.format("ProductReservationCancelledEvent is called for productId [%s] ", productReservationCancelledEvent.getProductId()));

        this.quantity += productReservationCancelledEvent.getQuantity();

    }


    @EventSourcingHandler
    public void on(ProductCreatedEvent productCreatedEvent) {

        log.info(String.format("ProductCreatedEvent is called for productId [%s] ", productCreatedEvent.getProductId()));

        this.productId = productCreatedEvent.getProductId();
        this.title = productCreatedEvent.getTitle();
        this.price = productCreatedEvent.getPrice();
        this.quantity = productCreatedEvent.getQuantity();
    }

    @EventSourcingHandler
    public void on(ProductReservedEvent productReservedEvent) {

        log.info(String.format("ProductReservedEvent is called for productId [%s] ", productReservedEvent.getProductId()));

        this.quantity -= productReservedEvent.getQuantity();
    }

}
