package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    /**
     * 文件上传
     * @param file
     * @return
     */
    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file)  {
        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        assert originalFilename != null;
        String filename = uuid + originalFilename.substring(originalFilename.lastIndexOf("."));
        //文件保存在本机磁盘
        try {
            file.transferTo(new File("D:\\SkyFast\\img\\" + filename));
            //文件的url访问路径
            String path = "http://localhost:8080/img/" + filename;   //注意：这里的path必须加上协议http://
            return Result.success(path);
        } catch (IOException e) {
            log.error("文件上传失败");
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
