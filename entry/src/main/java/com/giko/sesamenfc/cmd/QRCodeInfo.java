package com.giko.sesamenfc.cmd;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

public class QRCodeInfo implements Serializable {

    public static final String PROTOCOL_HEADER = "ssm://UI";

    private String scanStr;
    private String dataType;
    private String permission;
    private String name;
    private String deviceType;
    private String secretKey;
    private String publicKey;
    private String keyIndex;
    private String uuid;

    private final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public QRCodeInfo(String scanStr) {
        this.scanStr = scanStr;
        this.init();
    }

    private void init() {
        String[] paramParts = getParamParts(this.scanStr);
        HashMap<String, String> paramMap = getParamMap(paramParts);
        this.dataType = paramMap.get("t");
        this.permission = paramMap.get("l");
        this.name = paramMap.get("n");
        String sk = paramMap.get("sk");
        byte[] skBytes = Base64.getDecoder().decode(sk);
        this.deviceType = bytesToHex(Arrays.copyOfRange(skBytes, 0,1));
        this.secretKey = bytesToHex(Arrays.copyOfRange(skBytes, 1,17));
        this.publicKey = bytesToHex(Arrays.copyOfRange(skBytes, 17,81));
        this.keyIndex = bytesToHex(Arrays.copyOfRange(skBytes, 81,83));
        this.uuid = bytesToHex(Arrays.copyOfRange(skBytes, 83,99));
    }

    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public String getDataType() {
        return dataType;
    }

    public String getPermission() {
        return permission;
    }

    public String getName() {
        return name;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getUuid() {
        String part1 = uuid.substring(0, 8);
        String part2 = uuid.substring(8, 12);
        String part3 = uuid.substring(12, 16);
        String part4 = uuid.substring(16, 20);
        String part5 = uuid.substring(20);
        return part1 + "-" + part2 + "-" + part3 + "-" + part4 + "-" + part5;
    }

    private HashMap<String, String> getParamMap(String[] paramParts) {
        HashMap<String, String> paramMap = new HashMap<>();
        for (String paramPart : paramParts) {
            String[] keyVal = paramPart.split("=");
            if (keyVal.length != 2) {
                throw new RuntimeException("QR code information is not correct!");
            }
            paramMap.put(keyVal[0], keyVal[1]);
        }
        return paramMap;
    }

    private String[] getParamParts(String uri) {
        String[] uriParts = uri.split("\\?");
        if (uriParts.length < 2) {
            throw new RuntimeException("QR code uri information is not correct!");
        }
        try {
            String paramUrl = java.net.URLDecoder.decode(uriParts[1], StandardCharsets.UTF_8.name());
            return paramUrl.split("&");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("QR code uri information is not correct!");
        }
    }
}
