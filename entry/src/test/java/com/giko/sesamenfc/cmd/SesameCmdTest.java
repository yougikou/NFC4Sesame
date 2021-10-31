package com.giko.sesamenfc.cmd;

import org.junit.Test;

public class SesameCmdTest {

    private String testQRStr = "ssm://UI\\?t=sk&sk=ADJdzA6d0y14m%2FP8Xp3mWAWJUKLxQ0aAl5zRHRS3qkZ1MsiZY1KLqwhAtnut8i0VUP7amGmbDVYeXC%2FiN0CBOJYBO5i4AGKyoA1AXmCljoh5AACzB9EbRcYwVCtYb2GoxR21&l=0&n=%E5%A4%A7%E9%97%A8";
    // get from sesame console
    private String apiKey = "1T3rmL1ddL6m0VIVYKJoBayKqhLePmfZ44uNKt2N";

    @Test
    public void testGenerateRandomTag() {
        QRCodeInfo info = new QRCodeInfo(testQRStr);
        SesameCmd cmd = new SesameCmd(
                info,
                apiKey,
                SesameCmd.TOGGLE);
        System.out.println(cmd.generateRandomTag());
    }

    @Test
    public void testWm2Cmd() {
        QRCodeInfo info = new QRCodeInfo(testQRStr);
        SesameCmd cmd = new SesameCmd(
                info,
                apiKey,
                SesameCmd.TOGGLE);
        cmd.executeCmdSynchronously();
    }
}
