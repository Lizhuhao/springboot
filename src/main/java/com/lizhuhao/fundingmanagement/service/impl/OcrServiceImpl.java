package com.lizhuhao.fundingmanagement.service.impl;

import com.baidu.aip.ocr.AipOcr;
import com.lizhuhao.fundingmanagement.service.IOcrService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;

@Service
public class OcrServiceImpl implements IOcrService {
    @Override
    public String actionOcr(MultipartFile multipartFile) {

        final Logger log= LoggerFactory.getLogger(OcrServiceImpl.class);
        /**
         * 百度 app_id
         */
        final String APP_ID = "61989590";

        /**
         * 百度 api_key
         */
        final String API_KEY = "t7CBQEiiLnjHitISCNXPAgf6";

        /**
         * 百度 SECRET_KEY
         */
        final String SECRET_KEY = "F4zgAGOcrFKJmpiQaOqoHtayTyYjJtfC";
        AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
        HashMap<String, String> options = new HashMap<String, String>(4);
        options.put("language_type", "CHN_ENG");
        options.put("detect_direction", "true");
        options.put("detect_language", "true");
        options.put("probability", "true");

        // 参数为二进制数组,将图片文件转为二进制数组
        byte[] buf = new byte[0];
        try {
            buf = multipartFile.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("获取文件字节数据异常，{}",e.getMessage());
        }
        JSONObject res = client.vatInvoice(buf, options);
        String jsonData = "";
        try {
            jsonData = res.toString(2);
        } catch (JSONException e) {
            log.error("获取json数据异常，{}",e.getMessage());
        }
        return jsonData;
    }
}
