package com.zarpelon.estore.productserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class ProductServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServerApplication.class, args);
	}

}
