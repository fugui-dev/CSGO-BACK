package com.ruoyi.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;

@Data
public class TtUserPackSackDataVO implements Comparator<TtUserPackSackDataVO> {

    private Long id;

    private Integer holderUserId;

    private String nickName;

    private String avatar;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentsId;

    private String ornamentName;

    private BigDecimal ornamentsPrice;

    private String imageUrl;

    private String status;

    private String source;

    private Integer fightId;

    private Integer rollId;

    private Integer priceRank;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Override
    public int compare(TtUserPackSackDataVO o1, TtUserPackSackDataVO o2) {
        return o1.getOrnamentsPrice().compareTo(o2.getOrnamentsPrice());
    }
}
