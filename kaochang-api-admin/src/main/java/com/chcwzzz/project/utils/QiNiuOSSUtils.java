package com.chcwzzz.project.utils;

import com.chcwzzz.project.config.QiniuOSSProperties;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 七牛云OSS工具类
 */
@Component
public class QiNiuOSSUtils {
    @Resource
    private QiniuOSSProperties qiniuOSSProperties;


    //如果是Windows情况下，格式是 D:\\qiniu\\test.png
    public String upload(String filepath,
                         MultipartFile image) {
        String accessKey = qiniuOSSProperties.getAccessKey();
        String secretKey = qiniuOSSProperties.getSecretKey();
        String bucket = qiniuOSSProperties.getBucket();
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.xinjiapo());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本

        UploadManager uploadManager = new UploadManager(cfg);
        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = filepath;
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        DefaultPutRet putRet = null;
        try {
            Response response = uploadManager.put(image.getInputStream(), key, upToken, null, null);
            //解析上传成功的结果
            putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
        } catch (QiniuException ex) {
            ex.printStackTrace();
            if (ex.response != null) {
                System.err.println(ex.response);
                try {
                    String body = ex.response.toString();
                    System.err.println(body);
                } catch (Exception ignored) {
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "http://img.kaochang.me/" + putRet.key;
    }
}
