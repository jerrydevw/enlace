//package com.enlace.infrastructure.config;
//
//import io.awspring.cloud.sqs.config.SqsListenerConfigurer;
//import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.time.Duration;
//
//@Configuration
//public class SqsConfig {
//
//    @Bean
//    public SqsListenerConfigurer configurer() {
//        return registrar -> registrar.setDefaultListenerContainerFactory(
//                SqsMessageListenerContainerFactory.builder()
//                        .configure(options -> options
//                                .maxConcurrentMessages(10)       // equivalente ao seu ExecutorService
//                                .pollTimeout(Duration.ofSeconds(20)) // equivalente ao waitTimeSeconds
//                                .maxMessagesPerPoll(10)          // equivalente ao maxNumberOfMessages
//                        )
//                        .sqsAsyncClient(sqsAsyncClient())
//                        .build()
//        );
//    }
//}