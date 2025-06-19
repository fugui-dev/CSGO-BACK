package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.sys.TeamUsersParam;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtUserBody;
import com.ruoyi.domain.other.TtUserPackSackBody;
import com.ruoyi.domain.vo.TeamDetailSimpleVO;
import com.ruoyi.domain.vo.TtUserPackSackDataVO;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.domain.other.ApiUserOnline;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.vo.sys.SimpleTtUserVO;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface TtUserService extends IService<TtUser> {

    List<TtUser> queryList(TtUserBody ttUserBody);

    AjaxResult updateUserById(TtUser ttUser);

    void generateAccount(HttpServletResponse response, List<TtUser> accountList);

    List<TtUser> getAccountList(Integer num);

    boolean checkUserNameUnique(TtUser user);

    boolean checkPhoneUnique(TtUser user);

    boolean checkIdNumUnique(TtUser user);

    String getInvitationCode();

    public TtUser selectTtUserById(Long id);

    ApiUserOnline loginUserToUserOnline(LoginUser user);

    ApiUserOnline selectOnlineByInfo(String ipaddr, String userName, LoginUser user);

    ApiUserOnline selectOnlineByIpaddr(String ipaddr, LoginUser user);

    ApiUserOnline selectOnlineByUserName(String userName, LoginUser user);

    void insertUserAmountRecords(Integer userId, TtAccountRecordType type, TtAccountRecordSource source, BigDecimal amount, BigDecimal finalAmount);
    void insertUserAmountRecords(Integer userId, TtAccountRecordType type, TtAccountRecordSource source, BigDecimal amount, BigDecimal finalAmount,
                                 Integer pwChildId, String pwChildName, BigDecimal childAccount);

    void insertUserAmountRecords(Integer userId, TtAccountRecordType type, TtAccountRecordSource source, BigDecimal amount, BigDecimal finalAmount,
                                 Integer taskId);

    void insertUserCreditsRecords(Integer userId, TtAccountRecordType type, TtAccountRecordSource source, BigDecimal credits, BigDecimal finalCredits);

    void insertUserCreditsRecords(Integer userId, TtAccountRecordType type, TtAccountRecordSource source, BigDecimal credits, BigDecimal finalCredits,
                                  Integer PWChildId, String PWChildName, BigDecimal childAccount);

    void insertUserCreditsRecords(Integer userId, TtAccountRecordType type, TtAccountRecordSource source, BigDecimal credits, BigDecimal finalCredits,
                                  Integer taskId);

    List<TtUserPackSackDataVO> getPackSack(TtUserPackSackBody ttUserPackSackBody);

    Map<String, BigDecimal> getUserProfitStatistics(Integer userId);


    List<TtUserPackSackDataVO> propRankOfDay(Timestamp begin,Timestamp end,Integer number);

    R<Page<SimpleTtUserVO>> teamUsers(TeamUsersParam param);

    Map getPromotionDataInfo(Integer userId);

    List<TeamDetailSimpleVO> teamDetailsList(Integer parentId, String beginTime, String endTime, Integer pageSize, Integer pageNum);
}
