package com.ruoyi.admin.util;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class RandomUtils {

    private static final Logger log = LoggerFactory.getLogger(RandomUtils.class);

    private static final int SURNAME_PROBABILITY = 5;
    private static final String FAMILY_ONE_NAME = "赵钱孙李周吴郑王冯陈褚卫蒋沈韩杨朱秦尤许何吕施张孔曹严华金魏陶姜戚谢邹喻水云苏潘葛奚范彭郎鲁韦昌马苗凤花方俞任" +
            "袁柳鲍史唐费岑薛雷贺倪汤滕殷罗毕郝邬安常乐于时傅卞齐康伍余元卜顾孟平黄和穆萧尹姚邵湛汪祁毛禹狄米贝明臧计成戴宋茅庞熊纪舒屈项祝董粱杜阮" +
            "席季麻强贾路娄危江童颜郭梅盛林刁钟徐邱骆高夏蔡田胡凌霍万柯卢莫房缪干解应宗丁宣邓郁单杭洪包诸左石崔吉龚程邢滑裴陆荣翁荀羊甄家封芮储靳邴" +
            "松井富乌焦巴弓牧隗山谷车侯伊宁仇祖武符刘景詹束龙叶幸司韶黎乔苍双闻莘劳逄姬冉宰桂牛寿通边燕冀尚农温庄晏瞿茹习鱼容向古戈终居衡步都耿满弘" +
            "国文东殴沃曾关红游盖益桓公晋楚闫";

    private static final String FAMILY_TWO_NAME = "欧阳太史端木上官司马东方独孤南宫万俟闻人夏侯诸葛尉迟公羊赫连澹台皇甫宗政濮阳公冶太叔申屠公孙慕容仲孙钟离长孙宇" +
            "文司徒鲜于司空闾丘子车亓官司寇巫马公西颛孙壤驷公良漆雕乐正宰父谷梁拓跋夹谷轩辕令狐段干百里呼延东郭南门羊舌微生公户公玉公仪梁丘公仲公上" +
            "公门公山公坚左丘公伯西门公祖第五公乘贯丘公皙南荣东里东宫仲长子书子桑即墨达奚褚师吴铭";

    public static String getRandomName(int sex) {
        String girlName = "秀娟英华慧巧美娜静淑惠珠翠雅芝玉萍红娥玲芬芳燕彩春菊兰凤洁梅琳素云莲真环雪荣爱妹霞香月莺媛艳瑞凡佳嘉琼勤珍贞莉桂娣叶璧" +
                "璐娅琦晶妍茜秋珊莎锦黛青倩婷姣婉娴瑾颖露瑶怡婵雁蓓纨仪荷丹蓉眉君琴蕊薇菁梦岚苑婕馨瑗琰韵融园艺咏卿聪澜纯毓悦昭冰爽琬茗羽希宁欣飘育滢馥" +
                "筠柔竹霭凝晓欢霄枫芸菲寒伊亚宜可姬舒影荔枝思丽";
        String boyName = "伟刚勇毅俊峰强军平保东文辉力明永健世广志义兴良海山仁波宁贵福生龙元全国胜学祥才发武新利清飞彬富顺信子杰涛昌成康星光天达" +
                "安岩中茂进林有坚和彪博诚先敬震振壮会思群豪心邦承乐绍功松善厚庆磊民友裕河哲江超浩亮政谦亨奇固之轮翰朗伯宏言若鸣朋斌梁栋维启克伦翔旭鹏泽" +
                "晨辰士以建家致树炎德行时泰盛雄琛钧冠策腾楠榕风航弘";
        return sex == 1 ? getRandomName(boyName) : getRandomName(girlName);
    }

    private static String getRandomName(String name) {
        int bodNameIndexOne = randomInt(name.length());
        int bodNameIndexTwo = randomInt(name.length());
        if (randomInt(100) > SURNAME_PROBABILITY) {
            int familyOneNameIndex = randomInt(FAMILY_ONE_NAME.length());
            return FAMILY_ONE_NAME.charAt(familyOneNameIndex) +
                    name.substring(bodNameIndexOne, bodNameIndexOne + 1) +
                    name.charAt(bodNameIndexTwo);
        } else {
            int familyTwoNameIndex = randomInt(FAMILY_TWO_NAME.length());
            familyTwoNameIndex = familyTwoNameIndex % 2 == 0 ? familyTwoNameIndex : familyTwoNameIndex - 1;
            return FAMILY_TWO_NAME.substring(familyTwoNameIndex, familyTwoNameIndex + 2) +
                    name.charAt(bodNameIndexOne) +
                    name.charAt(bodNameIndexTwo);
        }
    }

    public static int randomInt(int maxNum) {
        Random random = new Random();
        return random.nextInt(maxNum);
    }

    public static List<Long> toList(Map<Long, Integer> data) {
        List<Long> resultList = new ArrayList<>();
        try {
            for (Map.Entry<Long, Integer> entry : data.entrySet()) {
                Long key = entry.getKey();
                Integer value = entry.getValue();
                for (int i = 0; i < value; i++) {
                    resultList.add(key);
                }
            }
            Collections.shuffle(resultList);
        } catch (Exception e) {
            log.error("调用RandomUtils.toList()方法时出现异常，请检查代码！");
        }
        return resultList;
    }

    public static int getRandomIndex(int maxValue) {
        Random random = new Random();
        return random.nextInt(maxValue);
    }

    public static BigDecimal getRandomPrice(String priceSectionJSONStr){
        List<BigDecimal> priceSection = JSONObject.parseObject(priceSectionJSONStr, new TypeReference<List<BigDecimal>>() {
        });
        if (priceSection.get(0).compareTo(priceSection.get(1)) == 0) {
            return priceSection.get(0);
        } else if (priceSection.get(0).compareTo(priceSection.get(1)) > 0) {
            return RandomUtil.randomBigDecimal(priceSection.get(1), priceSection.get(0));
        } else {
            return RandomUtil.randomBigDecimal(priceSection.get(0), priceSection.get(1));
        }
    }
}
