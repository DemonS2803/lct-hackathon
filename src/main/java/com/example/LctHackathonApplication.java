package com.example;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LctHackathonApplication {

	public static void main(String[] args) {
		SpringApplication.run(LctHackathonApplication.class, args);
		System.out.println("hello world!");
		System.out.println("password: " + DigestUtils.sha256Hex("password"));

	}

}
