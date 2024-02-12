package com.pjieyi.yupao.controller;

import com.pjieyi.yupao.common.BaseResponse;
import com.pjieyi.yupao.common.ResultUtils;
import com.pjieyi.yupao.utils.AliyunOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * @Author pjieyi
 * @Description 文件上传
 */
@RestController
public class FileUploadController {

    @Autowired
    private AliyunOssUtil aliyunOssUtil;

    @PostMapping("/upload")
    public BaseResponse<String> upload(MultipartFile file) throws IOException {
        //获取原始文件名
        String filename=file.getOriginalFilename();
        filename= UUID.randomUUID()+filename.substring(filename.lastIndexOf("."));
        String url=aliyunOssUtil.upload(filename,file.getInputStream());
        //file.transferTo(new File("C:\\Users\\pjy17\\Desktop\\img\\"+filename));
        return ResultUtils.success(url);
    }
}
