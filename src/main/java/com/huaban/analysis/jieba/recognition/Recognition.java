/*
 * @(#) Recognition
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-26 20:17:23
 *
 */

package com.huaban.analysis.jieba.recognition;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.huaban.analysis.jieba.WordDictionary;
import com.huaban.analysis.jieba.config.PropertyUtil;

import org.elasticsearch.SpecialPermission;
import org.fnlp.nlp.cn.ner.TimeNormalizer;
import org.fnlp.nlp.cn.ner.TimeUnit;
import org.fnlp.nlp.cn.tag.POSTagger;
import org.fnlp.util.exception.LoadModelException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 各种文本预处理
 */
public class Recognition {

    private static Recognition reg;
    private static POSTagger pos; //词性标注器
    private static JiebaSegmenter seg; //分词器
    private static TimeNormalizer normalizer;
    private static final Map<Integer, Pattern> patternMap = new LinkedHashMap<Integer, Pattern>(); //保存各种正则表达式，注意正则匹配的顺序

    static {
        String carnumRegex = "([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}(\\s*)(([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}(\\s*)[A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳]{1})";
        String sfzhRegex = "[1-9]\\d{16}[a-zA-Z0-9]{1}"; //身份证号
        String mobileRegex = "(\\+\\d+)?1[3458]\\d{9}";//手机号
        String phoneRegex = "(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}";//固定电话

        patternMap.put(SegToken.ENTITY_CHEPAI, Pattern.compile(carnumRegex));
        patternMap.put(SegToken.ENTITY_SFZH, Pattern.compile(sfzhRegex));
        patternMap.put(SegToken.ENTITY_MOBILE, Pattern.compile(mobileRegex));
        patternMap.put(SegToken.ENTITY_PHONE, Pattern.compile(phoneRegex));
    }

