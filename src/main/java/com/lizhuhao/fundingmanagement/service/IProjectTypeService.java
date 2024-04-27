package com.lizhuhao.fundingmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lizhuhao.fundingmanagement.entity.ProjectType;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-12
 */
public interface IProjectTypeService extends IService<ProjectType> {

    boolean delete(Integer id);

    boolean delBatch(List<Integer> ids);

    List<ProjectType> findAll();

    Page<ProjectType> findPage(Integer pageNum, Integer pageSize, String typeName, String startDate, String endDate);
}
