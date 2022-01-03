package com.tedcadet.grpc.calculator.server;

import com.proto.calculator.*;
import com.tedcadet.grpc.calculator.utils.Operations;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {
    @Override
    public void sum(CalculatorRequest request, StreamObserver<CalculatorResponse> responseObserver) {
//        super.sum(request, responseObserver);
        // get the integers from the request
        TwoIntegers twoIntegers = request.getTwoIntegers();
        int first = twoIntegers.getFirst();
        int second = twoIntegers.getSecond();

        // do the sum
        int result = first + second;

        // set the response
        CalculatorResponse calculatorResponse = CalculatorResponse.newBuilder()
                .setResult(result)
                .build();

        // send the response
        responseObserver.onNext(calculatorResponse);

        // complete the call
        responseObserver.onCompleted();
    }

    @Override
    public void primeNumberDecomposition(PNDRequest request, StreamObserver<PNDResponse> responseObserver) {
        // get the number from the request
        long number = request.getNumber();

        // do the decomposition
        int k = 2;
        while (number > 1) {
            if (number % k == 0) {

                // set the response
                PNDResponse response = PNDResponse.newBuilder()
                        .setResult(k)
                        .build();

                // send response
                responseObserver.onNext(response);

                number = number / k;
            } else {
                k = k + 1;
            }
        }

        // complete the call
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<AverageRequest> computeAverage(StreamObserver<AverageResponse> responseObserver) {

        // create a requestObserver
        StreamObserver<AverageRequest> requestObserver = new StreamObserver<AverageRequest>() {
            Stream<Integer> numbers = Stream.empty();

            @Override
            public void onNext(AverageRequest value) {
                int number = value.getNumber();
                System.out.println("next number: " + number);
                numbers = Stream.concat(numbers, Stream.of(number));
                System.out.println("The updated list: " + numbers);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                // create a response
                AverageResponse response = AverageResponse
                        .newBuilder()
                        .setResult(Operations.average(numbers.collect(Collectors.toList())))
                        .build();

                // return the response through the ressponseObserver
                responseObserver.onNext(response);

                // complete the call
                responseObserver.onCompleted();
                System.out.println("call completed");
            }
        };

        return requestObserver;
    }

    @Override
    public StreamObserver<MaxRequest> findMaximum(StreamObserver<MaxResponse> responseObserver) {



        StreamObserver<MaxRequest> requestObserver = new StreamObserver<MaxRequest>() {
            int max = 0;

            @Override
            public void onNext(MaxRequest value) {
                int number = value.getNumber();

                if(number > max) {
                    // create a response
                    MaxResponse response = MaxResponse
                            .newBuilder()
                            .setResult(number)
                            .build();

                    responseObserver.onNext(response);

                    max = number;
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };

        return requestObserver;
    }
}
