package com.ktully.otel.simpleRestApiController.OtelUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class OtelInterceptorConfig extends WebMvcConfigurationSupport {
	
	@Autowired
	HttpServletTraceInterceptor httpServletTraceInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry){
		registry.addInterceptor(httpServletTraceInterceptor);
	}

}
