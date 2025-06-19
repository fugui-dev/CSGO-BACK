package com.ruoyi.user.controller;


import com.ruoyi.admin.service.TtPromotionLevelService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.TtPromotionLevel;
import com.ruoyi.domain.vo.PromotionInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "用户推广界面相关接口（V2）")
@RestController
@RequestMapping("/api/promotinoLevel")
public class ApiPromotionController {

    @Autowired
    TtPromotionLevelService promotionLevelService;

    @ApiOperation("获取我的推广信息")
    @GetMapping("/getPromotionInfo")
    public R<PromotionInfoVO> getPromotionInfo(){
        return promotionLevelService.getPromotionInfo();
    }

    @ApiOperation("获取所有推广等级信息")
    @GetMapping("/allLevel")
    public R<List<TtPromotionLevel>> allLevel(){
        return R.ok(promotionLevelService.list());
    }

}
