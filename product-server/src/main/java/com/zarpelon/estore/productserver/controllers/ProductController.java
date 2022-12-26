package com.zarpelon.estore.productserver.controllers;

import com.zarpelon.estore.productserver.command.CreateProductCommand;
import com.zarpelon.estore.productserver.model.CreateProductRestModel;
import java.util.UUID;
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
public class ProductController {

    private final Environment environment;
    private final CommandGateway commandGateway;

    @Autowired
    public ProductController(Environment environment, CommandGateway commandGateway) {
        this.environment = environment;
        this.commandGateway = commandGateway;
    }

    @GetMapping
    public String get() {
        return "GET Products";
    }

    @PostMapping
    public String createProduct(@RequestBody CreateProductRestModel createProductRestModel) {
        var createProductCommand = CreateProductCommand.builder()
                .price(createProductRestModel.getPrice())
                .quantity(createProductRestModel.getQuantity())
                .title(createProductRestModel.getTitle())
                .productId(UUID.randomUUID().toString())
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
        return "GET info" + environment.getProperty("local.server.port");
    }
}
