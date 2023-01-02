package com.zarpelon.estore.productserver.command;

import com.zarpelon.estore.productserver.command.model.CreateProductCommand;
import com.zarpelon.estore.productserver.command.data.ProductLookupEntity;
import com.zarpelon.estore.productserver.command.data.ProductLookupRepository;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreateProductCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

    private final ProductLookupRepository productLookupRepository;

    @Autowired
    public CreateProductCommandInterceptor(ProductLookupRepository productLookupRepository) {
        this.productLookupRepository = productLookupRepository;
    }

    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(@Nonnull List<? extends CommandMessage<?>> list) {

        return (index, command) -> {

            log.info("Interceptor command: "+command.getPayloadType());

            if (CreateProductCommand.class.equals(command.getPayloadType())){

                CreateProductCommand createProductCommand = (CreateProductCommand) command.getPayload();

                Optional<ProductLookupEntity> productLookupEntity = productLookupRepository
                        .findByProductIdOrTitle(createProductCommand.getProductId(), createProductCommand.getTitle());

                productLookupEntity.ifPresent(it -> {
                    throw new IllegalStateException(
                            String.format("-> Product with productId %s or title %s already exist", createProductCommand.getProductId(), createProductCommand.getTitle())
                    );
                });

//                if (createProductCommand.getPrice().compareTo(BigDecimal.ZERO) <=0) {
//                    throw new IllegalArgumentException("price cannot be less opr equal than zero");
//                }
//                if (createProductCommand.getTitle() == null
//                        || createProductCommand.getTitle().isBlank()) {
//                    throw new IllegalArgumentException("title cannot be empty");
//                }
            }
            return command;
        };
    }
}
