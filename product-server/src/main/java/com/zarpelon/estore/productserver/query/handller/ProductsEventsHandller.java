package com.zarpelon.estore.productserver.query.handller;

import com.zarpelon.estore.productserver.core.data.ProductEntity;
import com.zarpelon.estore.productserver.core.data.ProductsRepository;
import com.zarpelon.estore.productserver.core.event.ProductCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductsEventsHandller {

    private final ProductsRepository productsRepository;

    public ProductsEventsHandller(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @EventHandler
    public void on(ProductCreatedEvent event){

        ProductEntity productEntity = new ProductEntity();

        BeanUtils.copyProperties(event, productEntity);

        if(!productsRepository.findByProductIdOrTitle(productEntity.getProductId(),productEntity.getTitle()).isPresent()){
            productsRepository.save(productEntity);
        }

    }
}
