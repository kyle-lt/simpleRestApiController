package com.ktully.otel.simpleRestApiController.Controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(BaseController.class);
	
	@GetMapping("*")
	public String baseUrl(@RequestHeader Map<String, String> headers) {
		logger.debug("BaseController called.");
		return "it works for any URL!";
	}

}
