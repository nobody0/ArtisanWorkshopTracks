package com.sig.rs.utils;

import java.util.Random;

public class Utils {
    private static Utils ourInstance = new Utils();

    private Random r = new Random();

    public static Utils getInstance() {
        return ourInstance;
    }

    private Utils() {
    }

    public int rand(int max)
    {
        return rand(0, max);
    }

    public int rand(int min, int max)
    {
        return min + r.nextInt(max);
    }
}
