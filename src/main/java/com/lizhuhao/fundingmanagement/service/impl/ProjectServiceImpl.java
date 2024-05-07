package com.lizhuhao.fundingmanagement.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lizhuhao.fundingmanagement.controller.dto.ProjectDTO;
import com.lizhuhao.fundingmanagement.entity.Project;
import com.lizhuhao.fundingmanagement.entity.ProjectType;
import com.lizhuhao.fundingmanagement.entity.User;
import com.lizhuhao.fundingmanagement.mapper.ProjectMapper;
import com.lizhuhao.fundingmanagement.service.IProjectService;
import com.lizhuhao.fundingmanagement.service.IProjectTypeService;
import com.lizhuhao.fundingmanagement.service.IUserService;
import com.lizhuhao.fundingmanagement.utils.TimeUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

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

    @Autowired
    private IUserService userService;

    @Autowired
    private IProjectTypeService projectTypeService;

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

    @Override
    public boolean addAndUpdate(Project project) {
        if(project.getId() != null){ //修改时填入修改时间
            Date currentTime = new Date();
            project.setModifyTime(currentTime);
        }else{
            Random random = new Random();
            int projectNumber = random.nextInt(900000) + 100000;//生成六位随机数作为项目号
            project.setProjectNumber(projectNumber);
            User user = userService.getById(project.getUId());
            project.setResponsiblePerson(user.getName());
        }
        return saveOrUpdate(project);
    }

    @Override
    public Page<ProjectDTO> findPage(Integer pageNum, Integer pageSize, Integer pId, String projectName, String responsiblePerson,
                                     String startDate, String endDate, String startTime, String endTime,Integer userId) {
        User user = userService.getById(userId);
        QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("del_flag",true);
        if(StrUtil.equals(user.getPermissions(),"1")){//如果是科研负责人，只能看见自己的项目信息
            queryWrapper.eq("u_id",userId);
        }
        if(!pId.equals(0)){
            queryWrapper.eq("p_id",pId);
        }
        if(StrUtil.isNotBlank(startDate) && StrUtil.isNotBlank(endDate)){
            queryWrapper.between("create_time", TimeUtils.timeProcess(startDate),TimeUtils.timeProcess(endDate));
        }
        if(StrUtil.isNotBlank(projectName)){
            queryWrapper.like("project_name",projectName);
        }
        if(StrUtil.isNotBlank(responsiblePerson)){
            queryWrapper.like("responsible_person",responsiblePerson);
        }
        if(StrUtil.isNotBlank(startTime)){
            queryWrapper.eq("start_time",TimeUtils.timeProcess(startTime));
        }
        if(StrUtil.isNotBlank(endTime)){
            queryWrapper.eq("end_Time",TimeUtils.timeProcess(endTime));
        }
        queryWrapper.orderByDesc("id");
        Page<Project> page = page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Project> records = page.getRecords();
        List<ProjectDTO> list = new ArrayList<>();
        for (Project record : records) {
            ProjectType projectType = projectTypeService.getById(record.getPId());
            ProjectDTO projectDTO = new ProjectDTO();
            BeanUtils.copyProperties(record,projectDTO);
            projectDTO.setProjectType(projectType.getTypeName());
            projectDTO.setTotalBudget(new DecimalFormat("0.00").format(record.getTotalBudget()));
            projectDTO.setBalance(new DecimalFormat("0.00").format(record.getBalance()));
            list.add(projectDTO);
        }
        Page<ProjectDTO> returnPage = new Page<>();
        returnPage.setRecords(list);
        returnPage.setPages(page.getPages());
        returnPage.setCurrent(page.getCurrent());
        returnPage.setSize(page.getSize());
        returnPage.setTotal(page.getTotal());
        return returnPage;
    }
}
