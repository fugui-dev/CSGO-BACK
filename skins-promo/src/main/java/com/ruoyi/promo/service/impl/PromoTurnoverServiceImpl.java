package com.ruoyi.promo.service.impl;

import com.ruoyi.admin.mapper.TtUserMapper;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.entity.TtCommissionRecord;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.promo.domain.vo.AnchorDayTurnoverVO;
import com.ruoyi.promo.domain.vo.DayInviteVO;
import com.ruoyi.promo.domain.vo.DayTurnoverVO;
import com.ruoyi.promo.mapper.PromoTurnoverMapper;
import com.ruoyi.promo.service.PromoTurnoverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PromoTurnoverServiceImpl implements PromoTurnoverService {

    @Autowired
    private PromoTurnoverMapper promoTurnoverMapper;

    @Override
    public AjaxResult getRealTimeData(Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("anchorCount", promoTurnoverMapper.getAnchorCount(userId));
        map.put("totalTurnover", promoTurnoverMapper.getTotalTurnover(userId));
        map.put("lastMonthTurnover", promoTurnoverMapper.getLastMonthTurnover(userId));
        map.put("currentMonthTurnover", promoTurnoverMapper.getCurrentMonthTurnover(userId));
        map.put("lastWeekTurnover", promoTurnoverMapper.getLastWeekTurnover(userId));
        map.put("currentWeekTurnover", promoTurnoverMapper.getCurrentWeekTurnover(userId));
        map.put("lastDayTurnover", promoTurnoverMapper.getLastDayTurnover(userId));
        map.put("currentDayTurnover", promoTurnoverMapper.getCurrentDayTurnover(userId));
        return AjaxResult.success(map);
    }

    @Override
    public AjaxResult getLast10DaysPromotionData(Long userId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        List<DayTurnoverVO> last10DaysTurnoverVOList = promoTurnoverMapper.getLast10DaysTurnover(userId);
        List<DayInviteVO> last10DaysAnchorVOList = promoTurnoverMapper.getLast10DaysInvite(userId);

        List<Map<String, Object>> list = new ArrayList<>();
        for (DayTurnoverVO last10DaysTurnoverVO : last10DaysTurnoverVOList) {
            for (DayInviteVO last10DaysAnchorVO : last10DaysAnchorVOList) {
                if (last10DaysTurnoverVO.getDate().equals(last10DaysAnchorVO.getDate())) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", dateFormat.format(last10DaysTurnoverVO.getDate()));
                    map.put("last10DaysTurnover", last10DaysTurnoverVO.getAmount());
                    map.put("last10DaysAnchorInvite", last10DaysAnchorVO.getAnchorCount());
                    list.add(map);
                }
            }
        }
        return AjaxResult.success(list);
    }

    @Override
    public List<AnchorDayTurnoverVO> getAnchorDayTurnover(AnchorDayTurnoverVO anchorDayTurnoverVO) {
        return promoTurnoverMapper.getAnchorDayTurnover(anchorDayTurnoverVO);
    }

    @Override
    public List<TtUser> getSubBranches(Long userId) {
        List<TtUser> ttUserList = new ArrayList<>();
        // 获取根节点
        List<TtUser> children = promoTurnoverMapper.findByParentId(userId.intValue());
        for (TtUser child : children) {
            if ("02".equals(child.getUserType())) { // 02代表玩家
                ttUserList.add(child);
            } else if ("01".equals(child.getUserType())) { // 01代表主播
                buildUserTree(child); // 递归构建树
                ttUserList.add(child);
            }
        }
        return ttUserList;
    }

    @Override
    public List<TtUserBlendErcash> getPurchaseByUserId(Integer userId) {
        return promoTurnoverMapper.getPurchaseByUserId(userId);
    }

    @Override
    public BigDecimal getCommissionRateByUserId(Integer userId) {
        return promoTurnoverMapper.getCommissionRateByUserId(userId);
    }

    @Override
    public int updateCommissionRate(Integer userId, BigDecimal commissionRate) {
        return promoTurnoverMapper.updateCommissionRate(userId, commissionRate);
    }

    @Override
    public List<TtCommissionRecord> getCommissionList(Integer userId) {
        return promoTurnoverMapper.getCommissionList(userId);
    }

    /**
     * 递归下级分支
     */
    private void buildUserTree(TtUser ttUser) {
        // 获取当前用户的子节点
        List<TtUser> children = promoTurnoverMapper.findByParentId(ttUser.getUserId());
        // 设置当前用户的子节点
        ttUser.setChildren(children);
        for (TtUser child : children) {
            // 如果子节点是主播，继续递归
            if ("01".equals(child.getUserType())) {
                buildUserTree(child);
            }
        }
    }

    /**
     * 递归名下所有玩家
     */
    private void getAllPlayersByAnchorIdRecursive(int parentId, List<TtUser> players) {
        List<TtUser> users = promoTurnoverMapper.findByParentId(parentId);
        for (TtUser user : users) {
            if ("02".equals(user.getUserType())) { // 如果是玩家
                players.add(user);
            } else if ("01".equals(user.getUserType())) { // 如果是主播，继续递归
                getAllPlayersByAnchorIdRecursive(user.getUserId(), players);
            }
        }
    }
}
