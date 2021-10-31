package com.giko.sesamenfc.cmd;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.params.KeyParameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Logger;

public class SesameCmd implements Runnable, Callback {
    public final static String TOGGLE = "88";
    public final static String LOCK = "82";
    public final static String UNLOCK = "83";

    private final String deviceId;
    private final String apiKey;
    private final String secretKey;
    private final Logger logger;

    private String cmd;
    private int responseCode = 0;
    private Callback c;
    public SesameCmd(
            QRCodeInfo info,
            String apiKey,
            String cmd) {
        this.deviceId = info.getUuid();
        this.apiKey = apiKey;
        this.secretKey = info.getSecretKey();
        this.cmd = cmd;
        this.logger = Logger.getLogger("Sesame API");
    }

    @Override
    public void run() {
        int code = executeCmd(cmd);
        if (c != null) {
            this.c.callback(code);
        }
    }

    public void executeCmdAsynchronously(Callback c) {
        this.c = c;
        new Thread(this).start();
    }

    public int executeCmdSynchronously() {
        this.c = this;
        int timeout = 100000;
        new Thread(this).start();
        try {
            while (responseCode == 0 && timeout > 0) {
                Thread.sleep(1000);
                timeout = timeout - 1000;
            }
            return responseCode;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return responseCode;
        } finally {
            this.c = null;
            this.responseCode = 0;
        }
    }

    @Override
    public void callback(int code) {
        this.responseCode = code;
    }

    private int executeCmd(String cmdStr) {
        String base64History = Base64.getEncoder().encodeToString("NFC Unlock".getBytes());
        String sign = generateRandomTag();
        String json = String.format("{\"cmd\": \"%s\", \"history\": \"%s\",\"sign\": \"%s\"}", cmdStr, base64History, sign);
        try {
            URL url = new URL("https://app.candyhouse.co/api/sesame2/" + this.deviceId + "/cmd");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("x-api-key", apiKey);
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
            os.close();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
            br.close();
            int code = con.getResponseCode();
            con.disconnect();
            return code;
        } catch (IOException e) {
            e.printStackTrace();
            return 400;
        }
    }

    public String generateRandomTag(){
        // 1. timestamp  (SECONDS SINCE JAN 01 1970. (UTC))  // 1621854456905
        long timestamp = new Date().getTime() / 1000;
        // 2. timestamp to uint32  (little endian)   //f888ab60
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putLong(timestamp);
        // 3. remove most-significant byte    //0x88ab60
        byte[] message = Arrays.copyOfRange(buffer.array(), 1,4);
        return getCMAC(parseHexStr2Byte(secretKey), message).replace(" ","");
    }

    private byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length()/2];
        for (int i = 0; i < hexStr.length()/2; i++) {
            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
            result[i] = (byte)(high * 16 + low);
        }
        return result;
    }

    public String getCMAC(byte[] secretKey, byte[] msg) {
        CipherParameters params = new KeyParameter(secretKey);
        BlockCipher aes = new AESEngine();
        CMac mac = new CMac(aes);
        mac.init(params);
        mac.update(msg, 0, msg.length);
        byte[] out = new byte[mac.getMacSize()];
        mac.doFinal(out, 0);

        StringBuilder s19 = new StringBuilder();
        for (byte b : out) {
            s19.append(String.format("%02X ", b));
        }
        return s19.toString();
    }

}