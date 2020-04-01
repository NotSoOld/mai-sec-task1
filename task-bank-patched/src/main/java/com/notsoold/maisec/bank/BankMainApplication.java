package com.notsoold.maisec.bank;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class BankMainApplication {

    public static void main(String[] args) {
	new SpringApplicationBuilder()
			.sources(BankMainApplication.class)
			.profiles("bank")
			.run(args);
    }

}