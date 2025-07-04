package com.ruoyi.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class queryTemplateSaleByCategoryDataVO {
    private List<queryTemplateSaleByCategoryVO> saleTemplateByCategoryResponseList;
}
