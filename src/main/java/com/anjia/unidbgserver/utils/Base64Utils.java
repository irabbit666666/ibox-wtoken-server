package com.anjia.unidbgserver.utils;

import java.util.Base64;

public class Base64Utils {
    public static byte[] decrypt(String encoded) throws Exception {
        return Base64.getDecoder().decode(encoded);
    }

    public static String encrypt(byte[] bytes) throws Exception {
        return Base64.getEncoder().encodeToString(bytes);
    }

}
