package com.enlace.infrastructure.aws;

import com.enlace.domain.port.out.ProvisioningPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.UUID;

@Component
public class SqsProvisioningPublisher implements ProvisioningPublisher {

    private final SqsAsyncClient sqsAsyncClient;

    @Value("${aws.sqs.provisioning-queue-url}")
    private String queueUrl;

    public SqsProvisioningPublisher(SqsAsyncClient sqsAsyncClient) {
        this.sqsAsyncClient = sqsAsyncClient;
    }

    @Override
    public void publishProvisioningJob(UUID eventId) {
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(eventId.toString())
                .build();

        try {
            sqsAsyncClient.sendMessage(request);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}