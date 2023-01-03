package com.zarpelon.estore.productserver.command.handller;

import com.appsdeveloperblog.estore.core.events.ProductReservedEvent;
import com.zarpelon.estore.productserver.command.model.ProductCreatedEvent;
import com.zarpelon.estore.productserver.core.data.ProductEntity;
import com.zarpelon.estore.productserver.core.data.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
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

    @ExceptionHandler(resultType = Exception.class)
    public void handle(Exception exception) throws Exception {
        throw exception;
    }


    @ExceptionHandler(resultType = IllegalArgumentException.class)
    public void handle(IllegalArgumentException exception){
        //logError
    }

    @EventHandler
    public void on(ProductCreatedEvent event) throws Exception {

        ProductEntity productEntity = new ProductEntity();

        BeanUtils.copyProperties(event, productEntity);

        try{
            productRepository.save(productEntity);
        }catch (Exception ex) {
            ex.getStackTrace();
        }

       // if(true) throw new Exception("forcing excepction");

    }

    @EventHandler
    public void on(ProductReservedEvent productReservedEvent) {
        var productEntity = productRepository.findByProductId(productReservedEvent.getProductId());
        productEntity.ifPresent(it ->{
            it.setQuantity(it.getQuantity() - productReservedEvent.getQuantity());
            productRepository.save(productEntity.get());
        });
        log.info(String.format("ProductReservedEvent is called for productId [%s] and orderId [%s]",productReservedEvent.getProductId(), productReservedEvent.getOrderId()));
    }



}
