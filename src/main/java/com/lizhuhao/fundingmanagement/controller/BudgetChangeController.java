package com.lizhuhao.fundingmanagement.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lizhuhao.fundingmanagement.common.Result;
import com.lizhuhao.fundingmanagement.controller.dto.EvidenceDTO;
import com.lizhuhao.fundingmanagement.entity.BudgetChange;
import com.lizhuhao.fundingmanagement.service.IBudgetChangeService;
import com.lizhuhao.fundingmanagement.service.IOcrService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    //新增或更新，执行预算
    @PostMapping
    public Result save(@RequestBody BudgetChange budgetChange){
        return budgetChangeService.addAndUpdate(budgetChange);
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
        return Result.success(budgetChangeService.findPage(pageNum,pageSize,projectId));
    }

    //查询明细
    @GetMapping("/detail")
    public Result findDetail(@RequestParam Integer projectId) {
        return Result.success(budgetChangeService.findDetail(projectId));
    }

    /**
     * 凭证文件上传接口
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public Result upload(@RequestBody MultipartFile file) throws IOException {
        return Result.success(budgetChangeService.upload(file));
    }

    //预算执行取消时，删除凭证文件
    @GetMapping("/deleteEvidences")
    public void deleteEvidences(@RequestParam String fileUUID){
        budgetChangeService.deleteEvidences(fileUUID);
    }

    /**
     * 凭证文件下载接口   "http://localhost:9090/budgetChange/{fileUUID}"
     * @param fileUUID
     * @param response
     * @throws IOException
     */
    @GetMapping("/{fileUUID}")
    public void download(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        budgetChangeService.download(fileUUID,response);
    }

    //导出明细
    @PostMapping("/exportDetail")
    public void exportDetail(@RequestBody List<EvidenceDTO> list, HttpServletResponse response) throws Exception {
        budgetChangeService.exportDetail(list,response);
    }

    @Resource
    private IOcrService ocrService;

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

