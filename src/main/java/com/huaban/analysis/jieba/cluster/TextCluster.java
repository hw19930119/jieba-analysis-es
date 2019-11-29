/*
 * @(#) TextCluster
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-28 21:46:55
 *
 */

package com.huaban.analysis.jieba.cluster;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 文本聚合类
 */
public class TextCluster {

    /**
     * 新的一组聚类，List中的一个节点表示一个聚类，可从结果中筛选出有指定数量的一个SET
     * 聚合中的抽样数据，从数据库中选择提交人重复提交最多的一条
     * @param cluster 原始的一组聚类
     * @param p1 匹配对象
     * @return 一组聚类
     */
    public static List<Set<String>> cluster(List<Set<String>> cluster, Pair p1) {

        boolean found = false;
        for (Set<String> c1 : cluster) {
            if (c1.contains(p1.one) || c1.contains(p1.another)) {
                c1.add(p1.one);
                c1.add(p1.another);
                found = true;
                break;
            }
        }
        if (!found) {
            Set<String> c2 = new LinkedHashSet<>();
            c2.add(p1.one);
            c2.add(p1.another);
            cluster.add(c2);
        }
        return cluster;
    }


    public static void main(String[] args) {
        Pair p1 = new Pair("1", "2");
        Pair p2 = new Pair("1", "3");
        Pair p3 = new Pair("2", "3");
        Pair p4 = new Pair("4", "5");
        Pair p5 = new Pair("5", "6");

        List<Pair> plist = new ArrayList<>();
        plist.add(p1);
        plist.add(p2);
        plist.add(p3);
        plist.add(p4);
        plist.add(p5);
        List<Set<String>> cluster = new ArrayList<>();
        for (Pair p : plist) {
            cluster = cluster(cluster, p);
        }
    }
}
