package com.lizhuhao.fundingmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lizhuhao.fundingmanagement.controller.dto.FileDTO;
import com.lizhuhao.fundingmanagement.entity.UploadFile;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-09
 */
public interface FileMapper extends BaseMapper<UploadFile> {

    List<FileDTO> findPage(Integer pageNum, Integer pageSize, String startDate, String endDate, String fileName, String projectName, String userName);

    Integer selectCount(String startDate, String endDate, String fileName, String projectName, String userName);
}
