package com.tedcadet.grpc.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {
    ManagedChannel channel;

    //TODO: generaliser la creation des clients

    public void run() {
        // cree un channel pour communiquer sur localhost:50052
        channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        doSum();

        doPrimeDecomposer();

        doAverage();

        // shutdown the client
        System.out.println("Shutting down client");
        channel.shutdown();
    }

    private void doAverage() {

        // Client Streaming
        // cree un non-blocking calculator client
        CalculatorServiceGrpc.CalculatorServiceStub calculatorClient = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        // create a requestObserver
        StreamObserver<AverageRequest> requestObserver = calculatorClient.computeAverage(new StreamObserver<AverageResponse>() {
            @Override
            public void onNext(AverageResponse value) {
                System.out.println("The average is: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });


        // Liste de nombre a evaluer
        List<Integer> numbers = Arrays.asList(1,2,3,4);

        numbers.forEach(number -> {
            System.out.println("Sending number: " + number);
            // create request
            AverageRequest request = AverageRequest
                    .newBuilder()
                    .setNumber(number)
                    .build();

            requestObserver.onNext(request);
        });

        // then close the requestObserver
        requestObserver.onCompleted();

        try {
            // wait till the latch is at o
            latch.await(3L, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doPrimeDecomposer() {

        // Server Streaming

        // cree un blocking calculator client
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        // instanciate a request
        long number = 1237243553L;

        PNDRequest request = PNDRequest.newBuilder()
                .setNumber(number)
                .build();

        // send a request, get and use the response
        System.out.printf("the prime numbers decomposition of %d%n", number);
        calculatorClient.primeNumberDecomposition(request)
                .forEachRemaining( response -> {
                    System.out.println(response);
                });

    }

    private void doSum() {
        // cree un blocking calculator client
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        // cree un request
        int first = 10;
        int second = 3;

        System.out.println("first int: " + first);
        System.out.println("first second: " + second);
        TwoIntegers twoIntegers = TwoIntegers.newBuilder()
                .setFirst(first)
                .setSecond(second)
                .build();

        CalculatorRequest calculatorRequest = CalculatorRequest.newBuilder()
                .setTwoIntegers(twoIntegers)
                .build();

        // use the rpc call from the client
        CalculatorResponse calculatorResponse = calculatorClient.sum(calculatorRequest);

        // get the result and print it
        int result = calculatorResponse.getResult();
        System.out.println("the sum is " + result);
    }
}
