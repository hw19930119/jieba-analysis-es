/*
 * @(#) CosineTextSimilarity
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-20 20:27:26
 *
 */

package com.huaban.analysis.jieba.similarity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文本相似度计算
 * 判定方式：余弦相似度，通过计算两个向量的夹角余弦值来评估他们的相似度
 * 余弦夹角原理：
 * 向量a=(x1,y1),向量b=(x2,y2)
 * similarity=a.b/|a|*|b|
 * a.b=x1x2+y1y2
 * |a|=根号[(x1)^2+(y1)^2],|b|=根号[(x2)^2+(y2)^2]
 * @author chenhao
 */
public class CosineTextSimilarity extends TextSimilarity {
    /**
     * 判定相似度的方式：余弦相似度
     * 余弦夹角原理：
     * 向量a=(x1,y1),向量b=(x2,y2)
     * similarity=a.b/|a|*|b|
     * a.b=x1x2+y1y2
     * |a|=根号[(x1)^2+(y1)^2],|b|=根号[(x2)^2+(y2)^2]
     * @param words1 词列表1
     * @param words2 词列表2
     * @return 相似度分值
     */
    @Override
    protected double scoreImpl(List<Word> words1, List<Word> words2) {
        //用词频来标注词的权重
        taggingWeightWithWordFrequency(words1, words2);
        //构造权重快速搜索容器
        Map<String, Float> weights1 = toFastSearchMap(words1);
        Map<String, Float> weights2 = toFastSearchMap(words2);
        //所有的不重复词
        Set<Word> words = new HashSet<>();
        words.addAll(words1);
        words.addAll(words2);
        //向量的维度为words的大小，每一个维度的权重是词频
        //a.b
        AtomicFloat ab = new AtomicFloat();
        //|a|的平方
        AtomicFloat aa = new AtomicFloat();
        //|b|的平方
        AtomicFloat bb = new AtomicFloat();
        //计算
        words
            .stream()
            .forEach(word -> {
                Float x1 = weights1.get(word.getText());
                Float x2 = weights2.get(word.getText());
                if (x1 != null && x2 != null) {
                    //x1x2
                    float oneOfTheDimension = x1 * x2;
                    //+
                    ab.addAndGet(oneOfTheDimension);
                }
                if (x1 != null) {
                    //(x1)^2
                    float oneOfTheDimension = x1 * x1;
                    //+
                    aa.addAndGet(oneOfTheDimension);
                }
                if (x2 != null) {
                    //(x2)^2
                    float oneOfTheDimension = x2 * x2;
                    //+
                    bb.addAndGet(oneOfTheDimension);
                }
            });
        //|a|
        double aaa = Math.sqrt(aa.doubleValue());
        //|b|
        double bbb = Math.sqrt(bb.doubleValue());
        //使用BigDecimal保证精确计算浮点数
        //|a|*|b|
        //double aabb = aaa * bbb;
        BigDecimal aabb = BigDecimal.valueOf(aaa).multiply(BigDecimal.valueOf(bbb));
        //similarity=a.b/|a|*|b|
        //double cos = ab.get() / aabb.doubleValue();
        double cos = BigDecimal.valueOf(ab.get()).divide(aabb, 9, BigDecimal.ROUND_HALF_UP).doubleValue();
        return cos;
    }

    public static void main(String[] args) {
        String text1 = "2019年10月10号凌晨2点半，涟水交警接警后，发现现场除了受害者李梅上官风独孤信，身份证号500102198702014291，电话18600213245，固定电话02387898765，家住：大渡口区建胜镇龙桥花苑小区2栋7-3的血迹、一块机动车号牌苏A F001U和苏AF001U少量玻璃碎片外，没有其他有价值的线索。大渡口区经信委，重庆市秋田齿轮有限公司，令人悲痛的是，第二天上午10点，伤者也因伤势过重经抢救无效身亡。";
        String text2 = "2019年10月10号凌晨2点半，涟水交警接警后，发现现场除了受害者李梅上官风独孤信，家住：大渡口区建胜镇龙桥花苑小区2栋7-3的血迹、一块机动车号牌苏A F001U和苏AF001U少量玻璃碎片外，没有其他有价值的线索。大渡口区经信委，重庆市秋田齿轮有限公司，伤者也因伤势过重经抢救无效身亡。";
        String text3 = "2019年10月10号凌晨2点半，涟水交警接警后，发现现场除了受害者李梅上官风独孤信，身份证号500102198702014291，电话18600213245，固定电话02387898765，家住：大渡口区建胜镇龙桥花苑小区2栋7-3的血迹、没有其他有价值的线索。大渡口区经信委，重庆市秋田齿轮有限公司，令人悲痛的是，第二天上午10点，伤者也因伤势过重经抢救无效身亡。";
        TextSimilarity textSimilarity = new CosineTextSimilarity();
        // double score1pk1 = textSimilarity.similarScore(text1, text1);
        double score1pk2 = textSimilarity.similarScore(text1, text2);
        double score1pk3 = textSimilarity.similarScore(text1, text3);
        double score2pk2 = textSimilarity.similarScore(text2, text2);
        // double score2pk3 = textSimilarity.similarScore(text2, text3);
        // double score3pk3 = textSimilarity.similarScore(text3, text3);
        // System.out.println(text1 + " 和 " + text1 + " 的相似度分值：" + score1pk1);
        System.out.println(text1 + " 和 " + text2 + " 的相似度分值：" + score1pk2);
        System.out.println(text1 + " 和 " + text3 + " 的相似度分值：" + score1pk3);
        System.out.println(text2 + " 和 " + text2 + " 的相似度分值：" + score2pk2);
        // System.out.println(text2 + " 和 " + text3 + " 的相似度分值：" + score2pk3);
        // System.out.println(text3 + " 和 " + text3 + " 的相似度分值：" + score3pk3);
    }
}
