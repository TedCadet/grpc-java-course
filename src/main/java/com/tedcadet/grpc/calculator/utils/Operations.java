package com.tedcadet.grpc.calculator.utils;

import java.util.List;

public class Operations {

    public static double average(List<Integer> numbers) {

        double result = 0.0;
        int addup = 0;

        // it doesn't take into account that numbers could contain a null, it's just a quick thang
        for(int i = 0; i < numbers.size(); i++) {
            addup += numbers.get(i);
        }

        result = (double) addup  / numbers.size();

        return result;
    }
}
