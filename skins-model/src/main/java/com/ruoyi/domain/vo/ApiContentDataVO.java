package com.ruoyi.domain.vo;

import com.ruoyi.domain.other.TtContent;
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
public class ApiContentDataVO {

    private Integer contentTypeId;

    private String contentTypeName;

    private String contentAlias;

    private List<TtContent> contentList;

}
