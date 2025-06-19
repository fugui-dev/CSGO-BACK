package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtUserPackSackBody;
import com.ruoyi.domain.vo.TtUserPackSackDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface TtUserMapper extends BaseMapper<TtUser> {
    List<TtUserPackSackDataVO> getPackSack(TtUserPackSackBody ttUserPackSackBody);

    //@MapKey("userId")
    Map<String, BigDecimal> getUserProfitStatistics(Integer userId);

    public TtUser selectTtUserById(Long id);

    int updateTtUserCoin(@Param("userId") Long userId, @Param("money") BigDecimal money);

    int updateAccountAmount(@Param("userId") Long userId, @Param("money") BigDecimal money);

    // List<TtUserPackSackDataVO> propRankOfDay(@Param("sources") String[] sources,
    //                                          @Param("createTime") String createTime,
    //                                          @Param("page") Integer page,
    //                                          @Param("size") Integer size
    //                                          );

    List<Integer> propRankUsers(@Param("sources") Integer[] sources,
                            @Param("begin") String begin,
                            @Param("end") String end);

    List<TtUserPackSackDataVO> maxPricePropByUserIds(@Param("sources") Integer[] sources,
                                              @Param("beginT") String beginT,
                                              @Param("endT") String endT,
                                              @Param("userId") List<Integer> userIds,
                                              @Param("size") Integer size);

    List<Integer> allEmployeesByParents(@Param("bossIds") List<Integer> bossIds);

    Integer getBdPromotionRegisterByChannelId(Integer id);


}
