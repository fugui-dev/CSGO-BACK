package com.ruoyi.common.utils.file.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.ruoyi.common.core.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @Author: rayfoo@qq.com
 * @Date: 2020/7/13 3:11 下午
 * @Description:
 *
 *
 *     @PostMapping("/upload")
 *     public String upload(@RequestParam("file")MultipartFile file, HttpServletRequest request){
 *         //如果文件为空 返回错误信息
 *         if(file.isEmpty()){
 *             return "field";
 *         }
 *         //获取原文件名
 *         String originalFilename = file.getOriginalFilename();
 *
 *         //返回图片的url
 *        return AliyunOSSUtils.uploadFileInputSteam(originalFilename,file);
 *     }
 *
 */
@Slf4j
public class AliyunOSSUtils {

    /**
     * 阿里云的配置参数
     * https://cfcfcs.oss-cn-beijing.aliyuncs.com/public/logo.png
     */
    private static String accessKeyId = "LTAI5tDJfsQmPHECTCgaTjhH";
    private static String accessKeySecret = "BgCr5JackFlOJjYqISZl9P6BGFAJjt";
    private static String endpoint = "oss-cn-beijing.aliyuncs.com";
    private static String bucketName = "cfcfcs";
    /**
     * 存储在OSS中的前缀名
     */
    private static  String file_prefix = "oss";


    /**
     * 静态块
     */
//    static {
//        //初始化AccessKey
//        accessKeyId = PropertiesReader.get("aliyun.AccessKeyID");
//        //初始化AccessKeySecret
//        accessKeySecret = PropertiesReader.get("aliyun.AccessKeySecret");
//        //初始化Endpoint
//        endpoint = PropertiesReader.get("aliyun.EndPoint");
//        //初始化bucketName
//        bucketName = PropertiesReader.get("aliyun.Buckets");
//        //初始化前缀
//        file_prefix = PropertiesReader.get("aliyun.prefix");
//    }

    /**
     * 私有化构造
     */
    private AliyunOSSUtils() {

    }

    /**
     * 获取图片的URL头信息
     *
     * @return 返回url头信息
     */
    private static String getURLHead() {
        //从哪个位置截取
        int cutPoint = endpoint.lastIndexOf('/') + 1;
        //http头
        String head = "https://";
//        String head = endpoint.substring(0, cutPoint);
        //服务器地址信息
        String tail = endpoint.substring(cutPoint);
        //返回结果
        return head + bucketName + "." + tail + "/";
    }

    /**
     * 获取存储在服务器上的地址
     *
     * @param oranName 文件名
     * @return 文件URL
     */
    private static String getRealName(String oranName) {
        return getURLHead() + oranName;
    }

    /**
     * 获取一个随机的文件名
     *
     * @param oranName 初始的文件名
     * @return 返回加uuid后的文件名
     */
    private static String getRandomImageName(String oranName) {
        //获取一个uuid 去掉-
        String uuid = UUID.randomUUID().toString().replace("-", "");
        //查一下是否带路径
        int cutPoint = oranName.lastIndexOf("/") + 1;
        //如果存在路径
        if (cutPoint != 0) {
            //掐头 如果开头是/ 则去掉
            String head = oranName.indexOf("/") == 0 ? oranName.substring(1, cutPoint) : oranName.substring(0, cutPoint);
            //去尾
            String tail = oranName.substring(cutPoint);
            //返回正确的带路径的图片名称
            return file_prefix + head + uuid + tail;
        }
        //不存在 直接返回
        return file_prefix + uuid + oranName;
    }

    /**
     * MultipartFile2File
     * @param multipartFile
     * @return
     */
    private static File transferToFile(MultipartFile multipartFile) {
        //选择用缓冲区来实现这个转换即使用java 创建的临时文件 使用 MultipartFile.transferto()方法 。
        File file = null;
        try {
            //获取文件名
            String originalFilename = multipartFile.getOriginalFilename();
            //获取最后一个"."的位置
            int cutPoint = originalFilename.lastIndexOf(".");
            //获取文件名
            String prefix = originalFilename.substring(0,cutPoint);
            //获取后缀名
            String suffix = originalFilename.substring(cutPoint + 1);
            //创建临时文件
            file = File.createTempFile(prefix, suffix);
            //multipartFile2file
            multipartFile.transferTo(file);
            //删除临时文件
            file.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static AjaxResult UploadFileReturnAjax(String oranFileName, MultipartFile file, String ossPath) {

        // <yourObjectName>上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
        String newFileName = getRandomImageName(oranFileName);
        String objectName = ossPath + newFileName;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 上传文件流
        try (InputStream inputStream = new FileInputStream(transferToFile(file))) {
            //上传到OSS
            ossClient.putObject(bucketName, objectName, inputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("上传oss失败!", ex);
        }

        // 关闭OSSClient。
        ossClient.shutdown();

        //返回文件在服务器上的全路径+名称
        String url = getRealName(objectName);

        AjaxResult ajax = AjaxResult.success();
        ajax.put("url", url);
        ajax.put("fileName", objectName);
        ajax.put("newFileName", newFileName);
        ajax.put("originalFilename", file.getOriginalFilename());

        return ajax;
    }

        /**
         * 上传文件流
         *
         * @param oranFileName 上传到服务器上的文件路径和名称
         * @param file         来自本地的文件或者文件流
         */
    public static String uploadFileInputSteam(String oranFileName, MultipartFile file, String ossPath) {

        // <yourObjectName>上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
        String objectName = ossPath + getRandomImageName(oranFileName);

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 上传文件流
        try (InputStream inputStream = new FileInputStream(transferToFile(file))) {
            //上传到OSS
            ossClient.putObject(bucketName, objectName, inputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("上传oss失败!", ex);
        }

        // 关闭OSSClient。
        ossClient.shutdown();

        //返回文件在服务器上的全路径+名称
        return getRealName(objectName);
    }


    /**
     * 上传文件流
     *
     * @param oranFileName 上传到服务器上的文件路径和名称
     * @param file         来自本地的文件或者文件流
     */
    public static String uploadFileInputSteam(String oranFileName, File file) {

        // <yourObjectName>上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
        String objectName = getRandomImageName(oranFileName);

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 上传文件流。
        try (InputStream inputStream = new FileInputStream(file);) {
            //上传到OSS
            ossClient.putObject(bucketName, objectName, inputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 关闭OSSClient。
        ossClient.shutdown();

        //返回文件在服务器上的全路径+名称
        return getRealName(objectName);
    }

}
