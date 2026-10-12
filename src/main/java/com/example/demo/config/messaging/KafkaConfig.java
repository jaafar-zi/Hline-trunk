// package com.example.demo.config.messaging;

// import lombok.extern.slf4j.Slf4j;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.kafka.annotation.EnableKafka;
// import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
// import org.springframework.kafka.core.ConsumerFactory;
// import org.springframework.kafka.listener.DefaultErrorHandler;
// import org.springframework.kafka.support.converter.RecordMessageConverter;
// import org.springframework.kafka.support.converter.StringJsonMessageConverter;
// import org.springframework.util.backoff.FixedBackOff;

// /**
//  * Main Kafka configuration
//  *
//  * @author Architecture Team
//  * @version 1.0
//  */
// @Slf4j
// @Configuration
// @EnableKafka
// public class KafkaConfig {

//     /**
//      * Message converter for JSON serialization/deserialization
//      */
//     @Bean
//     public RecordMessageConverter messageConverter() {
//         return new StringJsonMessageConverter();
//     }

//     /**
//      * Error handler with retry logic for Kafka consumers
//      * 
//      * Note: Named specifically to avoid conflict with Cache's errorHandler
//      */
//     @Bean("kafkaErrorHandler") 
//     public DefaultErrorHandler kafkaErrorHandler() {  
//         // Retry 3 times with 1 second interval
//         DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(1000L, 3L));
        
//         log.info("Kafka error handler configured with 3 retries and 1 second backoff");
        
//         return errorHandler;
//     }

//     /**
//      * Kafka listener container factory with JSON support
//      */
//     @Bean
//     public ConcurrentKafkaListenerContainerFactory<String, Object>
//     kafkaListenerContainerFactory(
//             ConsumerFactory<String, Object> consumerFactory,
//             RecordMessageConverter messageConverter
//     ) {
//         ConcurrentKafkaListenerContainerFactory<String, Object> factory =
//                 new ConcurrentKafkaListenerContainerFactory<>();

//         factory.setConsumerFactory(consumerFactory);
//         factory.setRecordMessageConverter(messageConverter);
//         factory.setCommonErrorHandler(kafkaErrorHandler()); 

//         log.info("Kafka listener container factory configured successfully");

//         return factory;
//     }
// }