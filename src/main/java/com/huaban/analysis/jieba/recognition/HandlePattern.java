/*
 * @(#) HandlePattern
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-28 09:10:58
 *
 */

package com.huaban.analysis.jieba.recognition;

import com.huaban.analysis.jieba.SegToken;

import java.util.ArrayList;
import java.util.List;

/**
 * 存储各种正则表达式匹配到的结果
 */
public class HandlePattern {

    public String src;
    public String target;
    public List<SegToken> entities = new ArrayList<SegToken>();

    public HandlePattern(String src, String target) {
        this.src = src;
        this.target = target;
    }

    public void addEntity(SegToken token) {
        this.entities.add(token);
    }

    public void addEntityAll(List<SegToken> tokens) {
        this.entities.addAll(tokens);
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
