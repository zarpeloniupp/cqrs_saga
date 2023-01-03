package com.zarpelon.estore.productserver.command;

import com.appsdeveloperblog.estore.core.commands.ReverseProductCommand;
import com.appsdeveloperblog.estore.core.events.ProductReservedEvent;
import com.zarpelon.estore.productserver.command.model.CreateProductCommand;
import com.zarpelon.estore.productserver.command.model.ProductCreatedEvent;
import java.math.BigDecimal;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@Aggregate
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

    @EventSourcingHandler
    public void on(ProductCreatedEvent productCreatedEvent) {
        this.productId = productCreatedEvent.getProductId();
        this.title = productCreatedEvent.getTitle();
        this.price = productCreatedEvent.getPrice();
        this.quantity = productCreatedEvent.getQuantity();
    }

    @EventSourcingHandler
    public void on(ProductReservedEvent productReservedEvent) {
        this.quantity -= productReservedEvent.getQuantity();
    }

}
