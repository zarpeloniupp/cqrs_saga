package com.zarpelon.estore.productserver.core.data;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepository extends JpaRepository<ProductEntity, String> {

    Optional<ProductEntity> findByProductId(String productId);

    Optional<ProductEntity> findByProductIdOrTitle(String productId, String title);

}
