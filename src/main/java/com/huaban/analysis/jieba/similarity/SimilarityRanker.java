/*
 * @(#) SimilarityRanker
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-20 20:27:26
 *
 */

package com.huaban.analysis.jieba.similarity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 相似度排名
 * @author chenhao
 */
public interface SimilarityRanker extends Similarity {
    /**
     * 计算源文本和目标文本的相似度
     * 根据相似度分值对目标文本进行排序
     * @param source 源文本
     * @param targets 目标文本
     * @return 相似度排名结果列表
     */
    default Hits rank(String source, List<String> targets) {
        return rank(source, targets, Integer.MAX_VALUE);
    }
    /**
     * 计算源文本和目标文本的相似度（带id）
     * 根据相似度分值对目标文本进行排序
     * 获取排名结果最高的topN项
     * @param sourceId 原文本id
     * @param source 源文本
     * @param targets 目标文本及其id
     * @param topN 相似度排名结果列表只保留相似度分值最高的topN项
     * @return 相似度排名结果列表
     */
    default Hits rank(String sourceId,String source, List<Map<String ,String>> targets, int topN) {
        Hits hits = new Hits(topN > targets.size() ? targets.size() : topN);
        targets
            .stream()
            .map(target -> {
                double score = similarScore(source, target.get("event_content"));
                Hit hit = new Hit();
                hit.setText(target.get("event_content"));
                hit.setEventId(target.get("event_id"));
                hit.setSourceId(sourceId);
                hit.setScore(score);
                return hit;
            })
            .sorted()
            .limit(topN)
            .collect(Collectors.toList())
            .forEach(hit -> hits.addHit(hit));
        return hits;
    }
    /**
     * 计算源文本和目标文本的相似度
     * 根据相似度分值对目标文本进行排序
     * 获取排名结果最高的topN项
     * @param source 源文本
     * @param targets 目标文本
     * @param topN 相似度排名结果列表只保留相似度分值最高的topN项
     * @return 相似度排名结果列表
     */
    default Hits rank(String source, List<String> targets, int topN) {
        Hits hits = new Hits(topN > targets.size() ? targets.size() : topN);
        targets
            .stream()
            .map(target -> {
                double score = similarScore(source, target);
                Hit hit = new Hit();
                hit.setText(target);
                hit.setScore(score);
                return hit;
            })
            .sorted()
            .limit(topN)
            .collect(Collectors.toList())
            .forEach(hit -> hits.addHit(hit));
        return hits;
    }
}
