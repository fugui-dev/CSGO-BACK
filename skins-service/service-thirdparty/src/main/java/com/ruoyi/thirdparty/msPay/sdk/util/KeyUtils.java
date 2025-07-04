package com.ruoyi.thirdparty.msPay.sdk.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class KeyUtils {

    /**
     * 测试参数 ###############
     */
    public static final String TEST_DEFAULT_MERCHANT_ID = "900029000000354";
    public static final String TEST_DEFAULT_PARTNER = "900029000000354";
    //测试商户私钥
    public static final String  TEST_MERCHANT_PRIVATE_KEY="MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAIqUuxd92eEBXVneDWhfNP6XCkLcGBO1YAulexKX+OdlfZzB/4NNHkOAQQy84k3ZgIUPIk5hewLbA+XGrk9Wih5HG3ZQeFugeoTcx3vwo7AQv7KnmcKEWFNlOr/EhB3JndmcQnBRsIRRdCP+7nobfBqU0jS8dnpcQX1AtBRZRnkfAgMBAAECgYAe+u70ansZ1Q9EduKycY5MWAHAPqnXRhXppJ3l4zmOqV6ye6Aef1ADsRlZuqQw2S3lESQPN7WjRskRRiBTtjn8Atul9YeC7+QirP1K8seUP5gKB4bcjlzzl1m5dmxldkptJAmdzwYn8PRTW0+tFVyEaD/B8hKGxij4Gew0e8bwCQJBAOboG3ttBESsG2cAtmP1MfKRTjVdY7qRMXzBybcAeobBbmgCQgybVXXgjbGai+qwrQqcVRIp6p1yDWTZxVSuDWsCQQCZpBhcayOCMZR6F8dQJSuSSSIJw/GGN7IXfMYIqLxA2oGzlQ0B1DffOUe2wrid+WdpLuYCz2LYPQHDEgYM1dwdAkEAnfwhEYm9ad73wLnUEQAqdHTGtex316aP3XQZt4Q0UQ73o2IoHsgI6OYDDIlZQfIv8xqTeiIDzEXEtEPrp8yOkQJBAIWAzFZKFqHD2UO6M8vVcKX9fGFF7TH2ZX75Qc82Z9ZmyDs2sgW71QzX5hPN4cQLeqswQFeCw14orMZHfBBdKJUCQQDiWYk85okRugsWtxeJFhMEt2oUT+Kd8Yz5Aiz3J9XIS+zWtJrFlv+hXkVedPJ3xtBF32DZrCbxDn3UjXipRaCP";
    public static final String TEST_EASYPAY_PUBLIC_KEY="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC2WTfvas1JvvaRuJWIKmKlBLmkRvr2O7Fu3k/zvhJs+X1JQorPWq/yZduY6HKu0up7Qi3T6ULHWyKBS1nRqhhHpmLHnI3sIO8E/RzNXJiTd9/bpXMv+H8F8DW5ElLxCIVuwHBROkBLWS9fIpslkFPt+r13oKFnuWhXgRr+K/YkJQIDAQAB";

    //测试访问地址
    public static String DEFAULT_URL = "http://test_nucc.bhecard.com:9088/api_gateway.do";
//    public static String DEFAULT_URL = "http://localhost:9087/api_gateway.do";
//    public static String DEFAULT_URL = "https://180.168.215.67:9088/api_gateway.do";
    public static String TEST_DES_ENCODE_KEY = "CueaiPrW9sIskbn9qkoPh9J3";


    /**
     * 生产参数 ###############
     */
//    public static final String SC_DEFAULT_NOTIFY = "http://test2398.mynatapp.cc/api/msPay/notify";
    public static final String SC_DEFAULT_NOTIFY = "http://api.991skins.com/prod-api/api/msPay/notify";

    public static final String SC_DEFAULT_PARTNER = "900065100025058";
    public static final String SC_MERCHANT_PRIVATE_KEY = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAILMw/JDkhQrgjwkMSnLjK8ocAlK2/cU/Bgq5ZeaaDCB8VyqJSQyUzmch5kqcKwCwIddOwrGtKCbwKGzViCx5b3eIClxDsZvPT48mEyBx1TDQjaj/Mon1NbJeYoYqO+ImqzHOnT7qYpwIt48fh+A2PZ/bMprbUtw27ZxvW50dPC/AgMBAAECgYBcDx6Cbohr5ct95MMtdXQI+98Oyz9SAdUEdXyuXSCjs3FbFO2bMkhdLgxKAxLWHgG/xCCADyamO7kylfQygsL9Abnmsbg/6MeRyKsU9dnIpHkp1qYDj6IM7JeclczzzwNTfEIMgeemxxiY0sHqPpg67OxKdkZod7tNm01zcfeEgQJBAM9B017VqdpmGs1C29ovD2lNpiWaOIAdJ+p4yc4GcAKn5ZQW3M6O+CeEVUYK+GYsxpuOH1OwarYQtlv9UwMQEiECQQChj7wMnMI4sNnt3onhPoZAeII+ByMIwZHzs5aaZBtmsgo8MwIU8wHS36Xyzg5yvNQkV9b097ng951yXPrSvGbfAkB3QxmuaKTk1/x0QRTh6FKl9di1qpj4n85oTjlSJMIvxQ2GueE1u29wzeWD+KwXn4xp1s6QCeReAB/vr0YlbQoBAkAlwcWc3PW28f28TYnDji0p2//0Jw7U+Qf3AD1uD38n3OvvbaxK4Q/IDGRn/if6QI8Df1PJJCvYm4W8OGaV4VnZAkAdLHGskC+wu2MdRUsRZXs2XaoU/6EhW/WvCduj+nJgc1R+g/ABGjopypAWALDEvTjOg0PxB3ocBjzp47y9em+H";
    public static final String SC_EASYPAY_PUBLIC_KEY="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC2WTfvas1JvvaRuJWIKmKlBLmkRvr2O7Fu3k/zvhJs+X1JQorPWq/yZduY6HKu0up7Qi3T6ULHWyKBS1nRqhhHpmLHnI3sIO8E/RzNXJiTd9/bpXMv+H8F8DW5ElLxCIVuwHBROkBLWS9fIpslkFPt+r13oKFnuWhXgRr+K/YkJQIDAQAB";
    public static final String SC_DEFAULT_MERCHANT_ID = "900065100025058";
    //生产访问地址
    public static String SC_URL = "https://newpay.bhecard.com/api_gateway.do";
    public static String SC_DES_ENCODE_KEY = "s6yaiIycSFXufo4jEg3VmLs4";

    public static final String TEST_DEFAULT_ENCODE_TYPE = "RSA";
    public static final String TEST_DEFAULT_CHARSET = "UTF-8";


    public static String getSign(String key, String charset, String bizContent) throws Exception {
        return AlipaySignature.rsaSign(bizContent, key, charset);
    }


    public static String getOutTradeNo(){
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(d) +  System.currentTimeMillis();
    }
}
