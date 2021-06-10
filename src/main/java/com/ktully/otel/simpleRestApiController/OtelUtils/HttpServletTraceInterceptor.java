package com.ktully.otel.simpleRestApiController.OtelUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;

@Component
public class HttpServletTraceInterceptor implements HandlerInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(HttpServletTraceInterceptor.class);

	// Otel Tracer instantiation
	// TODO: get Tracer name from ENV VAR or application.properties
	// TODO: figure out how to use GlobalOpenTelemetry in Spring Boot
	//public static final OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
	public static final OpenTelemetry openTelemetry = OtelConfig.OpenTelemetryConfig();
	private static String tracerName = "com.ktully.otel.simpleRestApiController.BaseController";
	//private static final Tracer tracer = GlobalOpenTelemetry.getTracer(tracerName);
	private static final Tracer tracer = openTelemetry.getTracer(tracerName);
	Span serverSpan = null;

	/*
	 * Configuration for Context Propagation to be done via HttpServletRequest
	 * extraction
	 */
	TextMapGetter<HttpServletRequest> getter = new TextMapGetter<HttpServletRequest>() {
		@Override
		public String get(HttpServletRequest carrier, String key) {
			logger.debug("TextMapGetter called to extract propagation context from HttpServletRequest HttpHeader.");
			return carrier.getHeader(key);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Iterable<String> keys(HttpServletRequest carrier) {
			return (Iterable<String>) carrier.getHeaderNames();
		}
	};

	/*
	 * Configuration for Context Propagation to be done via HttpHeaders injection
	 */
	TextMapSetter<HttpServletResponse> setter = new TextMapSetter<HttpServletResponse>() {

		@Override
		public void set(HttpServletResponse carrier, String key, String value) {
			logger.debug("TextMapSetter called to inject propagation context into HttpServletResponse HttpHeader.");
			logger.debug("Adding Header Name = " + key);
			logger.debug("Adding Header Value = " + value);
			carrier.setHeader(key, value);
		}
	};

	/*
	 * preHandle method extracts incoming propagation context
	 * from @HttpServletRequest @HttpHeaders to utilize in its Span, and injects
	 * propagation context into @HttpServletResponse @HttpHeaders
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		/*
		 * Extract Context from @HttpServletRequest
		 */
		Context extractedContext = null;
		try {
			logger.debug("Trying to extact Context Propagation Headers from HttpServletRequest");
			extractedContext = openTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), request,
					getter);

			logger.debug("Extracted Context = " + extractedContext.toString());
		} catch (Exception e) {
			logger.error("Exception caught while extracting Context Propagators, using Context.current()", e);
			extractedContext = Context.current();
		}

		String requestMethod = request.getMethod();
		String requestScheme = request.getScheme();
		String requestUri = request.getRequestURI();
		String requestHost = request.getRemoteHost();
		String requestSpanName = requestMethod + " " + requestUri;
		serverSpan = tracer.spanBuilder(requestSpanName).setParent(extractedContext).setSpanKind(SpanKind.SERVER)
				.startSpan();
		try (Scope scope = serverSpan.makeCurrent()) {

			// Add some "Events" (AKA logs) to the span
			serverSpan.addEvent("This is an event with no Attributes");
			AttributeKey<String> attrKey = AttributeKey.stringKey("attrKey");
			Attributes spanEventAttr = Attributes.of(attrKey, "attrVal");
			serverSpan.addEvent("This is an event with an Attributes String Array", spanEventAttr);

			// Add the attributes defined in the Semantic Conventions
			serverSpan.setAttribute("http.method", requestMethod);
			serverSpan.setAttribute("http.scheme", requestScheme);
			serverSpan.setAttribute("http.host", requestHost);
			serverSpan.setAttribute("http.target", requestUri);

		} catch (Exception e) {
			logger.error("Exception caught attempting to create Span", e);
			return true;
		}

		/*
		 * Inject Context into @HttpServletResponse
		 * 
		 * This code was initially inside of the postHandle method, but it did not allow
		 * me to add the headers there using a @RestController, so let's try here...
		 */
		try (Scope scope = serverSpan.makeCurrent()) {
			logger.debug("Trying to inject Context Propagation Headers into HttpServletResponse");
			openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), response, setter);
			response.addHeader("kjt-was-traced", "true");
		} catch (Exception e) {
			logger.error("Exception caught while injecting Context Propagators.", e);
		}

		// DEBUG LOGGING
		/*
		 * Collection<String> headerNames = response.getHeaderNames(); for
		 * (Iterator<String> iterator = headerNames.iterator(); iterator.hasNext();) {
		 * String headerName = iterator.next(); logger.debug("header Name = " +
		 * headerName); logger.debug("header Value = " +
		 * response.getHeader(headerName)); }
		 */

		return true;
	}

	/*
	 * postHandle method is not implemented in any way
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

	}

	/*
	 * afterCompletion method ends the Span
	 */
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception exception) throws Exception {

		if (serverSpan != null) {
			serverSpan.end();
		}

	}

}
