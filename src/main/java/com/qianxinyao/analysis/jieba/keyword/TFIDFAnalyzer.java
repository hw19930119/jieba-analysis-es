package com.qianxinyao.analysis.jieba.keyword;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Tom Qian
 * @email tomqianmaple@outlook.com
 * @github https://github.com/bluemapleman
 * @date Oct 20, 2018
 * tfidf算法原理参考：http://www.cnblogs.com/ywl925/p/3275878.html
 * 部分实现思路参考jieba分词：https://github.com/fxsjy/jieba
 */
public class TFIDFAnalyzer {
    private HashMap<String, Double> idfMap;
    private HashSet<String> stopWordsSet;
    private double idfMedian;

    private Map<String, Integer> freqMap; //词频统计

    public TFIDFAnalyzer() {
        this.idfMap = WordDictionary.getInstance().getIdfMap();
        this.stopWordsSet = WordDictionary.getInstance().getStopWordsSet();
        this.idfMedian = WordDictionary.getInstance().getIdfMedian();
    }

    /**
     * tfidf分析方法
     * @param content 需要分析的文本/文档内容
     * @param topN 需要返回的tfidf值最高的N个关键词，若超过content本身含有的词语上限数目，则默认返回全部
     * @return
     */
    public List<Keyword> analyze(String content, int topN) {
        List<Keyword> keywordList = new ArrayList<>();

        Map<String, Double> tfMap = getTF(content);
        for (String word : tfMap.keySet()) {
            // 若该词不在idf文档中，则使用平均的idf值(可能定期需要对新出现的网络词语进行纳入)
            if (idfMap.containsKey(word)) {
                keywordList.add(new Keyword(word, idfMap.get(word) * tfMap.get(word)));
            } else {
                keywordList.add(new Keyword(word, idfMedian * tfMap.get(word)));
            }
        }

        Collections.sort(keywordList);

        if (keywordList.size() > topN) {
            int num = keywordList.size() - topN;
            for (int i = 0; i < num; i++) {
                keywordList.remove(topN);
            }
        }
        return keywordList;
    }

    /**
     * tf值计算公式
     * tf=N(i,j)/(sum(N(k,j) for all k))
     * N(i,j)表示词语Ni在该文档d（content）中出现的频率，sum(N(k,j))代表所有词语在文档d中出现的频率之和
     * @param content
     * @return
     */
    private Map<String, Double> getTF(String content) {
        Map<String, Double> tfMap = new HashMap<>();
        if (content == null || content.equals("")) {
            return tfMap;
        }

        JiebaSegmenter segmenter = new JiebaSegmenter();
        List<String> segments = segmenter.process(content, JiebaSegmenter.SegMode.SEARCH).stream().map(seg -> seg.word).collect(Collectors.toList());
        Map<String, Integer> freqMap = new HashMap<>();

        int wordSum = 0;
        for (String segment : segments) {
            //停用词不予考虑，单字词不予考虑
            if (segment.length() > 1 && !stopWordsSet.contains(segment)) {
                wordSum++;
                if (freqMap.containsKey(segment)) {
                    freqMap.put(segment, freqMap.get(segment) + 1);
                } else {
                    freqMap.put(segment, 1);
                }
            }
        }
        this.freqMap = freqMap;
        // 计算double型的tf值
        for (String word : freqMap.keySet()) {
            tfMap.put(word, freqMap.get(word) * 0.1 / wordSum);
        }
        return tfMap;
    }

    public Map<String, Integer> getWordFreqMap() {
        return freqMap;
    }

    public static void main(String[] args) {
        String content = "孩子上了幼儿园 安全防拐教育要做好";
        int topN = 5;
        TFIDFAnalyzer tfidfAnalyzer = new TFIDFAnalyzer();
        List<Keyword> list = tfidfAnalyzer.analyze(content, topN);
        for (Keyword word : list) {
            System.out.print(word.getName() + ":" + word.getTfidfvalue() + ",");
        }
    }
}

