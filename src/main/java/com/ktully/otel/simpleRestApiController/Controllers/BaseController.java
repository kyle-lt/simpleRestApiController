package com.ktully.otel.simpleRestApiController.Controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

//import com.ktully.otel.simpleRestApiController.OtelUtils.OtelConfig;

//import io.opentelemetry.api.metrics.LongCounter;
//import io.opentelemetry.api.metrics.LongValueRecorder;
//import io.opentelemetry.api.metrics.MeterProvider;

@RestController
public class BaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(BaseController.class);
	
	
	// OpenTelemetry Metrics Recorders
	//MeterProvider meterProvider = OtelConfig.OpenTelemetryMetricsConfig();
	//LongCounter counter = meterProvider.get("com.ktully.otel.simpleRestApiController.Controllers.BaseController").longCounterBuilder("requests_counter").build();
	//LongValueRecorder recorder = meterProvider.get("com.ktully.otel.simpleRestApiController.Controllers.BaseController").longValueRecorderBuilder("method_timer").setUnit("ms").build();
	//LongCounter counter = GlobalMeterProvider.getMeter("com.ktully.otel.simpleRestApiController.Controllers.BaseController").longCounterBuilder("requests_counter").build();
	//LongValueRecorder recorder = GlobalMeterProvider.getMeter("com.ktully.otel.simpleRestApiController.Controllers.BaseController").longValueRecorderBuilder("method_timer").setUnit("ms").build();
	
	@GetMapping("/")
	public String baseUrl(@RequestHeader Map<String, String> headers) {
		//long startTime = System.currentTimeMillis();
		logger.debug("BaseController Base URL called.");
		//counter.add(1);
		//recorder.record(System.currentTimeMillis() - startTime);
		return "base url called.";
	}
	
	@GetMapping("/derp")
	public String derpUrl() {
		logger.debug("BaseController Derp URL called.");
		return "derp url called.";
	}

}
