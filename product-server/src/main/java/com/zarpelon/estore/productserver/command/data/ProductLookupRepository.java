package com.zarpelon.estore.productserver.command.data;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductLookupRepository extends JpaRepository<ProductLookupEntity, String> {

    Optional<ProductLookupEntity> findByProductIdOrTitle(String productId, String title);


}
