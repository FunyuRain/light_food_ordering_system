package com.lightfood.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderNoGeneratorTest {

    @Test
    void generatedNumbersAreShortAndUnique() {
        Set<String> numbers = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            String number = OrderNoGenerator.generate();
            assertEquals(22, number.length());
            assertTrue(number.matches("\\d{14}[0-9A-F]{8}"));
            numbers.add(number);
        }
        assertEquals(200, numbers.size());
    }
}
