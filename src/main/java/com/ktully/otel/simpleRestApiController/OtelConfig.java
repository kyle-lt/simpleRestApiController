package com.ktully.otel.simpleRestApiController;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// OpenTelemetry
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

//import io.opentelemetry.api.metrics.MeterProvider;
//import io.opentelemetry.sdk.metrics.SdkMeterProvider;
//import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
//import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;

// OTLP Exporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;

@Configuration
public class OtelConfig {

	@Bean
	public static OpenTelemetry OpenTelemetryConfig() {

		// ** Create OTLP gRPC Span Exporter & BatchSpanProcessor **
		OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
				.setEndpoint("http://host.docker.internal:4317").setTimeout(2, TimeUnit.SECONDS).build();
		BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(spanExporter)
				.setScheduleDelay(100, TimeUnit.MILLISECONDS).build();

		AttributeKey<String> myServiceName = AttributeKey.stringKey("service.name");
		AttributeKey<String> myServiceNamespace = AttributeKey.stringKey("service.namespace");
		Resource serviceNameResource = Resource.create(Attributes.of(myServiceName,
				"simple-rest-api-controller", myServiceNamespace, "kjt-springboot"));

		// ** Create OpenTelemetry SdkTracerProvider
		// Use OTLP & Logging Exporters
		// Use Service Name Resource (and attributes) defined above
		// Use AlwaysOn TraceConfig
		SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder().addSpanProcessor(spanProcessor) // OTLP
				.addSpanProcessor(SimpleSpanProcessor.create(new LoggingSpanExporter()))
				.setResource(Resource.getDefault().merge(serviceNameResource)).setSampler(Sampler.alwaysOn()).build();

		// ** Create OpenTelemetry SDK **
		// Use W3C Trace Context Propagation
		// Use the SdkTracerProvider instantiated above
		OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder().setTracerProvider(sdkTracerProvider)
				.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance())).build();

		// ** Create Shutdown Hook **
		Runtime.getRuntime().addShutdownHook(new Thread(sdkTracerProvider::shutdown));

		return openTelemetrySdk;
	}
	
	/*
	@Bean
	public static MeterProvider OpenTelemetryMetricsConfig() {
		
		OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.getDefault();
			
	}
	*/
}
