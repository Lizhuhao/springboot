package com.lizhuhao.fundingmanagement.service.impl;

import com.lizhuhao.fundingmanagement.entity.FundingType;
import com.lizhuhao.fundingmanagement.entity.ProjectType;
import com.lizhuhao.fundingmanagement.mapper.ProjectTypeMapper;
import com.lizhuhao.fundingmanagement.service.IProjectTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-12
 */
@Service
public class ProjectTypeServiceImpl extends ServiceImpl<ProjectTypeMapper, ProjectType> implements IProjectTypeService {

    @Override
    public boolean delete(Integer id) {
        ProjectType projectType = getById(id);
        projectType.setDelFlag(true);
        return updateById(projectType);
    }

    @Override
    public boolean delBatch(List<Integer> ids) {
        List<ProjectType> list = new ArrayList<>();
        for (Integer id : ids) {
            ProjectType projectType = getById(id);
            projectType.setDelFlag(true);
            list.add(projectType);
        }
        return updateBatchById(list);
    }

    @Override
    public List<ProjectType> findAll() {
        return null;
    }
}
