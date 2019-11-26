/*
 * @(#) Similarity
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-20 20:27:26
 *
 */

package com.huaban.analysis.jieba.similarity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 相似度
 * @author chenhao
 */
public interface Similarity {
    //相似性阈值
    float thresholdRate = 0.5F;

    /**
     * 对象1和对象2是否相似
     * @param object1 对象1
     * @param object2 对象2
     * @return 是否相似
     */
    default boolean isSimilar(String object1, String object2) {
        return similarScore(object1, object2) >= thresholdRate;
    }

    /**
     * 对象1和对象2的相似度分值
     * @param object1 对象1
     * @param object2 对象2
     * @return 相似度分值
     */
    double similarScore(String object1, String object2);

    /**
     * 词列表1和词列表2是否相似
     * @param words1 词列表1
     * @param words2 词列表2
     * @return 是否相似
     */
    default boolean isSimilar(List<Word> words1, List<Word> words2) {
        return similarScore(words1, words2) >= thresholdRate;
    }

    /**
     * 词列表1和词列表2的相似度分值
     * @param words1 词列表1
     * @param words2 词列表2
     * @return 相似度分值
     */
    double similarScore(List<Word> words1, List<Word> words2);


    /**
     * 词及其权重映射1和词及其权重映射2是否相似
     * @param weights1 词及其权重映射1
     * @param weights2 词及其权重映射2
     * @return 是否相似
     */
    default boolean isSimilar(HashMap<Word, Float> weights1, HashMap<Word, Float> weights2) {
        return similarScore(weights1, weights2) >= thresholdRate;
    }

    /**
     * 词及其权重映射1和词及其权重映射2的相似度分值
     * @param weights1 词及其权重映射1
     * @param weights2 词及其权重映射2
     * @return 相似度分值
     */
    default double similarScore(HashMap<Word, Float> weights1, HashMap<Word, Float> weights2) {
        List<List<Word>> words = Arrays.asList(weights1, weights2).stream().map(weights -> {
            return weights.keySet().stream()
                .map(word -> {
                    word.setWeight(weights.get(word));
                    return word;
                })
                .collect(Collectors.toList());
        }).collect(Collectors.toList());

        return similarScore(words.get(0), words.get(1));
    }


    /**
     * 词及其权重映射1和词及其权重映射2是否相似
     * @param weights1 词及其权重映射1
     * @param weights2 词及其权重映射2
     * @return 是否相似
     */
    default boolean isSimilar(Map<String, Float> weights1, Map<String, Float> weights2) {
        return similarScore(weights1, weights2) >= thresholdRate;
    }

    /**
     * 词及其权重映射1和词及其权重映射2的相似度分值
     * @param weights1 词及其权重映射1
     * @param weights2 词及其权重映射2
     * @return 相似度分值
     */
    default double similarScore(Map<String, Float> weights1, Map<String, Float> weights2) {
        List<List<Word>> words = Arrays.asList(weights1, weights2).stream().map(weights -> {
            return weights.keySet().stream()
                .map(w -> {
                    Word word = new Word(w);
                    word.setWeight(weights.get(w));
                    return word;
                })
                .collect(Collectors.toList());
        }).collect(Collectors.toList());

        return similarScore(words.get(0), words.get(1));
    }
}
