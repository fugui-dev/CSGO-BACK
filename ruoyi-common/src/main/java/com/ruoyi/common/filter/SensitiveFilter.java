package com.ruoyi.common.filter;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;


/**
 * ----------------------------------------------------------------------
 * | Copyright (c) 2019~2022 智能云科技 znyun.net.cn All rights reserved. |
 * | 面向系统：节省您的时间，拓展您的空间                                     |
 * | License     ：开源仅供学习，正式版源码购买后方可授权商用                  |
 * | Author      ：智能云团队 <2701596751@qq.com>                         |
 * ----------------------------------------------------------------------
 */
@Slf4j
@Component
public class SensitiveFilter {

    /**
     * 敏感词过滤器：利用DFA算法  进行敏感词过滤
     */
    private Map sensitiveWordMap = null;

    /**
     * 最小匹配规则，如：敏感词库["中国","中国人"]，语句："我是中国人"，匹配结果：我是[中国]人
     */
    public static final int MIN_MATCH_TYPE = 1;

    /**
     * 最大匹配规则，如：敏感词库["中国","中国人"]，语句："我是中国人"，匹配结果：我是[中国人]
     */
    public static final int MAX_MATCH_TYPE = 2;

    /**
     * 敏感词替换词
     */
    public static String placeHolder = "*";

    @PostConstruct
    private void initKeyWord() throws IOException {
        Set<String> strings = readSensitiveWordFile();
        if(ObjectUtil.isNotNull(strings)) {
            sensitiveWordMap = addSensitiveWordToHashMap(strings);
        }
    }

