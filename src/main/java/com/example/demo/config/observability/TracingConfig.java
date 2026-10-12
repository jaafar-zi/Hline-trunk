package com.example.demo.config.observability;

import brave.Tracing;
import brave.handler.SpanHandler;
import brave.propagation.B3Propagation;
import brave.propagation.CurrentTraceContext;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

/**
 * Distributed tracing configuration with Zipkin
 */
@Slf4j
@Configuration
public class TracingConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${management.zipkin.tracing.endpoint:http://localhost:9411/api/v2/spans}")
    private String zipkinEndpoint;

    @Value("${management.tracing.sampling.probability:0.1}")
    private float samplingProbability;

    @Bean
    public CurrentTraceContext currentTraceContext() {
        return ThreadLocalCurrentTraceContext.newBuilder()
                .build();
    }

    @Bean
    public Tracing tracing(CurrentTraceContext currentTraceContext, SpanHandler spanHandler) {
        log.info("Configuring distributed tracing: service={}, zipkin={}, sampling={}",
                applicationName, zipkinEndpoint, samplingProbability);

        return Tracing.newBuilder()
                .localServiceName(applicationName)
                .currentTraceContext(currentTraceContext)
                .propagationFactory(B3Propagation.newFactoryBuilder()
                        .injectFormat(B3Propagation.Format.SINGLE)
                        .build())
                .sampler(Sampler.create(samplingProbability))
                .addSpanHandler(spanHandler)
                .build();
    }

    @Bean
    public SpanHandler spanHandler() {
        URLConnectionSender sender = URLConnectionSender.newBuilder()
                .endpoint(zipkinEndpoint)
                .connectTimeout(1000)
                .readTimeout(5000)
                .build();

        AsyncReporter<zipkin2.Span> reporter = AsyncReporter.builder(sender)
                .build();

        return zipkin2.reporter.brave.AsyncZipkinSpanHandler.create(reporter);
    }

    @Bean
    public brave.Tracer tracer(Tracing tracing) {
        return tracing.tracer();
    }
}