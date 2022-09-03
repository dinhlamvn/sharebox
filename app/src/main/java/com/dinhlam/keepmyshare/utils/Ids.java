package com.dinhlam.keepmyshare.utils;

import androidx.annotation.Nullable;

public final class Ids {
    private Ids() {
    }
    
    public static long hashLong64Bit(long value) {
        value ^= (value << 21);
        value ^= (value >>> 35);
        value ^= (value << 4);
        return value;
    }

    public static long hashString64Bit(@Nullable CharSequence str) {
        if (str == null) {
            return 0;
        }

        long result = 0xcbf29ce484222325L;
        final int len = str.length();
        for (int i = 0; i < len; i++) {
            result ^= str.charAt(i);
            result *= 0x100000001b3L;
        }
        return result;
    }
}
