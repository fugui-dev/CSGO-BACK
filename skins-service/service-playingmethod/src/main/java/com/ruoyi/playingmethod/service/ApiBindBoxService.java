package com.ruoyi.playingmethod.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtBoxA;
import com.ruoyi.domain.other.TtOrnamentsA;
import com.ruoyi.domain.other.TtBoxVO;
import com.ruoyi.domain.vo.OpenBoxVO;
import com.ruoyi.playingmethod.entity.request.OpenBox2Request;

import java.util.List;

public interface ApiBindBoxService {

    TtBoxA getBoxData(Integer boxId);

    List<TtBoxVO> getBoxList(Integer boxTypeId, String homeFlag, Integer isFight);

    List<TtBoxVO> groupByBoxType(List<TtBoxA> boxData, Integer isFight);

    List<TtOrnamentsA> openBox(TtBox ttBox, Integer num, TtUser ttUser);

    List<TtBoxRecords> addBoxRecord(TtUser ttUser, TtBox ttBox, Integer num);

    List<TtBoxRecords> openBoxArithmetic(Integer fightId,TtUser ttUser, TtBox ttBox, Integer num);

    // List<TtBoxUser> getBindBoxHistory(ApiBindBoxController.boxHistoryParam param);

    R blindBox(TtUser ttUser, TtBox ttBox, Integer num);


    //机器人开箱
    R blindBoxReboot(TtUser ttUser, TtBox ttBox, Integer num);

    R<List<OpenBoxVO>> openBox2(OpenBox2Request openBox2Request);

}
