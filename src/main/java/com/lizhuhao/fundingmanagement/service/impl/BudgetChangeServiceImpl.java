package com.lizhuhao.fundingmanagement.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.StyleSet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lizhuhao.fundingmanagement.common.Constants;
import com.lizhuhao.fundingmanagement.common.Result;
import com.lizhuhao.fundingmanagement.controller.dto.EvidenceDTO;
import com.lizhuhao.fundingmanagement.entity.*;
import com.lizhuhao.fundingmanagement.mapper.BudgetChangeMapper;
import com.lizhuhao.fundingmanagement.service.*;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-16
 */
@Service
public class BudgetChangeServiceImpl extends ServiceImpl<BudgetChangeMapper, BudgetChange> implements IBudgetChangeService {

    @Autowired
    private IProjectService projectService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IProjectTypeService projectTypeService;
    @Autowired
    private IFundingService fundingService;

    @Value("${evidences.upload.path}")
    private String fileUploadPath;

    @Resource
    private IOcrService ocrService;

    @Override
    public Result addAndUpdate(BudgetChange budgetChange) {
        QueryWrapper<Funding> queryFunding = new QueryWrapper<>();
        queryFunding.eq("project_id", budgetChange.getProjectId());
        queryFunding.eq("funding_type_id",budgetChange.getFundingTypeId());
        queryFunding.ne("del_flag", true);
        Funding funding = fundingService.getOne(queryFunding);
        BigDecimal amount = funding.getAmount();    //查询该预算类型总额
        QueryWrapper<BudgetChange> queryChange = new QueryWrapper<>();
        queryChange.eq("project_id", budgetChange.getProjectId());
        queryChange.eq("funding_type_id",budgetChange.getFundingTypeId());
        queryChange.ne("del_flag", true);
        List<BudgetChange> list = list(queryChange);   //查询该预算类型预算变化
        if(list.size() != 0){
            for (BudgetChange change : list) {
                amount = amount.subtract(change.getCostAmount());//该预算类型剩余额度
            }
        }
        if(amount.compareTo(budgetChange.getCostAmount()) < 0){//该预算类型剩余额度比传入数据小
            if(budgetChange.getEvidenceUrl() != null){
                deleteEvidences(budgetChange.getEvidenceUrl());//删除上传的文件
            }
            return Result.error(Constants.CODE_400,"报销金额大于该预算类型余额");
        }
        BigDecimal balance = new BigDecimal(0);//余额
        QueryWrapper<Project> queryProject= new QueryWrapper<>();
        queryProject.eq("id", budgetChange.getProjectId());
        queryProject.ne("del_flag", true);
        Project project = projectService.getOne(queryProject);
        if(budgetChange.getId() != null){
            Date currentTime = new Date();
            budgetChange.setModifyTime(currentTime);
        }
        boolean flag = false;
        if(saveOrUpdate(budgetChange)) {
            BigDecimal totalBudget = project.getTotalBudget();
            QueryWrapper<BudgetChange> queryBudgetChange = new QueryWrapper<>();
            queryBudgetChange.eq("project_id", budgetChange.getProjectId());
            queryBudgetChange.ne("del_flag", true);
            balance = balance.add(totalBudget);
            List<BudgetChange> budgetChangeList = list(queryBudgetChange);
            if(budgetChangeList.size() != 0){
                for (BudgetChange budget : budgetChangeList) {
                    balance = balance.subtract(budget.getCostAmount());
                }
            }
            project.setBalance(balance);
            //执行预算或修改执行预算时，给项目重新计算余额
            if(projectService.saveOrUpdate(project)) {
                flag = true;
            }
        }
        if(flag){
            return Result.success();
        }else {
            return Result.error(Constants.CODE_500,"保存失败");
        }
    }

    @Override
    public Page<EvidenceDTO> findPage(Integer pageNum, Integer pageSize, Integer projectId) {
        QueryWrapper<BudgetChange> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("del_flag",true);
        if(projectId != null){
            queryWrapper.eq("project_id",projectId);
        }
        queryWrapper.orderByAsc("id");
        Page<BudgetChange> page = page(new Page<>(pageNum, pageSize), queryWrapper);
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
        return returnPage;
    }

