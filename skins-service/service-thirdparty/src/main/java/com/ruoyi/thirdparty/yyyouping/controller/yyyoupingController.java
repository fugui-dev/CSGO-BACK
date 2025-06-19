package com.ruoyi.thirdparty.yyyouping.controller;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.ruoyi.admin.service.TtOrnamentYYService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.TtOrnamentsYY;
import com.ruoyi.thirdparty.yyyouping.service.yyyoupingService;
import com.ruoyi.thirdparty.yyyouping.utils.YYClient;
import com.ruoyi.thirdparty.yyyouping.utils.common.YYResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "YYYouPing")
@RestController
@RequestMapping("api/yyyouping")
public class yyyoupingController {

    @Autowired
    private yyyoupingService yyyoupingService;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Param {
        private Integer templateId;
        private String templateHashName;
    }

    @Autowired
    private TtOrnamentYYService ttOrnamentYYService;

    // 加载平台的饰品数据
    @ApiOperation("加载平台的饰品数据")
    @GetMapping("loadData")
    public R loadData(){
        // 读json
        File file = new File("C:\\Users\\Administrator\\Desktop\\response.json");
        JSONArray array = JSONUtil.readJSONArray(file, StandardCharsets.UTF_8);
        List<TtOrnamentsYY> list = JSONUtil.toList(array, TtOrnamentsYY.class);
        // 写db
        ttOrnamentYYService.saveBatch(list);
        return R.ok();
    }

    // 通过此接口可获取悠悠有品所有商品对应的模板ID的下载链接。
    @ApiOperation("查询模板ID下载链接")
    @GetMapping("yyApiTemplateQuery")
    public R yyApiTemplateQuery(){
        YYResult yyResult = yyyoupingService.yyApiTemplateQuery(new HashMap<>());
        if (!yyResult.getCode().equals(0)) return R.fail(yyResult);
        return R.ok(yyResult);
    }

    @ApiOperation("查询商品列表")
    @PostMapping("yyApiGoodsQuery")
    public R yyApiGoodsQuery(@RequestBody Map<String,Object> map){
        yyyoupingService.yyApiGoodsQuery(map);
        return R.ok();
    }

    @ApiOperation("余额查询")
    @PostMapping("yyApiGetAssetsInfo")
    public R yyApiGetAssetsInfo(){
        YYResult yyResult = yyyoupingService.yyApiGetAssetsInfo();
        if (!yyResult.getCode().equals(0)) return R.fail(yyResult);
        return R.ok(yyResult);
    }

    @ApiOperation("批量查询模板饰品的在售列表")
    @PostMapping("/yyApiBatchGetOnSaleCommodityInfo")
    public R balance(@RequestBody List<String> ids) {
        ArrayList<Object> list = new ArrayList<>();
        HashMap<String, Object> param = new HashMap<>();
        for (String id:ids){
            HashMap<String, String> map = new HashMap<>();
            map.put("templateId",id);
            list.add(map);
        }
        param.put("requestList",list);
        YYResult yyResult = yyyoupingService.yyApiBatchGetOnSaleCommodityInfo(param);
        return R.ok(yyResult);
    }

    @ApiOperation("按类别查询模板销售")
    @GetMapping("/yyApiQueryTemplateSaleByCategory")
    public R yyApiQueryTemplateSaleByCategory() throws Exception {
        HashMap<String, Object> map = new HashMap<>();

        // map.put("typeHashName","FAMAS | Commemoration (Factory New)");
        map.put("weaponHashName","FAMAS | Commemoration (Factory New)");
        map.put("page",1);
        map.put("size",10);

        // return yyClient.yyApiQueryTemplateSaleByCategory(map);

        return R.ok();
    }
}
