/*
 * @(#) Pair
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-28 21:46:49
 *
 */

package com.huaban.analysis.jieba.cluster;

/**
 * 相似的一对文本的ID
 */
public class Pair {
    public String one;
    public String another;

    public Pair(String one, String another) {
        this.one = one;
        this.another = another;
    }
}