    @Override
    public EvidenceDTO upload(MultipartFile file) throws IOException {
        String jsonString  = ocrService.actionOcr(file);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        String invoiceNum = null;
        String invoiceDate = null;
        String amountInFiguers = null;
        if(jsonNode.get("words_result") != null){
            invoiceNum = jsonNode.get("words_result").get("InvoiceNum").asText();//发票号码
            invoiceDate = jsonNode.get("words_result").get("InvoiceDate").asText();//开票日期"yyyy年mm月dd日"
            amountInFiguers = jsonNode.get("words_result").get("AmountInFiguers").asText();//金额
        }
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
        List<BudgetChange> fileList = list(queryWrapper);
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
        ZonedDateTime utcDateTime = ZonedDateTime.now();
        if(invoiceDate != null){
            // 定义日期格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
            // 解析日期字符串
            LocalDate date = LocalDate.parse(invoiceDate, formatter);
            // 将日期转换为UTC时间
            utcDateTime = date.atStartOfDay(ZoneOffset.UTC);
        }
        //返回前端的类
        EvidenceDTO evidenceDTO = new EvidenceDTO();
        evidenceDTO.setCostAmount(amountInFiguers);
        if(invoiceDate != null){
            evidenceDTO.setEvidenceDate(Date.from(utcDateTime.toInstant()));
        }
        evidenceDTO.setEvidenceNumber(invoiceNum);
        evidenceDTO.setEvidenceName(originalFilename);
        evidenceDTO.setEvidenceUrl(url);
        evidenceDTO.setMd5(md5);
        return evidenceDTO;
    }

