package com.zarpelon.estore.productserver.query.handller;

import com.zarpelon.estore.productserver.core.data.ProductEntity;
import com.zarpelon.estore.productserver.core.data.ProductsRepository;
import com.zarpelon.estore.productserver.query.FindProductsQuery;
import com.zarpelon.estore.productserver.query.model.ProductRestModel;
import java.util.ArrayList;
import java.util.List;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ProductsQueryHandler {

    private final ProductsRepository productsRepository;

    public ProductsQueryHandler(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @QueryHandler
    public List<ProductRestModel> findProducts(FindProductsQuery query) {

        List<ProductRestModel> productRest = new ArrayList<>();

        List<ProductEntity> storedProducts = productsRepository.findAll();

        for (ProductEntity productEntity : storedProducts) {
            ProductRestModel productRestModel = new ProductRestModel();
            BeanUtils.copyProperties(productEntity, productRestModel);
            productRest.add(productRestModel);
        }
        return productRest;
    }
}
