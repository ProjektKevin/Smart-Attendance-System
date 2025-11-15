package com.smartattendance.util.security;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RandomUtil {
    private static SecureRandom secureRandom = new SecureRandom();

    public static String generateToken(int bitLength) {
        return new BigInteger(bitLength, secureRandom).toString(32);
    }
}
