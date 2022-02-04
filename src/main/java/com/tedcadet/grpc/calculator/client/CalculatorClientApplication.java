package com.tedcadet.grpc.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CalculatorClientApplication {
    public static void main(String[] args) {

        // instancie un nouveau client
        CalculatorClient calculatorClient = new CalculatorClient();

        // run le client
        calculatorClient.run();
    }
}
