package com.lizhuhao.fundingmanagement.controller.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class EvidenceDTO {
    private Integer id;
    private Integer fundingTypeId;
    private String costAmount;
    private String balance;//余额，需要计算后返回
    private String digest;
    private Date evidenceDate;
    private String evidenceName;
    private String evidenceNumber;
    private String evidenceUrl;
    private String md5;
    private Date createTime;
}
