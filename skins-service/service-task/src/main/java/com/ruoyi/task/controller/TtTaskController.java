package com.ruoyi.task.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.task.TtTask;
import com.ruoyi.domain.task.TtTaskDoing;
import com.ruoyi.domain.task.VO.TtTaskDoingVO;
import com.ruoyi.task.mapper.TtTaskMapper;
import com.ruoyi.task.service.TtTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "任务模块")
@RestController
@RequestMapping("/api/ttTask")
public class TtTaskController extends BaseController {

    @Autowired
    private TtTaskService ttTaskService;

    @ApiOperation("关于我的任务")
    @GetMapping("/taskOfme")
    @Anonymous
    // public List<TtTaskDoing> taskOfme(@PathVariable("page") Integer page,@PathVariable("size") Integer size) {
    public List<TtTaskDoingVO> taskOfme() {
        Long userId = getUserId();
        return ttTaskService.taskOfme(1,5,Integer.valueOf(String.valueOf(userId))); //暂时不需要分页
        // return ttTaskService.taskOfme(1,5,1);
    }

    // 领取奖励
    @GetMapping("/getAward/{tid}")
    @Anonymous
    public AjaxResult getAward(@PathVariable("tid") Integer taskDoingid) {
        Long userId = getUserId();
        return ttTaskService.getAward(Integer.valueOf(String.valueOf(userId)),taskDoingid);
        // return ttTaskService.getAward(1,1);
    }

    // 接首次下载任务
    @GetMapping("/firsDownLoadTask")
    @Anonymous
    public AjaxResult firsDownLoadTask() {
        Long userId = getUserId();
        return ttTaskService.firsDownLoadTask(Integer.valueOf(String.valueOf(userId)));
    }

}
