package com.lizhuhao.fundingmanagement.service;

import com.lizhuhao.fundingmanagement.entity.Project;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
