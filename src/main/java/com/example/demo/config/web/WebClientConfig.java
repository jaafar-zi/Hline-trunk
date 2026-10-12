package com.example.demo.config.web;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient configuration for reactive HTTP calls
 *
 * Features:
 * - Non-blocking I/O
 * - Connection pooling
 * - Timeout configuration
 * - Request/response logging
 * - Retry mechanism
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Configuration
public class WebClientConfig {

    /**
     * Default WebClient with reactive HTTP client
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        log.info("Configuring default WebClient");

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(30))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS))
                );

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    /**
     * WebClient for external services with longer timeout
     */
    @Bean("externalWebClient")
    public WebClient externalWebClient(WebClient.Builder builder) {
        log.info("Configuring external WebClient");

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofSeconds(60))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS))
                );

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    /**
     * Request logging filter
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("WebClient Request: {} {}",
                    clientRequest.method(),
                    clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    /**
     * Response logging filter
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("WebClient Response: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}