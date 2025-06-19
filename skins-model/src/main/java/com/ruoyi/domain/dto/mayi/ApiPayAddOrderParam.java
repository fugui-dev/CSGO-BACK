package com.ruoyi.domain.dto.mayi;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiPayAddOrderParam {

    //平台分配商户号
    private String pay_memberid;

    //上送订单号唯一
    private String pay_orderid;

    //提交时间 2016-12-26 18:18:18
    private String pay_applydate;

    //银行编码
    private String pay_bankcode;

    //服务端通知
    private String pay_notifyurl;

    //页面跳转通知
    private String pay_callbackurl;

    //订单金额
    private String pay_amount;

    //MD5 签名
    private String pay_md5sign;

    //附加字段
    private String pay_attach;

    //商品名称
    private String pay_productname;

    //商户品数量
    private String pay_productnum;

    //商品描述
    private String pay_productdesc;

    //商户链接地址
    private String pay_producturl;

    //分账绑卡 id
    private String pay_bindid;

    //客户端 ip
    private String pay_clientip;

    //返回数据格式
    private String type;

}
