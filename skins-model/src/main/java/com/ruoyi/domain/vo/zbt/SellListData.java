package com.ruoyi.domain.vo.zbt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SellListData {

    private Integer limit;
    private List<SellItemInfo> list;
    private String offsetToken;
    private Integer page;
    private Integer pages;
    private String scrollId;
    private Integer total;

}
