package br.com.viavarejo.utils;

import redis.clients.jedis.JedisCommands;

public final class Util {
    public static final String KeyPrefix = "Benchmark%s";
    private static final String BenchmarkKeysCreated = "BenchmarkKeysCreated";

    private Util() {
    }

    public static void createOneMillionOfKeys() {
        JedisCommands commands = JedisConnectionManagement.getCommands();
        String keysCreated = commands.get(BenchmarkKeysCreated);
        if (keysCreated != null && keysCreated.equals("y")) {
            return;
        }

        String data = BenchmarkConfiguration.get().getKeyContentData();
        Integer amountOfKeys = BenchmarkConfiguration.get().getAmountOfKeys();
        for (int i = 0; i <= amountOfKeys; i++) {
            progressPercentage(i, amountOfKeys);
            try {
                String keyName = String.format(Util.KeyPrefix, i);
                commands.set(keyName, data);
            } catch (Exception e) {
                i--;
                commands = JedisConnectionManagement.getCommands();
                e.printStackTrace();
            }
        }

        commands.set(BenchmarkKeysCreated, "y");
        commands.expire(BenchmarkKeysCreated, 28800);
    }

    private static void progressPercentage(int done, int total) {
        int size = 5;

        String iconLeftBoundary = "[";
        String iconDone = "=";
        String iconRemain = ".";
        String iconRightBoundary = "]";

        if (done > total) {
            throw new IllegalArgumentException();
        }

        int donePercents = (100 * done) / total;
        int doneLength = size * donePercents / 100;

        StringBuilder bar = new StringBuilder(iconLeftBoundary);
        for (int i = 0; i < size; i++) {
            if (i < doneLength) {
                bar.append(iconDone);
            } else {
                bar.append(iconRemain);
            }
        }

        bar.append(iconRightBoundary);
        System.out.print("\r" + String.format("Creating %s of %s key(s) to the benchmark: ", done, total) + " " + bar + " " + donePercents + "%");

        if (done == total) {
            System.out.print("\n");
        }
    }
}