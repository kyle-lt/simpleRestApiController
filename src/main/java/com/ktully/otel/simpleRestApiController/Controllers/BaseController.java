package com.ktully.otel.simpleRestApiController.Controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.ktully.otel.simpleRestApiController.OtelConfig;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;

@RestController
public class BaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(BaseController.class);
	
	public static final OpenTelemetry openTelemetry = OtelConfig.OpenTelemetryConfig();
	
	private static final Tracer tracer = openTelemetry.getTracer("com.ktully.otel.simpleRestApiController.BaseController");
	
	@GetMapping("/")
	public String baseUrl(@RequestHeader Map<String, String> headers) {
		return "it works";
	}

}
