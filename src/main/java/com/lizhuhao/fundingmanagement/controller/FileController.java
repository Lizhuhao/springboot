package com.lizhuhao.fundingmanagement.controller;

import com.lizhuhao.fundingmanagement.common.Result;
import com.lizhuhao.fundingmanagement.service.IFileService;
import com.lizhuhao.fundingmanagement.service.IUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传相关接口
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private IFileService fileService;

    @Autowired
    private IUserService userService;

    //分页查询
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "'0'") Integer userId,
                           @RequestParam(defaultValue = "") String fileName,
                           @RequestParam(defaultValue = "") String projectName,
                           @RequestParam(defaultValue = "") String userName,
                           @RequestParam(defaultValue = "") String startDate,
                           @RequestParam(defaultValue = "") String endDate) {
        return Result.success(fileService.findPage(pageNum, pageSize, startDate, endDate, fileName, projectName, userName,userId));
    }

    /**
     * 文件上传接口
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam MultipartFile file,
                         @RequestParam Integer projectId,
                         @RequestParam Integer userId) throws IOException {
        if(fileService.upload(file,projectId,userId)){
            return Result.success();
        }else{
            return Result.error();
        }

    }

    /**
     * 文件下载接口   "http://localhost:9090/file/{fileUUID}"
     * @param fileUUID
     * @param response
     * @throws IOException
     */
    @GetMapping("/{fileUUID}")
    public void download(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        fileService.download(fileUUID,response);
    }

}