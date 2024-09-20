package com.ecacho.c8queryapi.config;

import io.camunda.zeebe.client.CredentialsProvider;
import io.camunda.zeebe.client.ZeebeClient;

import java.io.IOException;
import java.net.URI;


public class CamundaClientBuilder {
    private static String host = "ingress.local";
    private static final String zeebeGrpc = "grpc://" + host + ":443";
    private static final String zeebeRest = "http://" + host + ":8088";

    public static ZeebeClient build() throws Exception {
        return ZeebeClient.newClientBuilder()
                .grpcAddress(new URI(zeebeGrpc))
                .restAddress(new URI(zeebeRest))
                .credentialsProvider(getCredentials())
                .build();
    }

    public static ZeebeClient buildPlainText() throws Exception {
        return ZeebeClient.newClientBuilder()
                .grpcAddress(new URI(zeebeGrpc))
                .restAddress(new URI(zeebeRest))
                .credentialsProvider(getCredentials())
                .usePlaintext()
                .build();
    }

    private static CredentialsProvider getCredentials() {
        return new CredentialsProvider() {
            @Override
            public void applyCredentials(CredentialsApplier applier) throws IOException {
                applier.put("Authorization", "Basic ZGVtbzpkZW1v");
            }

            @Override
            public boolean shouldRetryRequest(StatusCode statusCode) {
                return false;
            }

        };
    }
}
