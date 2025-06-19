package com.ruoyi.thirdparty.realname;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class RealName2Config {

    @Value("${realName2.url}")
    private String url = "https://puhui.shumaidata.com/id_card/check/puhui";

    @Value("${realName2.appCode}")
    private String appCode = "e6476acd9d214b498e680394e9b81e67";
//    private String appCode = "7a6022eb5445443cb1f0eaacdc647b08";

}
