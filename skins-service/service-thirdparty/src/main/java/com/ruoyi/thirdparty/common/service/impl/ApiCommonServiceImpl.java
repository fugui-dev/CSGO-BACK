package com.ruoyi.thirdparty.common.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.thirdparty.common.service.ApiCommonService;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApiCommonServiceImpl implements ApiCommonService {

    @Override
    public R<Object> writerPayImage(HttpServletResponse response, String contents) throws IOException {
        ServletOutputStream out = response.getOutputStream();
        try {
            Map<EncodeHintType,Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET,"UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            hints.put(EncodeHintType.MARGIN, 0);
            BitMatrix bitMatrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE,300,300,hints);
            MatrixToImageWriter.writeToStream(bitMatrix,"jpg",out);
            return R.ok("success");
        }catch (Exception e){
            return R.fail("数据异常");
        }finally {
            if(out != null){
                out.flush();
                out.close();
            }
        }
    }
}
