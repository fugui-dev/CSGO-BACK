package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.admin.controller.TtUpgradeFailOrnamentsController;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.other.TtUpgradeFailOrnaments;
import com.ruoyi.domain.vo.TtUpgradeFailOrnamentsDataVO;
import com.ruoyi.domain.vo.upgrade.SimpleOrnamentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface TtUpgradeFailOrnamentsMapper extends BaseMapper<TtUpgradeFailOrnaments> {
    List<TtUpgradeFailOrnamentsDataVO> queryList(TtUpgradeFailOrnamentsController.listParam param);

    List<TtUpgradeFailOrnaments> getFailOrnamentsList(@Param("id") Integer id, @Param("price") BigDecimal price);

    List<SimpleOrnamentVO> ornamentInfoByUpgradeId(@Param("upgradeOrnId") Long upgradeOrnId);

    @Update("TRUNCATE TABLE tt_upgrade_fail_ornaments")
    void truncateTable();

}
