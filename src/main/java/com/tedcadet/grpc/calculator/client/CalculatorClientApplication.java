package com.tedcadet.grpc.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CalculatorClientApplication {
    public static void main(String[] args) {
        CalculatorClient calculatorClient = new CalculatorClient();

        calculatorClient.run();
    }
}