    // 读取敏感词库 ,存入HashMap中
    private Set<String> readSensitiveWordFile() throws IOException {
        Set<String> wordSet = null;
        ClassPathResource classPathResource = new ClassPathResource("words.txt");
        if(!classPathResource.exists()) {
            return null;
        }

        InputStream inputStream = classPathResource.getInputStream();
        //敏感词库
        try {
            // 读取文件输入流
            InputStreamReader read = new InputStreamReader(inputStream, "UTF-8");
            // 文件是否是文件 和 是否存在
            wordSet = new HashSet<String>();
            // StringBuffer sb = new StringBuffer();
            // BufferedReader是包装类，先把字符读到缓存里，到缓存满了，再读入内存，提高了读的效率。
            BufferedReader br = new BufferedReader(read);
            String txt = null;
            // 读取文件，将文件内容放入到set中
            while ((txt = br.readLine()) != null) {
                wordSet.add(txt);
            }
            br.close();
            // 关闭文件流
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wordSet;
    }

    // 将HashSet中的敏感词,存入HashMap中
    private Map addSensitiveWordToHashMap(Set<String> wordSet) {
        // 初始化敏感词容器，减少扩容操作
        Map wordMap = new HashMap(wordSet.size());
        for (String word : wordSet) {
            Map nowMap = wordMap;
            for (int i = 0; i < word.length(); i++) {
                // 转换成char型
                char keyChar = word.charAt(i);
                // 获取
                Object tempMap = nowMap.get(keyChar);
                // 如果存在该key，直接赋值
                if (tempMap != null) {
                    nowMap = (Map) tempMap;
                }
                // 不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
                else {
                    // 设置标志位
                    Map<String, String> newMap = new HashMap<String, String>();
                    newMap.put("isEnd", "0");
                    // 添加到集合
                    nowMap.put(keyChar, newMap);
                    nowMap = newMap;
                }
                // 最后一个
                if (i == word.length() - 1) {
                    nowMap.put("isEnd", "1");
                }
            }
        }
        return wordMap;
    }

    /**
     * 获取文字中的敏感词
     */
    public Set<String> getSensitiveWord(String txt, int matchType) {
        Set<String> sensitiveWordList = new HashSet<>();
        for (int i = 0; i < txt.length(); i++) {
            // 判断是否包含敏感字符
            int length = checkSensitiveWord(txt, i, matchType);
            // 存在,加入list中
            if (length > 0) {
                sensitiveWordList.add(txt.substring(i, i + length));
                // 减1的原因，是因为for会自增
                i = i + length - 1;
            }
        }
        return sensitiveWordList;
    }


    /**
     * 替换敏感字字符,使用了默认的替换符合，默认最小匹配规则
     */
    public String replaceSensitiveWord(String txt) {
        if(ObjectUtil.isNotNull(sensitiveWordMap)) {
            return replaceSensitiveWord(txt, MIN_MATCH_TYPE, placeHolder);
        }
        return txt;
    }

    /**
     * 替换敏感字字符,使用了默认的替换符合
     */
    public String replaceSensitiveWord(String txt, int matchType) {
        if(ObjectUtil.isNotNull(sensitiveWordMap)) {
            return replaceSensitiveWord(txt, matchType, placeHolder);
        }
        return txt;
    }

    /**
     * 替换敏感字字符
     */
    public String replaceSensitiveWord(String txt, int matchType, String replaceChar) {
        if(ObjectUtil.isNull(sensitiveWordMap)) {
            return txt;
        }

        String resultTxt = txt;
        // 获取所有的敏感词
        Set<String> set = getSensitiveWord(txt, matchType);
        Iterator<String> iterator = set.iterator();
        String word = null;
        String replaceString = null;
        while (iterator.hasNext()) {
            word = iterator.next();
            replaceString = getReplaceChars(replaceChar, word.length());
            resultTxt = resultTxt.replaceAll(word, replaceString);
        }
        return resultTxt;
    }

    /**
     * 获取替换字符串
     */
    private String getReplaceChars(String replaceChar, int length) {
        StringBuilder resultReplace = new StringBuilder(replaceChar);
        for (int i = 1; i < length; i++) {
            resultReplace.append(replaceChar);
        }
        return resultReplace.toString();
    }

    /**
     * 检查文字中是否包含敏感字符，检查规则如下：<br>
     * 如果存在，则返回敏感词字符的长度，不存在返回0
     * 核心
     */
    public int checkSensitiveWord(String txt, int beginIndex, int matchType) {
        // 敏感词结束标识位：用于敏感词只有1的情况结束
        boolean flag = false;
        // 匹配标识数默认为0
        int matchFlag = 0;
        Map nowMap = sensitiveWordMap;
        for (int i = beginIndex; i < txt.length(); i++) {
            char word = txt.charAt(i);
            // 获取指定key
            nowMap = (Map) nowMap.get(word);
            // 存在，则判断是否为最后一个
            if (nowMap != null) {
                // 找到相应key，匹配标识+1
                matchFlag++;
                // 如果为最后一个匹配规则,结束循环，返回匹配标识数
                if ("1".equals(nowMap.get("isEnd"))) {
                    // 结束标志位为true
                    flag = true;
                    // 最小规则，直接返回,最大规则还需继续查找
                    if (MIN_MATCH_TYPE == matchType) {
                        break;
                    }
                }
            }
            // 不存在，直接返回
            else {
                break;
            }
        }

        // 匹配长度如果匹配上了最小匹配长度或者最大匹配长度
        if (MAX_MATCH_TYPE == matchType || MIN_MATCH_TYPE == matchType) {
            //长度必须大于等于1，为词，或者敏感词库还没有结束(匹配了一半)，flag为false
            if (matchFlag < 2 || !flag) {
                matchFlag = 0;
            }
        }
        return matchFlag;
    }

    /**
     * 仅判断txt中是否有关键字
     *
     * @param txt
     * @return
     */
    public boolean isContentKeyWords(String txt) {
        if(ObjectUtil.isNull(sensitiveWordMap)) {
            return false;
        }

        for (int i = 0; i < txt.length(); i++) {
            int len = checkSensitiveWord(txt, i, 1);
            if (len > 0) {
                return true;
            }
        }
        txt = null;
        return false;
    }
}
