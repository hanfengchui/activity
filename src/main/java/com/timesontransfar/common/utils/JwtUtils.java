package com.timesontransfar.common.utils;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import static com.transfar.common.utils.JWTUtils.generalKey;

public class JwtUtils {
	
	private JwtUtils() {
		
	}

    private static final String AES_ALGORITHM = "AES";
    /**
     * 指定填充方式
     */
    private static final String CIPHER_PADDING = "AES/ECB/PKCS5Padding";

    private static final String AESKEY = "wxwshi1ai2ndzdi3";

    //有效期为
    public static final Long JWT_TTL = 86400000L;

    public static String createJWT(String id, String subject, Long ttlMillis) {

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        if(ttlMillis==null){
            ttlMillis = JWT_TTL;
        }
        long expMillis = nowMillis + ttlMillis;
        Date expDate = new Date(expMillis);
        SecretKey secretKey = generalKey();
        JwtBuilder builder = Jwts.builder()
                .setId(id)              //唯一的ID
                .setSubject(subject)   // 主题  可以是JSON数据
                .setIssuer("admin")     // 签发者
                .setIssuedAt(now)      // 签发时间
                .signWith(signatureAlgorithm, secretKey) //使用HS256对称加密算法签名, 第二个参数为秘钥
                .setExpiration(expDate);// 设置过期时间
        return builder.compact();
    }


    /**
     * AES加密
     *
     * @param content 待加密内容
     */
    public static String encrypt(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        //判断秘钥是否为16位
        //对密码进行编码
        try {
            byte[] bytes = AESKEY.getBytes(StandardCharsets.UTF_8);
            //设置加密算法，生成秘钥
            SecretKeySpec skeySpec = new SecretKeySpec(bytes, AES_ALGORITHM);
            // "算法/模式/补码方式"
            Cipher cipher = Cipher.getInstance(CIPHER_PADDING);
            //选择加密
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            //根据待加密内容生成字节数组
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            //返回base64字符串
            return Base64Utils.encodeToString(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException e) {
            return "";
        }
    }
}
