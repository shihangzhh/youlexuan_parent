package com.offcn.shop.controller;

import com.offcn.entity.Result;
import com.offcn.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
@RestController
public class UploadController {
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;


    @RequestMapping("/upload")
    public Result upload(MultipartFile file){
      //1、获取上传文件原始名称
        String filename = file.getOriginalFilename();
        //2、获取上传文件的扩展名
        String extName = filename.substring(filename.lastIndexOf(".") + 1);

        //3、创建Fastdfs客户端对象
        try {
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            //执行上传文件到FastDfs
         String fileName = fastDFSClient.uploadFile(file.getBytes(),extName);
         //拼接服务器返回的文件名称和 请求访问id  http://192.168.188.146/group1/M00/00/00/wKi8kl2pYTOAY_TyAAI1bFuIUJE564.jpg
           fileName= FILE_SERVER_URL+fileName;
           return new Result(true,fileName);
        } catch (Exception e) {
            e.printStackTrace();

            return new Result(false,"上传失败");
        }
    }
}
