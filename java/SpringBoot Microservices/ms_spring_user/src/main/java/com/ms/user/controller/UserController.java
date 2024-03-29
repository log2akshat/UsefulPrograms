package com.ms.user.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ms.user.request.PaymentRequest;
import com.ms.user.request.UserRegisterRequest;
import com.ms.user.response.OrderRegisterResponse;
import com.ms.user.response.UserRegisterResponse;
import com.ms.user.service.UserService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
public class UserController {

	Logger logger = LogManager.getLogger(UserController.class);

	@Autowired
	UserService service;

	@PostMapping("/register")
	public String registerUser(@RequestBody UserRegisterRequest request) {
		logger.info("Invoking UserService:registerUser with request: " + request);
		return service.registerUser(request);
	}

	@GetMapping("/userid/{userId}")
	public ResponseEntity<UserRegisterResponse> getUser(@PathVariable String userId) {
		logger.info("Fetching userdetails with the provided userId: " + userId);
		UserRegisterResponse response = service.getUser(userId);
		if (response != null) {
			return new ResponseEntity<UserRegisterResponse>(response, HttpStatus.OK);
		}
		return new ResponseEntity<UserRegisterResponse>(response, HttpStatus.NOT_FOUND);
	}

	// Consuming Payment Microservice using Feign Client
	@PostMapping("/make")
	@CircuitBreaker(name="payment-service", fallbackMethod = "makePayment")
	public String makePayment(@RequestBody PaymentRequest request) {
		logger.info("Consuming Payment Microservice using Feign Client, request: " + request);
		return "Generated ID: " + service.makePayment(request);
	}

	// Consuming Order Microservice using Feign Client
	@GetMapping("/orderid/{orderId}")
	public ResponseEntity<OrderRegisterResponse> getOrderServiceUser(@PathVariable String orderId) {
		logger.info("Consuming Order Microservice using Feign Client for the orderId: " + orderId);
		return service.getOrderServiceUser(orderId);
	}

	// Circuit Breaker Fault Tolerance method
	public String makePayment(Throwable exp) {
		logger.fatal("Payment Microservice is down, Enabling CIrcuit Breaker");
		return "There is issue with Payment Gateway, Please try again later..";
	}
}
