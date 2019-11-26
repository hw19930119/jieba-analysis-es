/*
 * @(#) TextSimilarity
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-20 20:27:43
 *
 */

package com.huaban.analysis.jieba.similarity;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.huaban.analysis.jieba.WordDictionary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 文本相似度
 * @author chenhao
 */
public abstract class TextSimilarity implements Similarity, SimilarityRanker {

    protected JiebaSegmenter segmenter = new JiebaSegmenter();
    protected WordDictionary dict = WordDictionary.getInstance();

    /**
     * 文本1和文本2的相似度分值
     * @param text1 文本1
     * @param text2 文本2
     * @return 相似度分值
     */
    @Override
    public double similarScore(String text1, String text2) {

        if (text1 == null || text2 == null) {
            //只要有一个文本为null，规定相似度分值为0，表示完全不相等
            return 0.0;
        }
        //分词
        List<Word> words1 = seg(text1);
        List<Word> words2 = seg(text2);
        //计算相似度分值
        return similarScore(words1, words2);
    }

    /**
     * 词列表1和词列表2的相似度分值
     * @param words1 词列表1
     * @param words2 词列表2
     * @return 相似度分值
     */
    @Override
    public double similarScore(List<Word> words1, List<Word> words2) {
        if (words1 == null || words2 == null) {
            //只要有一个文本为null，规定相似度分值为0，表示完全不相等
            return 0.0;
        }
        if (words1.isEmpty() && words2.isEmpty()) {
            //如果两个文本都为空，规定相似度分值为1，表示完全相等
            return 1.0;
        }
        if (words1.isEmpty() || words2.isEmpty()) {
            //如果一个文本为空，另一个不为空，规定相似度分值为0，表示完全不相等
            return 0.0;
        }
        double score = scoreImpl(words1, words2);
        score = (int) (score * 1000000 + 0.5) / (double) 1000000;
        return score;
    }

    /**
     * 计算相似度分值
     * @param words1 词列表1
     * @param words2 词列表2
     * @return 相似度分值
     */
    protected abstract double scoreImpl(List<Word> words1, List<Word> words2);

    /**
     * 对文本进行分词
     * @param text 文本
     * @return 分词结果
     */
    private List<Word> seg(String text) {
        Predicate<SegToken> lengthRule = (token) -> token.word.length() > 1;
        Predicate<SegToken> noStopword = (token) -> !dict.getStopWordsSet().contains(token.word);
        Predicate<SegToken> andCondition = lengthRule.and(noStopword);

        List<SegToken> tokens = segmenter.processWithFilterRule(text, JiebaSegmenter.SegMode.SEARCH, andCondition);
        List<Word> words = tokens.stream().map(key -> new Word(key.word)).collect(Collectors.toList());
        return words;
    }

    /**
     * 如果没有指定权重，则默认使用词频来标注词的权重
     * 词频数据怎么来？
     * 一个词在词列表1中出现了几次，它在词列表1中的权重就是几
     * 一个词在词列表2中出现了几次，它在词列表2中的权重就是几
     * 标注好的权重存储在Word类的weight字段中
     * @param words1 词列表1
     * @param words2 词列表2
     */
    protected void taggingWeightWithWordFrequency(List<Word> words1, List<Word> words2) {
        // if (words1.get(0).getWeight() != null || words2.get(0).getWeight() != null) {
        //     System.out.println("词已经被指定权重，不再使用词频进行标注");
        //     return;
        // }
        //词频统计
        Map<String, AtomicInteger> frequency1 = frequency(words1);
        Map<String, AtomicInteger> frequency2 = frequency(words2);
        //输出词频统计信息
        System.out.println("词频统计1：" + formatWordsFrequency(frequency1));
        System.out.println("词频统计2：" + formatWordsFrequency(frequency2));
        //权重标注
        words1.stream().forEach(word -> {
            if (word.getWeight() == null) { //词已经被指定权重，不再使用词频进行标注
                word.setWeight(frequency1.get(word.getText()).floatValue());
            }
        });
        words2.stream().forEach(word -> {
            if (word.getWeight() == null) {
                word.setWeight(frequency2.get(word.getText()).floatValue());
            }
        });
    }

    /**
     * 构造权重快速搜索容器
     * @param words 词列表
     * @return Map
     */
    protected Map<String, Float> toFastSearchMap(List<Word> words) {
        Map<String, Float> weights = new ConcurrentHashMap<>();
        if (words == null) {
            return weights;
        }
        words.stream().forEach(word -> {
            if (word.getWeight() != null) {
                weights.put(word.getText(), word.getWeight());
            } else {
                System.out.println("词没有权重信息：" + word.getText());
            }
        });
        return weights;
    }

    /**
     * 统计词频
     * @param words 词列表
     * @return 词频统计结果
     */
    private Map<String, AtomicInteger> frequency(List<Word> words) {
        Map<String, AtomicInteger> frequency = new HashMap<>();
        words.forEach(word -> {
            frequency.computeIfAbsent(word.getText(), k -> new AtomicInteger()).incrementAndGet();
        });
        return frequency;
    }

    /**
     * 格式化词频统计信息
     * @param frequency 词频统计信息
     */
    private String formatWordsFrequency(Map<String, AtomicInteger> frequency) {
        StringBuilder str = new StringBuilder();
        if (frequency != null && !frequency.isEmpty()) {
            AtomicInteger c = new AtomicInteger();
            frequency
                .entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().get() - a.getValue().get())
                .forEach(e -> str.append("\t").append(c.incrementAndGet()).append("、").append(e.getKey()).append("=").append(e.getValue()).append("\n"));
        }
        str.setLength(str.length() - 1);
        return str.toString();
    }
}
