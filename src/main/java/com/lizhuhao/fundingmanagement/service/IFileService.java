package com.lizhuhao.fundingmanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lizhuhao.fundingmanagement.controller.dto.FileDTO;
import com.lizhuhao.fundingmanagement.entity.UploadFile;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-09
 */
public interface IFileService extends IService<UploadFile> {

    List<FileDTO> findPage(Integer pageNum, Integer pageSize, String startDate, String endDate, String fileName,
                           String projectName, String userName,Integer userId);

    Integer selectCount(String startDate, String endDate, String fileName,
                        String projectName, String userName,Integer userId);
}
