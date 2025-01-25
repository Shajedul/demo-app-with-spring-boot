package com.example.battery_api;

import org.springframework.boot.SpringApplication;

public class TestBatteryApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(BatteryApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
