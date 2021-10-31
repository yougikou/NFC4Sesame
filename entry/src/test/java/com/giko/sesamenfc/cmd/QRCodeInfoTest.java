package com.giko.sesamenfc.cmd;

import org.junit.Test;

public class QRCodeInfoTest {

    private String testQRStr = "ssm://UI\\?t=sk&sk=AAAAAAAAAAAA3mEE9cDtT7yJUKLxQ0aAl5zRHRS3qkZ1MsiZY1KLqwhAtnut8i0VUP7amGmbDVYeXC%2FiN0CBOJYBO5i4AGKyoA1AXmCljoh5AACzB9EbRcYwVCtYb2GoxR21&l=2&n=%E5%A4%A7%E9%97%A8";

    @Test
    public void testInit() {
        QRCodeInfo info = new QRCodeInfo(testQRStr);
        System.out.println(info.getName());
        System.out.println(info.getPermission());
        System.out.println(info.getDeviceType());
        System.out.println(info.getSecretKey());
        System.out.println(info.getPublicKey());
        System.out.println(info.getUuid());
    }
}
