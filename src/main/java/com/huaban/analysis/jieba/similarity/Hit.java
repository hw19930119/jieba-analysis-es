/*
 * @(#) Hit
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-20 21:24:22
 *
 */

package com.huaban.analysis.jieba.similarity;

/**
 * 相似度排名结果
 * @author chenhao
 */
public class Hit implements Comparable {
    private String text;
    private Double score;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public int compareTo(Object o) {
        return ((Hit) o).getScore().compareTo(score);
    }

    @Override
    public String toString() {
        return score + " " + text;
    }
}
