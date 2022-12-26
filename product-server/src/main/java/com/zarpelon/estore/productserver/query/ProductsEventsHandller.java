package com.zarpelon.estore.productserver.query;

import com.zarpelon.estore.productserver.core.data.ProductEntity;
import com.zarpelon.estore.productserver.core.data.ProductsRepository;
import com.zarpelon.estore.productserver.core.event.ProductCreatedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ProductsEventsHandller {

    private final ProductsRepository productsRepository;

    public ProductsEventsHandller(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @EventHandler
    public void on(ProductCreatedEvent event){

        ProductEntity productEntity = new ProductEntity();

        BeanUtils.copyProperties(event, productEntity);

        productsRepository.save(productEntity);
    }
}
