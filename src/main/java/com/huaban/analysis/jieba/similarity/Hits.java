/*
 * @(#) Hits
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-20 21:24:23
 *
 */

package com.huaban.analysis.jieba.similarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 相似度排名结果列表
 * @author chehao
 */
public class Hits {
    private List<Hit> hits = null;

    public Hits() {
        hits = new ArrayList<>();
    }

    public Hits(int size) {
        hits = new ArrayList<>(size);
    }

    public int size() {
        return hits.size();
    }

    public List<Hit> getHits() {
        return Collections.unmodifiableList(hits);
    }

    public void addHits(List<Hit> hits) {
        this.hits.addAll(hits);
    }

    public void addHit(Hit hit) {
        this.hits.add(hit);
    }
}
