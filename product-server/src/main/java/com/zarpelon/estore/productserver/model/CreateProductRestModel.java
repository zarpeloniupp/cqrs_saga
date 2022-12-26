package com.zarpelon.estore.productserver.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateProductRestModel {
    private String title;
    private BigDecimal price;
    private Integer quantity;

}
