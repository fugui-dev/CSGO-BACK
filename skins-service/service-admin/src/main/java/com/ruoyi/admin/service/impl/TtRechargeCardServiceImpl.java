package com.ruoyi.admin.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.other.TtRechargeCard;
import com.ruoyi.domain.entity.TtRechargeProd;
import com.ruoyi.admin.mapper.TtRechargeCardMapper;
import com.ruoyi.admin.mapper.TtRechargeProdMapper;
import com.ruoyi.admin.service.TtRechargeCardService;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TtRechargeCardServiceImpl extends ServiceImpl<TtRechargeCardMapper, TtRechargeCard> implements TtRechargeCardService {

    private final TtRechargeProdMapper rechargeListMapper;

    public TtRechargeCardServiceImpl(TtRechargeProdMapper rechargeListMapper) {
        this.rechargeListMapper = rechargeListMapper;
    }

    @Override
    public List<TtRechargeCard> queryList(TtRechargeCard ttRechargeCard) {
        LambdaQueryWrapper<TtRechargeCard> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotNull(ttRechargeCard.getId())) wrapper.eq(TtRechargeCard::getId, ttRechargeCard.getId());
        if (StringUtils.isNotNull(ttRechargeCard.getRechargeListId())) wrapper.eq(TtRechargeCard::getRechargeListId, ttRechargeCard.getRechargeListId());
        if (StringUtils.isNotEmpty(ttRechargeCard.getPassword())) wrapper.eq(TtRechargeCard::getPassword, ttRechargeCard.getPassword());
        if (StringUtils.isNotEmpty(ttRechargeCard.getStatus())) wrapper.eq(TtRechargeCard::getStatus, ttRechargeCard.getStatus());
        if (StringUtils.isNotNull(ttRechargeCard.getUseUserId())) wrapper.eq(TtRechargeCard::getUseUserId, ttRechargeCard.getUseUserId());
        wrapper.orderByDesc(TtRechargeCard::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<String> generateCard(Integer rechargeListId, Integer num) {
        TtRechargeProd recharge = rechargeListMapper.selectById(rechargeListId);
        List<String> cardList = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            String password = IdUtils.fastSimpleUUID().toUpperCase();
            cardList.add(password);
            TtRechargeCard ttRechargeCard = TtRechargeCard.builder().build();
            ttRechargeCard.setRechargeListId(rechargeListId);
            ttRechargeCard.setPrice(recharge.getPrice());
            ttRechargeCard.setPassword(password);
            ttRechargeCard.setCreateBy(SecurityUtils.getUsername());
            ttRechargeCard.setCreateTime(DateUtils.getNowDate());
            this.save(ttRechargeCard);
        }
        return cardList;
    }

    @Override
    public void export(HttpServletResponse response, TtRechargeCard ttRechargeCard) {
        List<TtRechargeCard> ttRechargeCards = this.queryList(ttRechargeCard);
        List<String> cardList = ttRechargeCards.stream().map(TtRechargeCard::getPassword).collect(Collectors.toList());
        if (cardList.isEmpty()) return;
        String folderPath = RuoYiConfig.getDownloadPath() + "/";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        ServletOutputStream os = null;
        BufferedWriter bw = null;
        String uuid = IdUtils.fastSimpleUUID();
        try {
            bw = new BufferedWriter(new FileWriter(folderPath + uuid + ".txt"));
            for (String card : cardList) {
                bw.write(card);
                bw.newLine();
                bw.flush();
            }
            File uploadFile = new File(folderPath + uuid + ".txt");
            os = response.getOutputStream();
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/octet-stream");
            response.addHeader("Content-disposition", "attachment");
            os.write(FileUtil.readBytes(uploadFile));
            IoUtil.flush(os);
        } catch (IOException ignored) {
        } finally {
            if (os != null) IoUtil.close(os);
            if (bw != null) IoUtil.close(bw);
        }
    }
}
