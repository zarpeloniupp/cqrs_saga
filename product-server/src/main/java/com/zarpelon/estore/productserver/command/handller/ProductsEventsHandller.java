package com.zarpelon.estore.productserver.command.handller;

import com.zarpelon.estore.productserver.core.data.ProductEntity;
import com.zarpelon.estore.productserver.core.data.ProductRepository;
import com.zarpelon.estore.productserver.core.event.ProductCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ProcessingGroup("product-group")
public class ProductsEventsHandller {

    private final ProductRepository productRepository;

    public ProductsEventsHandller(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @EventHandler
    public void on(ProductCreatedEvent event) {

        ProductEntity productEntity = new ProductEntity();

        BeanUtils.copyProperties(event, productEntity);

        productRepository.save(productEntity);
    }
}
