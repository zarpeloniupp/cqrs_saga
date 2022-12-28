package com.zarpelon.estore.productserver.command.model;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Builder
@Data
public class CreateProductCommand {
    @TargetAggregateIdentifier
    private final String productId;
    private final String title;
    private final BigDecimal price;
    private final Integer quantity;
}


// NoumVerbCommand