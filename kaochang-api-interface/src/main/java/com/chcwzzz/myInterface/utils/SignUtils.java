package com.chcwzzz.myInterface.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.DigestUtil;

public class SignUtils {
    /**
     * 签名算法
     *
     * @param body
     * @param secretKey
     * @return
     */
    public static String getSign(String body, String secretKey) {
        String content = body.toString() + "--" + secretKey;
        String digested = DigestUtil
                .digester(DigestAlgorithm.SHA256)
                .digestHex(content);
        return digested;
    }
}
