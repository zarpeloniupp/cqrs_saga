package com.zarpelon.estore.productserver.command.rest;


import com.zarpelon.estore.productserver.command.CreateProductCommand;
import com.zarpelon.estore.productserver.core.model.CreateProductRestModel;
import java.util.UUID;
import javax.validation.Valid;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductCommandController {

    private final Environment environment;
    private final CommandGateway commandGateway;

    @Autowired
    public ProductCommandController(Environment environment, CommandGateway commandGateway) {
        this.environment = environment;
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public String createProduct(@Valid @RequestBody CreateProductRestModel createProductRestModel) {
        var createProductCommand = CreateProductCommand.builder()
                .price(createProductRestModel.getPrice())
                .quantity(createProductRestModel.getQuantity())
                .title(createProductRestModel.getTitle())
                .productId(UUID.randomUUID().toString()+"MZ")
                .build();

        String returnValue = null;

        try {
            returnValue = commandGateway.sendAndWait(createProductCommand);
        } catch (Exception e){
            returnValue = e.getLocalizedMessage();
        }

        return returnValue;
    }

    @GetMapping("/info")
    public String getInfo() {
        return "GET info: " + environment.getProperty("local.server.port");
    }
}
