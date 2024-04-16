package com.lizhuhao.fundingmanagement.service.impl;

import com.lizhuhao.fundingmanagement.entity.Project;
import com.lizhuhao.fundingmanagement.entity.ProjectType;
import com.lizhuhao.fundingmanagement.mapper.ProjectMapper;
import com.lizhuhao.fundingmanagement.mapper.ProjectTypeMapper;
import com.lizhuhao.fundingmanagement.service.IProjectService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-13
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements IProjectService {

    @Resource
    private ProjectMapper projectMapper;

    @Override
    public List<Project> findAll() {
        return projectMapper.findAll();
    }

    @Override
    public boolean delete(Integer id) {
        Project project = getById(id);
        project.setDelFlag(true);
        return updateById(project);
    }

    @Override
    public boolean delBatch(List<Integer> ids) {
        List<Project> list = new ArrayList<>();
        for (Integer id : ids) {
            Project project = getById(id);
            project.setDelFlag(true);
            list.add(project);
        }
        return updateBatchById(list);
    }
}
