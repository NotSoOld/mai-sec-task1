package com.notsoold.maisec.attacker;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class AttackerMainApplication {

    public static void main(String[] args) {
	new SpringApplicationBuilder()
			.sources(AttackerMainApplication.class)
			.profiles("attacker")
			.run(args);
    }

}
