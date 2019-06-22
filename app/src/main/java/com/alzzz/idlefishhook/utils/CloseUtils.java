package com.alzzz.idlefishhook.utils;

import java.io.Closeable;
import java.io.IOException;

public class CloseUtils {

    public static void close(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (null != closeable) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    LOGGER.d("CloseUtils", "close is exception");
                }
            }
        }
    }
}
