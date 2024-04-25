package com.lizhuhao.fundingmanagement.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lizhuhao.fundingmanagement.controller.dto.EvidenceDTO;
import com.lizhuhao.fundingmanagement.controller.dto.ProjectDTO;
import com.lizhuhao.fundingmanagement.entity.Project;
import com.lizhuhao.fundingmanagement.entity.ProjectType;
import com.lizhuhao.fundingmanagement.entity.UploadFile;
import com.lizhuhao.fundingmanagement.service.IOcrService;
import com.lizhuhao.fundingmanagement.utils.TimeUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.lizhuhao.fundingmanagement.common.Constants;
import com.lizhuhao.fundingmanagement.common.Result;

import com.lizhuhao.fundingmanagement.service.IBudgetChangeService;
import com.lizhuhao.fundingmanagement.entity.BudgetChange;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-16
 */
@RestController
@RequestMapping("/budgetChange")
public class BudgetChangeController {

    @Autowired
    private IBudgetChangeService budgetChangeService;

    @Resource
    private IOcrService ocrService;

    //新增或更新
    @PostMapping
    public Result save(@RequestBody BudgetChange budgetChange){
        if(budgetChange.getId() != null){
            Date currentTime = new Date();
            budgetChange.setModifyTime(currentTime);
        }
        return Result.success(budgetChangeService.saveOrUpdate(budgetChange));
    }

    //查询所有数据
    @GetMapping
    public Result findAll() {
        return Result.success(budgetChangeService.list());
    }


    //分页查询
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam Integer projectId) {
        QueryWrapper<BudgetChange> queryWrapper = new QueryWrapper<>();
        if(projectId != null){
            queryWrapper.eq("project_id",projectId);
        }
        queryWrapper.orderByAsc("id");
        Page<BudgetChange> page = budgetChangeService.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<BudgetChange> records = page.getRecords();
        List<EvidenceDTO> list = new ArrayList<>();
        for (BudgetChange record : records) {
            EvidenceDTO evidenceDTO = new EvidenceDTO();
            BeanUtils.copyProperties(record,evidenceDTO);//借用工具类复制属性
            evidenceDTO.setCostAmount(new DecimalFormat("0.00").format(record.getCostAmount()));
            list.add(evidenceDTO);
        }
        Page<EvidenceDTO> returnPage = new Page<>();
        returnPage.setRecords(list);
        returnPage.setPages(page.getPages());
        returnPage.setCurrent(page.getCurrent());
        returnPage.setSize(page.getSize());
        returnPage.setTotal(page.getTotal());
        return Result.success(returnPage);
    }


    @Value("${evidences.upload.path}")
    private String fileUploadPath;
    /**
     * 文件上传接口
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public Result upload(@RequestBody MultipartFile file) throws IOException {
        String jsonString  = ocrService.actionOcr(file);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        String invoiceNum = jsonNode.get("words_result").get("InvoiceNum").asText();//发票号码
        String invoiceDate = jsonNode.get("words_result").get("InvoiceDate").asText();//开票日期"yyyy年mm月dd日"
        String amountInFiguers = jsonNode.get("words_result").get("AmountInFiguers").asText();//金额

        String originalFilename = file.getOriginalFilename(); //获取凭证名称
        String type = FileUtil.extName(originalFilename);   //获取文件类型
        long size = file.getSize();
        //存储到磁盘
        File uploadParentFile = new File(fileUploadPath);//创建文件目录evidences/
        if(!uploadParentFile.exists()){
            uploadParentFile.mkdirs();
        }
        //定义文件唯一标识码
        String uuid = IdUtil.fastSimpleUUID();
        String fileUUID = uuid + StrUtil.DOT + type;
        File uploadEvidence = new File(fileUploadPath + fileUUID);
        //把获取到的文件存储到磁盘目录
        file.transferTo(uploadEvidence);
        //获取文件的md5，并在数据库中查询是否也存在相同的md5
        String md5 = SecureUtil.md5(uploadEvidence);
        QueryWrapper<BudgetChange> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("del_flag",true);
        queryWrapper.eq("md5",md5);
        List<BudgetChange> fileList = budgetChangeService.list(queryWrapper);
        String url;
        //通过md5查询，如果存在相同文件就使用同一个url，并将存到磁盘的文件删除
        if(fileList.size() != 0){
            url = fileList.get(0).getEvidenceUrl();
            //删除多余文件
            uploadEvidence.delete();
        }else{
//            url = "http://localhost:9090/file/" + fileUUID;
            url = fileUUID;
        }
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        // 解析日期字符串
        LocalDate date = LocalDate.parse(invoiceDate, formatter);
        // 将日期转换为UTC时间
        ZonedDateTime utcDateTime = date.atStartOfDay(ZoneOffset.UTC);
        //返回前端的类
        EvidenceDTO evidenceDTO = new EvidenceDTO();
        evidenceDTO.setCostAmount(amountInFiguers);
        evidenceDTO.setEvidenceDate(Date.from(utcDateTime.toInstant()));
        evidenceDTO.setEvidenceNumber(invoiceNum);
        evidenceDTO.setEvidenceName(originalFilename);
        evidenceDTO.setEvidenceUrl(url);
        evidenceDTO.setMd5(md5);
        return Result.success(evidenceDTO);
    }

    /**
     * 文件下载接口   "http://localhost:9090/budgetChange/{fileUUID}"
     * @param fileUUID
     * @param response
     * @throws IOException
     */
    @GetMapping("/{fileUUID}")
    public void download(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        //根据文件唯一标识码获取文件
        File uploadFile = new File(fileUploadPath + fileUUID);
        //设置输出流格式
        ServletOutputStream os = response.getOutputStream();
        response.setContentType("application/octet-stream");
//        response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(fileUUID, "UTF-8"));
        QueryWrapper<BudgetChange> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("evidence_url",fileUUID);
        queryWrapper.ne("del_flag",true);
        BudgetChange one = budgetChangeService.getOne(queryWrapper);
        String newFileName = one.getEvidenceName();
        response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(newFileName, "UTF-8"));
        //读取文件字节流
        os.write(FileUtil.readBytes(uploadFile));
        os.flush();
        os.close();
    }

    //测试图片转换接口
    @GetMapping("/ocr")
    public String ocr(@RequestBody MultipartFile file) throws JsonProcessingException {
        String jsonString  = ocrService.actionOcr(file);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        String invoiceNum = jsonNode.get("words_result").get("InvoiceNum").asText();//发票号码
        String invoiceDate = jsonNode.get("words_result").get("InvoiceDate").asText();//开票日期"yyyy年mm月dd日"
        String amountInFiguers = jsonNode.get("words_result").get("AmountInFiguers").asText();//金额
        return invoiceNum+invoiceDate+amountInFiguers;
    }
}

