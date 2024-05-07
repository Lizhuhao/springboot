package com.lizhuhao.fundingmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lizhuhao.fundingmanagement.controller.dto.ProjectDTO;
import com.lizhuhao.fundingmanagement.entity.Project;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-13
 */
public interface IProjectService extends IService<Project> {

    List<Project> findAll();

    boolean delete(Integer id);

    boolean delBatch(List<Integer> ids);

    boolean addAndUpdate(Project project);

    Page<ProjectDTO> findPage(Integer pageNum, Integer pageSize, Integer pId, String projectName, String responsiblePerson,
                              String startDate, String endDate, String startTime, String endTime, Integer userId);
}