    private Recognition() {
        seg = new JiebaSegmenter();
        try {
            SpecialPermission.check();
            AccessController.doPrivileged((PrivilegedAction<Recognition>) () -> {
                Path config = Paths.get(PropertyUtil.getKey("config"));
                try {
                    pos = new POSTagger(config.resolve("pos.m").toAbsolutePath().toString()); //只加载词性库
                    normalizer = new TimeNormalizer(config.resolve("time.m").toAbsolutePath().toString());
                } catch (LoadModelException e) {
                    e.printStackTrace();
                }
                return this;
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Recognition get() {
        if (reg == null) {
            synchronized (Recognition.class) {
                if (reg == null) {
                    reg = new Recognition();
                    return reg;
                }
            }
        }
        return reg;
    }

    /**
     * 识别句子中的人名、地名
     * @param tokens 基于分词结果
     * @return map
     */
    public List<SegToken> reg(List<SegToken> tokens) {
        Predicate<SegToken> lengthRule = (token) -> token.word.length() > 1;
        Predicate<SegToken> noStopword = (token) -> !WordDictionary.getInstance().getStopWordsSet().contains(token.word);
        Predicate<SegToken> noNumRule = (token) -> !token.word.matches("^\\d{1,6}$"); //1-6位数字
        Predicate<SegToken> andCondition = lengthRule.and(noStopword).and(noNumRule);

        List<SegToken> newTokens = new ArrayList<SegToken>();
        List<Integer> newIndex = new ArrayList<Integer>();

        int size = tokens.size(); //为了保证识别准确性，踢出干扰词语（停止词、单个词）
        for (int index = 0; index < size; index++) {
            SegToken token = tokens.get(index);
            if (andCondition.test(token)) {
                newTokens.add(token);
                newIndex.add(index);
            }
        }
        List<String> words = newTokens.stream().map(word -> word.word).collect(Collectors.toList());
        if (words.isEmpty()) { //如全是单个字
            return tokens;
        }
        String[] strings = new String[words.size()];
        words.toArray(strings);
        String[] results = pos.tagSeged(strings); //results存每个词的词性
        HashMap<String, String> map = new HashMap<String, String>();
        for (int j = 0; j < results.length; ++j) {
            SegToken t = tokens.get(newIndex.get(j));
            if ("人名".equals(results[j])) {
                map.put(t.word, results[j]);
                if (t.entity == -1) { //如果通过正则表达式已经匹配，不在标注
                    t.entity = SegToken.ENTITY_PNAME;
                }
            } else if ("地名".equals(results[j])) {
                map.put(t.word, results[j]);
                if (t.entity == -1) { //如果通过正则表达式已经匹配，不在标注
                    t.entity = SegToken.ENTITY_DNAME;
                }
            } else if ("专有名".equals(results[j])) {
                map.put(t.word, results[j]);
                if (t.entity == -1) { //如果通过正则表达式已经匹配，不在标注
                    t.entity = SegToken.ENTITY_ZNAME;
                }
            } else if ("机构名".equals(results[j])) {
                map.put(t.word, results[j]);
                if (t.entity == -1) { //如果通过正则表达式已经匹配，不在标注
                    t.entity = SegToken.ENTITY_GNAME;
                }
            }
        }
        System.out.println(map);
        return tokens;
    }

    /**
     * 执行各种正则匹配
     * @param paragraph 句子
     * @return HandlePattern
     */
    public HandlePattern preHandlerWithPatterns(String paragraph) {
        List<SegToken> entities = new ArrayList<SegToken>();
        String target = paragraph;
        for (Map.Entry<Integer, Pattern> entry : patternMap.entrySet()) {
            Integer type = entry.getKey();
            Pattern pattern = entry.getValue();
            HandlePattern p = this.preHandlerWithPatterns(target, pattern, type);
            target = p.target;
            entities.addAll(p.entities);
        }
        HandlePattern hp = new HandlePattern(paragraph, target);
        hp.addEntityAll(entities);

        return hp;
    }

    /**
     * 执行正则处理器
     * @param paragraph 句子
     * @param pattern 正则
     * @param type 类型 0 1 2 3 SegToken.常量
     * @return 结果
     */
    private HandlePattern preHandlerWithPatterns(String paragraph, Pattern pattern, Integer type) {
        HandlePattern hp = new HandlePattern(paragraph, paragraph);

        Matcher matcher = pattern.matcher(paragraph);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            System.out.println(matcher.group() + ":" + matcher.start() + "-" + matcher.end());
            int length = matcher.end() - matcher.start();
            String fill = Stream.iterate("#", i -> i).limit(length).collect(Collectors.joining());
            matcher.appendReplacement(sb, fill);
            hp.addEntity(new SegToken(matcher.group(), matcher.start(), matcher.end(), type));
        }
        matcher.appendTail(sb);
        hp.setTarget(sb.toString());
        return hp;
    }

    /**
     * 分析句子中的事件
     * @param sentence
     * @return
     */
    public List<TimeUnit> parseTime(String sentence) {
        normalizer.parse(sentence);
        TimeUnit[] unit = normalizer.getTimeUnit();
        return Arrays.stream(unit).filter(u -> !(u.Time_Norm == null || u.Time_Norm.trim().isEmpty())).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        String str = "2019年10月10号凌晨2点半，大渡口区春晖路街道涟水交警接警后，发现现场除了受害者李梅上官风，身份证号500102198702014291，电话18602314263，固定电话68574345，家住：大渡口区建胜镇龙桥花苑小区2栋7-3的血迹、一块机动车号牌苏A 7001U和苏A7001U少量玻璃碎片外，没有其他有价值的线索。大渡口区经信委，重庆市秋田齿轮有限公司对此进行了调查，令人悲痛的是，第二天上午10点，伤者也因伤势过重经抢救无效身亡。";
        JiebaSegmenter seg = new JiebaSegmenter();

        List<SegToken> tokens = seg.process(str, JiebaSegmenter.SegMode.INDEX);//用分词器切词
        System.out.println(tokens + "------------------");

        Recognition.get().parseTime(str).stream().forEach(System.out::println);

        // Arrays.stream(results).forEach(System.out::println);
    }

}
