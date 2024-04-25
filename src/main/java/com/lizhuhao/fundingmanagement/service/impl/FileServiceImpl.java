package com.lizhuhao.fundingmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lizhuhao.fundingmanagement.controller.dto.FileDTO;
import com.lizhuhao.fundingmanagement.entity.UploadFile;
import com.lizhuhao.fundingmanagement.mapper.FileMapper;
import com.lizhuhao.fundingmanagement.service.IFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lizhuhao.fundingmanagement.utils.TimeUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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

    @Override
    public List<FileDTO> findPage(Integer pageNum, Integer pageSize, String startDate, String endDate,
                                  String fileName, String projectName, String userName,Integer userId) {
        pageNum = (pageNum -1) * pageSize;
        return fileMapper.findPage(pageNum,pageSize,TimeUtils.timeProcess(startDate),
                TimeUtils.timeProcess(endDate),fileName, projectName,userName,userId);
    }

    @Override
    public Integer selectCount(String startDate, String endDate, String fileName,
                               String projectName, String userName,Integer userId) {
        return fileMapper.selectCount(startDate, endDate, fileName, projectName, userName,userId);
    }


}
