package com.lizhuhao.fundingmanagement.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lizhuhao.fundingmanagement.entity.UploadFile;
import com.lizhuhao.fundingmanagement.service.IFileService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 文件上传相关接口
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${files.upload.path}")
    private String fileUploadPath;

    @Autowired
    private IFileService fileService;

    /**
     * 文件上传接口
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename(); //获取文件名称
        String type = FileUtil.extName(originalFilename);   //获取文件类型
        long size = file.getSize();
        //存储到磁盘
        File uploadParentFile = new File(fileUploadPath);//创建文件目录files/
        if(!uploadParentFile.exists()){
            uploadParentFile.mkdirs();
        }
        //定义文件唯一标识码
        String uuid = IdUtil.fastSimpleUUID();
        String fileUUID = uuid + StrUtil.DOT + type;
        File uploadFile = new File(fileUploadPath + fileUUID);
        //把获取到的文件存储到磁盘目录
        file.transferTo(uploadFile);
        //获取文件的md5，并在数据库中查询是否也存在相同的md5
        String md5 = SecureUtil.md5(uploadFile);
        QueryWrapper<UploadFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5",md5);
        List<UploadFile> fileList = fileService.list(queryWrapper);
        String url;
        //通过md5查询，如果存在相同文件就使用同一个url，并将存到磁盘的文件删除
        if(fileList.size() != 0){
            url = fileList.get(0).getFileUrl();
            //删除多余文件
            uploadFile.delete();
        }else{
            url = "http://localhost:9090/file/" + fileUUID;
        }
        //记录到数据库
        UploadFile saveFile = new UploadFile();
        saveFile.setFileName(originalFilename);
        saveFile.setFileType(type);
        saveFile.setFileSize(size);
        saveFile.setFileUrl(url);
        saveFile.setMd5(md5);
        fileService.save(saveFile);
        return url;
    }

    /**
     * 文件下载接口   "http://localhost:9090/file/{fileUUID}"
     * @param fileUUID
     * @param response
     * @throws IOException
     */
    @GetMapping("/{fileUUID}")
    public void download(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        //根据文件唯一标识码获取文件
        File uploadFile = new File(fileUploadPath + fileUUID);
        //设置输出流格式
        ServletOutputStream os = response.getOutputStream();
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(fileUUID, "UTF-8"));
        //读取文件字节流
        os.write(FileUtil.readBytes(uploadFile));
        os.flush();
        os.close();
    }

}