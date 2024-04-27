package com.lizhuhao.fundingmanagement.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lizhuhao.fundingmanagement.entity.ProjectType;
import com.lizhuhao.fundingmanagement.mapper.ProjectTypeMapper;
import com.lizhuhao.fundingmanagement.service.IProjectTypeService;
import com.lizhuhao.fundingmanagement.utils.TimeUtils;
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

    @Resource
    private ProjectTypeMapper projectTypeMapper;

    @Override
    public List<ProjectType> findAll() {
        return projectTypeMapper.findAll();
    }

    @Override
    public Page<ProjectType> findPage(Integer pageNum, Integer pageSize, String typeName, String startDate, String endDate) {
        QueryWrapper<ProjectType> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("del_flag",true);
        if(!typeName.equals("")){
            queryWrapper.like("type_name",typeName);
        }
        if(StrUtil.isNotBlank(startDate) && StrUtil.isNotBlank(endDate)){
            queryWrapper.between("create_time", TimeUtils.timeProcess(startDate),TimeUtils.timeProcess(endDate));
        }
        queryWrapper.orderByDesc("id");
        return page(new Page<>(pageNum, pageSize),queryWrapper);
    }
}
