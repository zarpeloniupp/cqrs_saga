package com.zarpelon.estore.productserver.command.handller;

import com.zarpelon.estore.productserver.command.data.ProductLookupEntity;
import com.zarpelon.estore.productserver.command.data.ProductLookupRepository;
import com.zarpelon.estore.productserver.core.event.ProductCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductLookupEventsHandler {

    private final ProductLookupRepository productLookupRepository;

    public ProductLookupEventsHandler(ProductLookupRepository productLookupRepository){
        this.productLookupRepository = productLookupRepository;
    }

    @EventHandler
    public void on(ProductCreatedEvent event){
        productLookupRepository.save(new ProductLookupEntity(event.getProductId(), event.getTitle()));
    }
}
