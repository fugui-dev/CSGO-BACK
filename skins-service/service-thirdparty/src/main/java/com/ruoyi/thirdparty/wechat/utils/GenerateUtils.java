package com.ruoyi.thirdparty.wechat.utils;

import java.text.SimpleDateFormat;
import java.util.*;

public class GenerateUtils {
    public static String getOrderIdByUUId() {

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        String first = df.format(new Date());
        System.out.println(first);
        int hashCodeV = UUID.randomUUID().toString().hashCode();
        if (hashCodeV < 0) {//有可能是负数
            hashCodeV = -hashCodeV;
        }
        // 0 代表前面补充0
        // 4 代表长度为4
        // d 代表参数为正数型
        return first + String.format("%015d", hashCodeV);
    }

    /**
     * @author 作者：Jason E-mail: Jason_Lee_lizhen@outlook.com
     * @copyright All Rights Reserved @version v1.0
     * @version 创建时间：2019年7月1日 上午9:56:16
     * @description方法说明 :获取UUID
     * @arg 入参类型：*****；出参类型：String
     */
    public static String generateUUid() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");


        return uuid;
    }

    /**
     * @author 作者：Jason E-mail: Jason_Lee_lizhen@outlook.com
     * @copyright All Rights Reserved @version v1.0
     * @version 创建时间：2019年7月1日 上午9:58:40
     * @description方法说明 :生成商户号
     * @arg 入参类型：*****；出参类型：****
     */
    public static String generateStoreId() {

//		注册年月日+CHAMBROAD+四位随机数
        //加上三位随机数
        String dateString = DateUtils.queryCurrentDay().replaceAll("-", "");
        Random random = new Random();
        int end4 = random.nextInt(9999);
        String storeId = "store-" + dateString + "-LEE-" + end4;


        return storeId;
    }

    /**
     * @author 作者：Jason E-mail: Jason_Lee_lizhen@outlook.com
     * @copyright All Rights Reserved @version v1.0
     * @version 创建时间：2019年8月9日 上午10:34:08
     * @description方法说明 : 生成用户名
     * @arg 入参类型：*****；出参类型：****
     */
    public static String getStringRandom(int length) {

        String val = "";
        Random random = new Random();

        //参数length，表示生成几位随机数
        for (int i = 0; i < length; i++) {

            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) {
                //输出是大写字母还是小写字母
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char) (random.nextInt(26) + temp);
            } else if ("num".equalsIgnoreCase(charOrNum)) {
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val;
    }

    public static int Guid = 1;

    public static String guidDate = "2021070900";

    public static int guidNew() {
        SimpleDateFormat simpleDateFormatDay = new SimpleDateFormat("yyyyMMddHH");
        String outTradeDate = simpleDateFormatDay.format(new Date());
        if (!outTradeDate.equals(GenerateUtils.guidDate)) {
            GenerateUtils.guidDate = outTradeDate;
            GenerateUtils.Guid = 1;
            return 1;
        }
        return GenerateUtils.Guid;


    }

    /**
     * @author 作者：Jason E-mail: Jason_Lee_lizhen@outlook.com
     * @copyright All Rights Reserved @version v1.0
     * @version 创建时间：2021年7月12日 下午2:48:15
     * @description方法说明 :
     * @arg 入参类型：*****workId  String   分布式系统中传入机器码；出参类型：****String 生成的以yyyyMMddHH开头的有序单号
     */

    public static String getGuid(String workId) {

        GenerateUtils.Guid += 1;

        int ran = 0;

        ran = GenerateUtils.guidNew();
        String guidIntStr = addZeroForNum(ran + "", 10);


        return GenerateUtils.guidDate + guidIntStr + workId;
    }

    public static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(str);//左补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }


    public static List<String> GuidList = new ArrayList<String>();
    public static int GuidIndex = 0;


    /**
     * @author 作者：Jason E-mail: Jason_Lee_lizhen@outlook.com
     * @copyright All Rights Reserved @version v1.0
     * @version 创建时间：2021年7月28日 上午10:54:24
     * @description方法说明 :获取最新下标
     * @arg 入参类型：*****；出参类型：****
     */
    public static int getGuidIndex() {

        SimpleDateFormat simpleDateFormatDay = new SimpleDateFormat("yyyyMMddHH");
        String outTradeDate = simpleDateFormatDay.format(new Date());
        if (!outTradeDate.equals(GenerateUtils.guidDate)) {
            GenerateUtils.guidDate = outTradeDate;
            Collections.shuffle(GuidList);
            GenerateUtils.GuidIndex = 0;
        }
        return GenerateUtils.GuidIndex;

    }

    /**
     * @author 作者：Jason E-mail: Jason_Lee_lizhen@outlook.com
     * @copyright All Rights Reserved @version v1.0
     * @version 创建时间：2021年7月28日 上午10:54:50
     * @description方法说明 :获取无序单号
     * @arg 入参类型：*****；出参类型：****String 生成的以yyyyMMddHH开头的无序单号
     */
    public static String getGuidRand(String workId) {

        GenerateUtils.GuidIndex += 1;

        long now = System.currentTimeMillis();
        String info = now + "";
        int ran = 0;

        ran = GenerateUtils.getGuidIndex();
        String guidIntStr = GuidList.get(ran);


        return GenerateUtils.guidDate + guidIntStr + workId;
    }


//	public static void main (String args[]) {
////		for(int i=0;i<999999;i++) {
////			GuidList.add(addZeroForNum(i+"",6));
////			System.out.println(GuidList.get(i));
////		}
//		for(int i=0;i<999999;i++) {
//			System.out.println(getGuidRand(""));
//			try {
//				Thread.sleep(5 * 1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//
//	}

    // 生成6位随机验证码
    public static String generateCode() {
        Random randObj = new Random();
        return Integer.toString(100000 + randObj.nextInt(900000));
    }

    // 生成4位随机验证码
    public static String generateCode4() {
        Random randObj = new Random();
        return Integer.toString(1000 + randObj.nextInt(9000));
    }


    public static void main(String args[]) {
        for (int i = 0; i < 100000000; i++) {
            System.out.println(getGuid(""));
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }


}
