package com.smartattendance.util.security;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Utility to generate random string
 * 
 * @author Thiha Swan Htet
 */
public class RandomUtil {
    private static SecureRandom secureRandom = new SecureRandom();

    public static String generateToken(int bitLength) {
        return new BigInteger(bitLength, secureRandom).toString(32);
    }
}
