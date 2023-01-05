package com.zarpelon.estore.productserver;

import com.thoughtworks.xstream.XStream;
import com.zarpelon.estore.productserver.command.CreateProductCommandInterceptor;
import com.zarpelon.estore.productserver.core.errohandler.ProductsServiceEventsErrorHandler;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@EnableEurekaClient
@SpringBootApplication
public class ProductServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServerApplication.class, args);
	}

	@Autowired
	public void registerCreateProductCommandInterceptor(ApplicationContext context, CommandBus commandBus){
		commandBus.registerDispatchInterceptor(context.getBean(CreateProductCommandInterceptor.class));
	}

	@Autowired
	public void configure(EventProcessingConfigurer config) {
		config.registerListenerInvocationErrorHandler("product-group", configuration -> new ProductsServiceEventsErrorHandler());
		//	config.registerListenerInvocationErrorHandler("product-group", configuration -> PropagatingErrorHandler.instance());
	}
}
