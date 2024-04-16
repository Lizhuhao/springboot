package com.lizhuhao.fundingmanagement.controller.dto;

import lombok.Data;

import java.util.Date;

@Data
public class FileDTO {
    private Integer id;
    private String fileName;
    private Integer userId;
    private Integer projectId;
    private String projectName;
    private String userName;
    private Date createTime;
    private Date modifyTime;
    private String fileUrl;
}
