package com.notsoold.maisec.forum;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ForumMainApplication {

    public static void main(String[] args) {
	new SpringApplicationBuilder()
			.sources(ForumMainApplication.class)
			.profiles("forum")
			.run(args);
    }

}
