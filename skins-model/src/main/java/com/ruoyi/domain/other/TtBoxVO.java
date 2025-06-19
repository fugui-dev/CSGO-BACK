package com.ruoyi.domain.other;

import com.ruoyi.domain.vo.TtOrnamentVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class TtBoxVO {

    private Integer boxTypeId;
    private String boxTypeName;
    private String icon;
    private List<TtBoxA> BoxList;
}
