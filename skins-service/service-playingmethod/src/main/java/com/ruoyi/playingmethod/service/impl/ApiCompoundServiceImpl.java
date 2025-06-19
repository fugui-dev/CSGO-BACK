package com.ruoyi.playingmethod.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.admin.mapper.TtOrnamentMapper;
import com.ruoyi.domain.common.constant.TtboxRecordStatus;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.other.TtOrnamentsLevel;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.admin.service.TtOrnamentsLevelService;
import com.ruoyi.admin.service.WebsitePropertyService;
import com.ruoyi.admin.util.RandomUtils;
import com.ruoyi.domain.vo.WebsitePropertyDataVO;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.playingmethod.mapper.ApiCompoundMapper;
import com.ruoyi.playingmethod.service.ApiCompoundService;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.user.service.ApiUserPackSackService;
import com.ruoyi.domain.vo.UserPackSackDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ruoyi.domain.common.constant.TtboxRecordSource.MALL_EXCHANGE;

@Service
@Slf4j
public class ApiCompoundServiceImpl implements ApiCompoundService {

    private final ISysConfigService configService;
    private final ApiUserPackSackService userPackSackService;
    private final WebsitePropertyService websitePropertyService;
    private final TtBoxRecordsService boxRecordsService;
    private final TtOrnamentMapper ornamentsMapper;
    private final TtOrnamentsLevelService ornamentsLevelService;
    private final ApiCompoundMapper apiCompoundMapper;

    public ApiCompoundServiceImpl(ISysConfigService configService,
                                  ApiUserPackSackService userPackSackService,
                                  WebsitePropertyService websitePropertyService,
                                  TtBoxRecordsService boxRecordsService,
                                  TtOrnamentMapper ornamentsMapper,
                                  TtOrnamentsLevelService ornamentsLevelService,
                                  ApiCompoundMapper apiCompoundMapper) {
        this.configService = configService;
        this.userPackSackService = userPackSackService;
        this.websitePropertyService = websitePropertyService;
        this.boxRecordsService = boxRecordsService;
        this.ornamentsMapper = ornamentsMapper;
        this.ornamentsLevelService = ornamentsLevelService;
        this.apiCompoundMapper = apiCompoundMapper;
    }

    @Override
    public R<UserPackSackDataVO> compound(List<Long> packSackIds, TtUser ttUser) {
        List<TtBoxRecords> boxRecordsList = userPackSackService.packSackHandle(packSackIds, ttUser, 3);
        if (ObjectUtils.isEmpty(boxRecordsList)) return R.fail("请选择饰品后再进行合成！");
        List<Long> ornamentsIds = boxRecordsList.stream().map(TtBoxRecords::getOrnamentId).collect(Collectors.toList());
        List<Long> websitePropertyIds = websitePropertyService.list().stream().map(WebsitePropertyDataVO::getId).collect(Collectors.toList());
        websitePropertyIds.retainAll(ornamentsIds);
        if (!websitePropertyIds.isEmpty()) return R.fail("选择的饰品中存在网站专属道具，请重新选择！");
        String maxCompoundNumStr = configService.selectConfigByKey("maxCompoundNum");
        int maxCompoundNum = Integer.parseInt(maxCompoundNumStr);
        if (boxRecordsList.size() > maxCompoundNum) return R.fail("最多选择" + maxCompoundNum + "个饰品！");
        BigDecimal compoundBeanTotal = boxRecordsList.stream().map(TtBoxRecords::getOrnamentsPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        String compoundMinPriceStr = configService.selectConfigByKey("compoundMinPrice");   // 获取配置中的汰换最低价
        BigDecimal compoundMinPrice = new BigDecimal(compoundMinPriceStr);
        if (compoundBeanTotal.compareTo(compoundMinPrice) < 0)
            return R.fail("选择的饰品总价必须大于" + compoundMinPrice + "游戏币才可以汰换哦！");
        Optional<BigDecimal> maxPriceOptional = boxRecordsList.stream().map(TtBoxRecords::getOrnamentsPrice).max(Comparator.naturalOrder());
        if (!maxPriceOptional.isPresent()) return null;
        try {
            String compoundMinPremiumRateStr = configService.selectConfigByKey("compoundMinPremiumRate");
            BigDecimal minPremiumRatePrice = new BigDecimal(compoundMinPremiumRateStr).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
                    .multiply(maxPriceOptional.get());
            String compoundMaxPremiumRateStr = configService.selectConfigByKey("compoundMaxPremiumRate");
            BigDecimal maxPremiumRatePrice = new BigDecimal(compoundMaxPremiumRateStr).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
                    .multiply(compoundBeanTotal);
            BigDecimal minPrice = maxPriceOptional.get().subtract(minPremiumRatePrice);
            BigDecimal maxPrice = compoundBeanTotal.subtract(maxPremiumRatePrice);
            List<TtOrnament> ornamentsList = new LambdaQueryChainWrapper<>(ornamentsMapper).in(TtOrnament::getType, 2, 3, 4, 5)
                    .between(TtOrnament::getUsePrice, minPrice, maxPrice).list();
            if (StringUtils.isNull(ornamentsList) || ornamentsList.isEmpty())
                return R.fail("汰换失败，饰品池中未筛选出符合您价格区间的饰品！");
            int randomIndex = RandomUtils.getRandomIndex(ornamentsList.size());
            TtOrnament ttOrnament = ornamentsList.get(randomIndex);
            List<Integer> levelIds = ornamentsLevelService.list().stream().map(TtOrnamentsLevel::getId).collect(Collectors.toList());
            Integer ornamentsLevelId = levelIds.get(RandomUtils.getRandomIndex(levelIds.size()));
            boxRecordsService.updateBatchById(boxRecordsList, 1);
            TtBoxRecords boxRecords = TtBoxRecords.builder().build();
            boxRecords.setUserId(ttUser.getUserId());
            boxRecords.setOrnamentId(ttOrnament.getId());
            boxRecords.setOrnamentsPrice(ttOrnament.getUsePrice());
            boxRecords.setOrnamentsLevelId(ornamentsLevelId);
            boxRecords.setStatus(TtboxRecordStatus.IN_PACKSACK_ON.getCode());
            boxRecords.setCreateTime(new Date());
            boxRecords.setSource(MALL_EXCHANGE.getCode()); //// TODO: 2024/4/1 类型不对
            boxRecords.setHolderUserId(ttUser.getUserId());
            boxRecordsService.save(boxRecords);
            UserPackSackDataVO compoundData = apiCompoundMapper.selectCompoundDataById(boxRecords.getId());
            return R.ok(compoundData);
        } catch (Exception e) {
            return R.fail("数据异常！");
        }
    }

    @Override
    public List<UserPackSackDataVO> getUserCompoundRecord(Integer userId) {
        return apiCompoundMapper.selectCompoundRecordByUserId(userId);
    }
}
