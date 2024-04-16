package com.lizhuhao.fundingmanagement.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lizhuhao.fundingmanagement.common.Result;
import com.lizhuhao.fundingmanagement.controller.dto.ProjectDTO;
import com.lizhuhao.fundingmanagement.entity.Project;
import com.lizhuhao.fundingmanagement.entity.ProjectType;
import com.lizhuhao.fundingmanagement.entity.User;
import com.lizhuhao.fundingmanagement.service.IProjectService;
import com.lizhuhao.fundingmanagement.service.IProjectTypeService;
import com.lizhuhao.fundingmanagement.service.IUserService;
import com.lizhuhao.fundingmanagement.utils.TimeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-13
 */
@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private IProjectService projectService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IProjectTypeService projectTypeService;

    //新增或更新
    @PostMapping
    public Result save(@RequestBody Project project){
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
        return Result.success(projectService.saveOrUpdate(project));
    }

    //查询所有未逻辑删除的数据
    @GetMapping
    public Result findAll() {
        return Result.success(projectService.findAll());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(projectService.getById(id));
    }

    //分页查询
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "0") Integer pId,
                           @RequestParam(defaultValue = "") String projectName,
                           @RequestParam(defaultValue = "") String responsiblePerson,
                           @RequestParam(defaultValue = "") String startDate,
                           @RequestParam(defaultValue = "") String endDate,
                           @RequestParam(defaultValue = "") String startTime,
                           @RequestParam(defaultValue = "") String endTime) {
        QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("del_flag",true);
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
        Page<Project> page = projectService.page(new Page<>(pageNum, pageSize), queryWrapper);
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
        return Result.success(returnPage);
    }

    //逻辑删除
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id){
        if(projectService.delete(id)){
            return Result.success();
        }else{
            return Result.error();
        }
    }

    //批量逻辑删除
    @PostMapping("/del/batch")
    public Result delBatch(@RequestBody List<Integer> ids){
        if(projectService.delBatch(ids)){
            return Result.success();
        }else{
            return Result.error();
        }
    }
}

