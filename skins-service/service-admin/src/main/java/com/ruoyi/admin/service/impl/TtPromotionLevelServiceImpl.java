package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.mapper.*;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.domain.entity.TtPromotionLevel;
import com.ruoyi.admin.service.TtPromotionLevelService;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.vo.PromotionInfoVO;
import com.ruoyi.domain.vo.TeamDetailVO;
import com.ruoyi.domain.vo.TtUserAmountRecords.PersonBlendErcashVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class TtPromotionLevelServiceImpl extends ServiceImpl<TtPromotionLevelMapper, TtPromotionLevel> implements TtPromotionLevelService {

    private final WebsiteSetupMapper websiteSetupMapper;

    @Autowired
    private TtUserService userService;

    @Autowired
    private TtUserMapper userMapper;

    @Autowired
    private TtPromotionUpdateMapper ttPromotionUpdateMapper;

    @Autowired
    private TtUserBlendErcashMapper ttUserBlendErcashMapper;


    public TtPromotionLevelServiceImpl(WebsiteSetupMapper websiteSetupMapper) {
        this.websiteSetupMapper = websiteSetupMapper;
    }

    @Override
    public String generateVipLevel(Integer num) {
        for (int i = 1; i <= num; i++) {
            TtPromotionLevel ttPromotionLevel = TtPromotionLevel.builder().build();
            ttPromotionLevel.setName("LV" + i);
            ttPromotionLevel.setIcon("");
            ttPromotionLevel.setRechargeThreshold(BigDecimal.ZERO);
            ttPromotionLevel.setCommissions(BigDecimal.ZERO);
            ttPromotionLevel.setAddedBonus(BigDecimal.ZERO);
            ttPromotionLevel.setCreateTime(DateUtils.getNowDate());
            this.save(ttPromotionLevel);
        }
        return "";
    }

    @Override
    public String updatePromotionLevelById(TtPromotionLevel ttPromotionLevel) {
        String icon = ttPromotionLevel.getIcon();
        ttPromotionLevel.setIcon(RuoYiConfig.getDomainName() + icon);
        this.updateById(ttPromotionLevel);
        return "";
    }

    @Override
    public void truncatePromotionLevel() {
        websiteSetupMapper.truncateTtPromotionLevel();
    }

    @Override
    public R<PromotionInfoVO> getPromotionInfo() {
        //查询个人信息
        Long userId = SecurityUtils.getUserId();
        TtUser user = userService.getById(userId);
        Integer levelId = user.getPromotionLevel();
        BigDecimal commissions = BigDecimal.valueOf(0);

        //查询对应推广等级
        TtPromotionLevel promotionLevel = this.getById(levelId);
        if (promotionLevel != null && promotionLevel.getCommissions() != null) commissions = promotionLevel.getCommissions();

        //查询所有下级id
        List<Integer> allEmployeesId = userMapper.allEmployeesByParents(Arrays.asList(userId.intValue()));

        //封装数据
        PromotionInfoVO infoVO = new PromotionInfoVO();
        infoVO.setLevel(levelId);
        infoVO.setCommissions(commissions);
        infoVO.setTeamSize(allEmployeesId.size());
        infoVO.setAfterDayPre(BigDecimal.valueOf(0));

        //存在下级id时再封装预计收入
        if (!allEmployeesId.isEmpty()){
            //明日预计收入（查询当天所有下级的消费）

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startOfDay = getStartDayTime();
            String beginTimeStr = dateFormat.format(startOfDay); //当天0点
            String endTimeStr = dateFormat.format(new Date()); // 当前时间

            List<PersonBlendErcashVO> employessBlendErcashList = ttUserBlendErcashMapper.personsTotalConsumeByTime(
                    allEmployeesId,
                    beginTimeStr,
                    endTimeStr,
                    0
            );

            BigDecimal temp = BigDecimal.ZERO;
            for (PersonBlendErcashVO personBlendErcashVO : employessBlendErcashList) {
                if (personBlendErcashVO.getAmount() != null && personBlendErcashVO.getAmount().compareTo(BigDecimal.ZERO) >= 0){
                    temp.add(personBlendErcashVO.getAmount());
                }
            }
            infoVO.setAfterDayPre(temp);
        }


        return R.ok(infoVO);
    }

    private static Date getStartDayTime() {
        // 使用 Calendar 获取今天凌晨 0 点
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();
        return startOfDay;
    }
}
