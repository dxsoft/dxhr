package com.dxsoft.rsgzgl.security.ukey.enc;

/**
 * SoftKey 增强算法一（与厂商 SoftKey.StrEnc / 锁内 EncString 对齐）。
 * 移植自 2K 高级例子 SoftKey.java，仅保留登录验算所需方法。
 */
public final class SoftKeyStrEnc {

    private SoftKeyStrEnc() {
    }

    public static String strEnc(String inString, String key) {
        if (inString == null) {
            inString = "";
        }
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("增强算法密钥不能为空");
        }
        byte[] tempB = inString.getBytes();
        byte[] temp = new byte[8];
        byte[] outtemp = new byte[8];
        int nlen = tempB.length + 1;
        int outlen = Math.max(nlen, 8);
        byte[] b = new byte[outlen];
        byte[] outb = new byte[outlen];
        System.arraycopy(tempB, 0, b, 0, tempB.length);
        System.arraycopy(b, 0, outb, 0, outlen);
        for (int n = 0; n <= outlen - 8; n = n + 8) {
            System.arraycopy(b, n, temp, 0, 8);
            enCode(temp, outtemp, key);
            System.arraycopy(outtemp, 0, outb, n, 8);
        }
        StringBuilder outstring = new StringBuilder();
        for (int n = 0; n < outlen; n++) {
            outstring.append(myHex(outb[n]));
        }
        return outstring.toString();
    }

    public static boolean matches(String challenge, String key, String encData) {
        if (encData == null || encData.isBlank()) {
            return false;
        }
        String expected = strEnc(challenge, key);
        return constantTimeEquals(expected.trim(), encData.trim());
    }

    private static void enCode(byte[] inb, byte[] outb, String key) {
        long cnDelta = 2654435769L;
        long sum = 0;
        long mask = 4294967295L;
        long[] buf = new long[16];
        int nlen = key.length();
        int i = 0;
        for (int n = 1; n <= nlen; n = n + 2) {
            String tempString = key.substring(n - 1, n - 1 + 2);
            buf[i] = hexToInt(tempString);
            i = i + 1;
        }
        long a = 0;
        long b = 0;
        long c = 0;
        long d = 0;
        for (int n = 0; n <= 3; n++) {
            a = (buf[n] << (n * 8)) | a;
            b = (buf[n + 4] << (n * 8)) | b;
            c = (buf[n + 4 + 4] << (n * 8)) | c;
            d = (buf[n + 4 + 4 + 4] << (n * 8)) | d;
        }
        long y = 0;
        long z = 0;
        for (int n = 0; n <= 3; n++) {
            y = (conver(inb[n]) << (n * 8)) | y;
            z = (conver(inb[n + 4]) << (n * 8)) | z;
        }
        int n = 32;
        while (n > 0) {
            sum = (cnDelta + sum) & mask;
            long temp = (z << 4) & mask;
            temp = (temp + a) & mask;
            long temp1 = (z + sum) & mask;
            temp = (temp ^ temp1) & mask;
            temp1 = (z >> 5) & mask;
            temp1 = (temp1 + b) & mask;
            temp = (temp ^ temp1) & mask;
            temp = (temp + y) & mask;
            y = temp & mask;

            temp = (y << 4) & mask;
            temp = (temp + c) & mask;
            temp1 = (y + sum) & mask;
            temp = (temp ^ temp1) & mask;
            temp1 = (y >> 5) & mask;
            temp1 = (temp1 + d) & mask;
            temp = (temp ^ temp1) & mask;
            temp = (z + temp) & mask;
            z = temp & mask;
            n = n - 1;
        }
        for (int idx = 0; idx <= 3; idx++) {
            outb[idx] = (byte) ((y >>> (idx * 8)) & 255);
            outb[idx + 4] = (byte) ((z >>> (idx * 8)) & 255);
        }
    }

    private static String myHex(byte indata) {
        String outstring = String.format("%X", indata & 0xff);
        if (outstring.length() < 2) {
            outstring = "0" + outstring;
        }
        return outstring;
    }

    private static long conver(byte temp) {
        long tempInt = temp;
        if (tempInt < 0) {
            tempInt += 256;
        }
        return tempInt;
    }

    private static int hexToInt(String s) {
        String[] hexch = {
                "0", "1", "2", "3", "4", "5", "6", "7",
                "8", "9", "A", "B", "C", "D", "E", "F"};
        int k = 1;
        int r = 0;
        for (int i = s.length(); i > 0; i--) {
            String ch = s.substring(i - 1, i);
            int n = 0;
            for (int j = 0; j < 16; j++) {
                if (ch.compareToIgnoreCase(hexch[j]) == 0) {
                    n = j;
                }
            }
            r += (n * k);
            k *= 16;
        }
        return r;
    }

    private static boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        byte[] a = left.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] b = right.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
