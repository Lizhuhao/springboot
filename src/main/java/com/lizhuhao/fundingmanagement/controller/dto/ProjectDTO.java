package com.lizhuhao.fundingmanagement.controller.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ProjectDTO {
    private Integer id;
    private String projectName;
    private Integer projectNumber;
    private Integer pId;
    private String projectType;
    private Integer uId;
    private String totalBudget;
    private String balance;
    private String responsiblePerson;
    private Date startTime;
    private Date endTime;
    private Boolean delFlag;
    private Date createTime;
    private Date modifyTime;
}
