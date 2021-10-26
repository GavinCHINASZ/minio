package com.minio.controller;

import com.minio.util.MinioUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


@Slf4j
@RestController
public class MinioController {
    private final static Logger logger = LoggerFactory.getLogger(MinioController.class);

    @Autowired
    private MinioUtil minioUtil;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    /**
     * 上传文件
     *
     * @author 黄贵川
     * @param file 上传的文件
     * @return url 上传成功的文件URL
     */
    @PostMapping("/upload")
    public String MinIOUpload(MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (file.isEmpty() || file.getSize() == 0) {
                return "文件为空";
            }
            String contentType = request.getContentType();

            // 检查存储桶是否存在
            String bucketName = "wly" + sdf.format(new Date());
            if (!minioUtil.bucketExists(bucketName)) {
                // 创建存储桶
                minioUtil.makeBucket(bucketName);
            }

            InputStream inputStream = file.getInputStream();
            String fileName = file.getOriginalFilename();
            String objectName = "20211008" + "/"
                    + UUID.randomUUID().toString().replaceAll("-", "")
                    + fileName.substring(fileName.lastIndexOf("."));
            minioUtil.putObject(bucketName, objectName, inputStream);
            inputStream.close();

            return minioUtil.getObjectUrl(bucketName, objectName);
        } catch (Exception e) {
            logger.debug("上传文件失败", e);
            return "上传文件失败";
        }
    }

    /**
     * 下载文件
     *
     * @param fileName 文件绝对路径
     * @param response HttpServletResponse
     */
    @GetMapping("downloadFile")
    public void downloadFile(String fileName, HttpServletResponse response) {
        try {
            // 拿到文件路径
            String[] split = fileName.split("/");
            String bucketName = split[split.length - 3];
            String bucketNames = split[split.length - 2];
            String objectName = split[split.length - 1];

            // 获取文件对象
            InputStream object = minioUtil.getObject(bucketName, bucketNames + "/" + objectName);
            if (object != null) {
                byte buf[] = new byte[1024];
                int length = 0;
                response.reset();
                response.setHeader("Content-Disposition", "attachment;filename="
                        + URLEncoder.encode(objectName, "UTF-8"));

                String suffixName = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
                boolean imageBollean = ".png".equals(suffixName);
                if (!imageBollean) {
                    response.setContentType("application/octet-stream");
                    response.setCharacterEncoding("UTF-8");
                }

                OutputStream outputStream = response.getOutputStream();
                // 输出文件
                while ((length = object.read(buf)) > 0) {
                    outputStream.write(buf, 0, length);
                }
                // 关闭输出流
                outputStream.close();
            }
        } catch (Exception ex) {
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            String data = "文件下载失败";
            OutputStream ps = null;
            try {
                ps = response.getOutputStream();
                ps.write(data.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件绝对路径
     * @return String
     */
    @RequestMapping("removeObject")
    public String removeObject(String fileUrl) {
        try {
            // 拿到文件路径
            String[] split = fileUrl.split("/wly");
            String bucketName = "wly" + split[1].substring(0, 8);
            String url = "wly" + split[1];

            // 获取文件对象
            String objectName = url.substring(url.indexOf("/") + 1);
            boolean removeObjectBoolean = minioUtil.removeObject(bucketName, objectName);
            return removeObjectBoolean ? "删除文件成功" : "删除文件失败";
        } catch (Exception e) {
            logger.debug("删除文件异常", e);
            return "删除文件失败!";
        }
    }

    @RequestMapping("updateCustomer")
    public String updateCustomer(String fileUrl) {

        for (int i = 0; i < 1; i++) {
            try {
                //String urlSting = "https://nptest.gnwly.cn/file/headportrait/1632989074148qq.png";
                //String urlSting = "http://127.0.0.1:9000/wly20211009/20211008/df90e3e0169540ff9a7aa7fccb90c72d.jpg";
                String urlSting = "http://nongpi.oss-cn-hangzhou.aliyuncs.com/guizhou/appupload/57517987511624032934202.jpg";
                URL url = new URL(urlSting);
                // 后缀名   .png
                String suffix = urlSting.substring(urlSting.lastIndexOf("."));
                // 打开链接
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // 设置请求方式为"GET"
                conn.setRequestMethod("GET");
                // 超时响应时间为5秒
                conn.setConnectTimeout(5 * 1000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = conn.getInputStream();
                    // 检查存储桶是否存在
                    String bucketName = "0wly" + sdf.format(new Date());
                    if (!minioUtil.bucketExists(bucketName)) {
                        // 创建存储桶
                        minioUtil.makeBucket(bucketName);
                    }
                    //得到图片的二进制数据，以二进制封装得到数据，具有通用性
                    byte[] data = readinputstream(inputStream);

                    //new一个文件对象用来保存图片，默认保存当前工程根目录
                    File imagefile = new File("pic20170419" + suffix);
                    //创建输出流
                    FileOutputStream outstream = new FileOutputStream(imagefile);
                    //写入数据
                    outstream.write(data);
                    //关闭输出流
                    outstream.close();

                    FileInputStream input = new FileInputStream("pic20170419" + suffix);

                    long l = System.currentTimeMillis();
                    System.out.println(l);
                    String objectName = sdf.format(new Date()) + "/"
                            + l + suffix;
                    minioUtil.putObject(bucketName, objectName, input);
                    inputStream.close();

                    //return minioUtil.getObjectUrl(bucketName, objectName);
                }
            } catch (Exception e) {
                System.out.println("updateCustomer异常" + e);
            }
        }

        System.out.println("updateCustomer成功");
        return "updateCustomer成功";
    }

    public static byte[] readinputstream(InputStream instream) throws Exception {
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();

        //创建一个buffer字符串
        byte[] buffer = new byte[1024];
        //每次读取的字符串长度，如果为-1，代表全部读取完毕
        int len = 0;
        //使用一个输入流从buffer里把数据读取出来
        while ((len = instream.read(buffer)) != -1) {
            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
            outstream.write(buffer, 0, len);
        }
        //关闭输入流
        instream.close();
        //把outstream里的数据写入内存
        return outstream.toByteArray();
    }

}
