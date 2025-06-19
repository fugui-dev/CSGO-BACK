package com.ruoyi.thirdparty.common.NetworkDataLoader;

import cn.hutool.core.util.HashUtil;
import com.ruoyi.common.core.domain.entity.SysDictData;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
@Data
public abstract class BaseNetworkDataLoader {

    public String name;

    protected List<SysDictData> allEnum;

    // 根据类型和标签获取code
    protected String getCodeByNameOrHash(List<SysDictData> typeList, String nameOrHash){

        for (SysDictData type : typeList){
            if (type.getDictLabel().equals(nameOrHash)) return type.getDictValue();
            if (type.getDictType().equals(nameOrHash)) return type.getDictValue();
        }
        return null;
    }

    protected Long createOrnamentId(String MarketHashName){
        Long id = Long.valueOf(HashUtil.rsHash(MarketHashName));
        if (id < 0) id = Math.abs(id);
        return id;
    }

}
