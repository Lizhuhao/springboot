package com.lizhuhao.fundingmanagement.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lizhuhao.fundingmanagement.controller.dto.FileDTO;
import com.lizhuhao.fundingmanagement.entity.UploadFile;
import com.lizhuhao.fundingmanagement.entity.User;
import com.lizhuhao.fundingmanagement.mapper.FileMapper;
import com.lizhuhao.fundingmanagement.service.IFileService;
import com.lizhuhao.fundingmanagement.service.IUserService;
import com.lizhuhao.fundingmanagement.utils.TimeUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-09
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, UploadFile> implements IFileService {

    @Resource
    private FileMapper fileMapper;

    @Autowired
    private IUserService userService;

    @Override
    public Page<FileDTO> findPage(Integer pageNum, Integer pageSize, String startDate, String endDate,
                                  String fileName, String projectName, String userName,Integer userId) {
        pageNum = (pageNum -1) * pageSize;
        User user = userService.getById(userId);
        if(user.getPermissions().equals("0")){//管理员不需要筛选userId条件,直接查出
            userId = null;
        }
        List<FileDTO> list = fileMapper.findPage(pageNum,pageSize,TimeUtils.timeProcess(startDate),
                TimeUtils.timeProcess(endDate),fileName, projectName,userName,userId);
        Page<FileDTO> fileDTOPage = new Page<>();
        fileDTOPage.setRecords(list);
        fileDTOPage.setCurrent(pageNum);
        fileDTOPage.setSize(pageSize);
        Integer total = selectCount(startDate, endDate, fileName, projectName, userName,userId);
        fileDTOPage.setTotal(total);
        return fileDTOPage;
    }

    @Override
    public Integer selectCount(String startDate, String endDate, String fileName,
                               String projectName, String userName,Integer userId) {
        return fileMapper.selectCount(startDate, endDate, fileName, projectName, userName,userId);
    }

    @Value("${files.upload.path}")
    private String fileUploadPath;

    @Override
    public Boolean upload(MultipartFile file, Integer projectId, Integer userId) throws IOException {
        QueryWrapper<UploadFile> queryWrapperOne = new QueryWrapper<>();
        queryWrapperOne.ne("del_flag",true);
        queryWrapperOne.eq("project_id",projectId);
        queryWrapperOne.eq("user_id",userId);
        UploadFile one = getOne(queryWrapperOne);
        if(one != null){//重新提交时，先将磁盘中已存在的文件删除，数据库记录也删除
            File fileDel = new File(fileUploadPath + one.getFileUrl());
            fileDel.delete();
            one.setDelFlag(true);
            saveOrUpdate(one);
        }
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
        queryWrapper.ne("del_flag",true);
        queryWrapper.eq("md5",md5);
        List<UploadFile> fileList = list(queryWrapper);
        String url;
        //通过md5查询，如果存在相同文件就使用同一个url，并将存到磁盘的文件删除
        if(fileList.size() != 0){
            url = fileList.get(0).getFileUrl();
            //删除多余文件
            uploadFile.delete();
        }else{
//            url = "http://localhost:9090/file/" + fileUUID;
            url = fileUUID;
        }
        //记录到数据库
        UploadFile saveFile = new UploadFile();
        saveFile.setFileName(originalFilename);
        saveFile.setFileType(type);
        saveFile.setFileSize(size);
        saveFile.setFileUrl(url);
        saveFile.setMd5(md5);
        saveFile.setProjectId(projectId);
        saveFile.setUserId(userId);
        return save(saveFile);
    }

    @Override
    public void download(String fileUUID, HttpServletResponse response) throws IOException {
        //根据文件唯一标识码获取文件
        File uploadFile = new File(fileUploadPath + fileUUID);
        //设置输出流格式
        ServletOutputStream os = response.getOutputStream();
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(fileUUID, "UTF-8"));
//        String newFileName = "newFileName.doc";
//        response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(newFileName, "UTF-8"));
        //读取文件字节流
        os.write(FileUtil.readBytes(uploadFile));
        os.flush();
        os.close();
    }
}
