package com.appsdeveloperblog.estore.OrdersService.command.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
public class ApproveOrderCommand {
    @TargetAggregateIdentifier
    public final String orderId;

}