    @Override
    public void download(String fileUUID, HttpServletResponse response) throws IOException {
        //根据文件唯一标识码获取文件
        File uploadFile = new File(fileUploadPath + fileUUID);
        //设置输出流格式
        ServletOutputStream os = response.getOutputStream();
        response.setContentType("application/octet-stream");
//        response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(fileUUID, "UTF-8"));
        QueryWrapper<BudgetChange> queryWrapper = new QueryWrapper<>();//设置下载文件名
        queryWrapper.eq("evidence_url",fileUUID);
        queryWrapper.ne("del_flag",true);
        BudgetChange one = getOne(queryWrapper);
        String newFileName = one.getEvidenceName();
        response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(newFileName, "UTF-8"));
        //读取文件字节流
        os.write(FileUtil.readBytes(uploadFile));
        os.flush();
        os.close();
    }

    @Override
    public void deleteEvidences(String fileUUID) {
        QueryWrapper<BudgetChange> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("del_flag",true);
        queryWrapper.eq("evidence_url",fileUUID);
        List<BudgetChange> fileList = list(queryWrapper);
        if(fileList.size() != 0){
            File evidence = new File(fileUploadPath + fileUUID);
            evidence.delete();
        }
    }

    @Override
    public List<EvidenceDTO> findDetail(Integer projectId) {
        QueryWrapper<BudgetChange> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("del_flag",true);
        queryWrapper.eq("project_id",projectId);
        queryWrapper.orderByAsc("id");
        List<BudgetChange> records = list(queryWrapper);
        List<EvidenceDTO> list = new ArrayList<>();
        for (BudgetChange record : records) {
            EvidenceDTO evidenceDTO = new EvidenceDTO();
            BeanUtils.copyProperties(record,evidenceDTO);//借用工具类复制属性
            evidenceDTO.setCostAmount(new DecimalFormat("0.00").format(record.getCostAmount()));
            list.add(evidenceDTO);
        }
        return list;
    }

    @Override
    public void exportDetail(List<EvidenceDTO> list, HttpServletResponse response) throws IOException {
        Integer userId = null;
        Integer projectId = null;
        if(list.size() > 0){
            userId = list.get(0).getUserId();
            projectId = list.get(0).getProjectId();
        }
        User user = userService.getById(userId);
        Project project = projectService.getById(projectId);
        ProjectType projectType = projectTypeService.getById(project.getPId());

        //在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        //给日期单元格设置格式
        DataFormat dataFormat = writer.getWorkbook().createDataFormat();
        short format = dataFormat.getFormat("yyyy-mm-dd");
        StyleSet styleSet = writer.getStyleSet();
        styleSet.getCellStyleForDate().setDataFormat(format);

        // 设置标题一样式
        CellStyle cellStyle1 = writer.createCellStyle();
        cellStyle1.setVerticalAlignment(VerticalAlignment.CENTER);//垂直居中
        cellStyle1.setAlignment(HorizontalAlignment.CENTER);//水平居中
        //边框设置
        cellStyle1.setBorderLeft(writer.getCellStyle().getBorderLeft());
        cellStyle1.setBorderRight(writer.getCellStyle().getBorderRight());
        cellStyle1.setBorderBottom(writer.getCellStyle().getBorderBottom());
        cellStyle1.setWrapText(true);//自动换行
        Font font = writer.createFont();    //设置字体
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("微软雅黑");
        cellStyle1.setFont(font);

        CellStyle cellStyle2 = writer.createCellStyle();
        //边框设置
        cellStyle2.setBorderLeft(writer.getCellStyle().getBorderLeft());
        cellStyle2.setBorderRight(writer.getCellStyle().getBorderRight());
        cellStyle2.setBorderBottom(writer.getCellStyle().getBorderBottom());
        cellStyle2.setWrapText(true);//自动换行
        Font font2 = writer.createFont();    //设置字体
        font2.setFontHeightInPoints((short) 9);
        font2.setFontName("宋体");
        cellStyle2.setFont(font2);

        CellStyle cellStyle3 = writer.createCellStyle();
        cellStyle3.setVerticalAlignment(VerticalAlignment.CENTER);//垂直居中
        cellStyle3.setAlignment(HorizontalAlignment.CENTER);//水平居中
        cellStyle3.setBorderLeft(writer.getCellStyle().getBorderLeft());
        cellStyle3.setBorderRight(writer.getCellStyle().getBorderRight());
        cellStyle3.setBorderBottom(writer.getCellStyle().getBorderBottom());
        cellStyle3.setWrapText(true);//自动换行
        cellStyle3.setFont(font2);

        CellStyle cellStyle4 = writer.createCellStyle();
        cellStyle4.setWrapText(true);//自动换行
        cellStyle4.setFont(font2);

        //首行、信息行
        writer.merge(0,0,0,4, "项目预算明细表", cellStyle1);
        writer.setRowHeight(0, 40);
        String projectAndUser = project.getProjectNumber() + "/" +user.getName() + " " +project.getProjectName();
        String message = projectType.getTypeNumber() + "/" + projectType.getTypeName() + "\r\n" + projectAndUser;
        writer.merge(1,1,0,4, message, cellStyle2);
        writer.setRowHeight(1, 26);
        writer.passRows(2);//跳过标题行、信息行

        //定义标题别名
        writer.addHeaderAlias("evidenceDate","凭证日期");
        writer.addHeaderAlias("evidenceNumber","凭证编号");
        writer.addHeaderAlias("digest","摘要");
        writer.addHeaderAlias("costAmount","项目支出");
        writer.addHeaderAlias("balance","余额");
        //只输出设置了Alias(别名的字段)
        writer.setOnlyAlias(true);

        //list中的数据写入excel，默认样式，强制输出标题
        writer.write(list,true);

        //倒数第三行
        writer.merge(list.size()+3,list.size()+3,0,1, "负责人：" + user.getName(), cellStyle3);
        writer.writeCellValue(2,list.size()+3,projectAndUser + " 合计");
        writer.writeCellValue(3,list.size()+3,project.getTotalBudget().subtract(project.getBalance()));
        writer.writeCellValue(4,list.size()+3,project.getBalance());

        //倒数第二行
        writer.merge(list.size()+4,list.size()+4,0,1, "合计", cellStyle3);
        writer.writeCellValue(2,list.size()+4,"合计");
        writer.writeCellValue(3,list.size()+4,project.getTotalBudget().subtract(project.getBalance()));
        writer.writeCellValue(4,list.size()+4,project.getBalance());

        //最后一行
        LocalDate currentDate = LocalDate.now();
        String tabulation = currentDate.getYear() + "-" + currentDate.getMonthValue() + "-" + currentDate.getDayOfMonth() + " 制表";
        writer.merge(list.size()+5,list.size()+5,0,4, tabulation, cellStyle4);

        //设置宽度自适应
//        writer.autoSizeColumnAll();
        Sheet sheet = writer.getSheet();
        for (int i = 0; i < 5; i++) {
            // 调整每一列宽度
            sheet.autoSizeColumn((short) i);
            // 解决自动设置列宽中文失效的问题
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) * 20 / 10);
        }

        //设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("预算明细", "UTF-8");
        response.setHeader("Content-Disposition","attachment;filename="+fileName+".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out,true);
        out.close();
        writer.close();
    }

}
