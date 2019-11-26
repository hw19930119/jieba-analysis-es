/*
 * @(#) ChineseWordPlugin
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-22 14:12:40
 *
 */

package com.huaban.analysis.jieba.elasticsearch;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;

/**
 * 中文分词组件（jieba）的ElasticSearch插件
 * @author chenhao
 */
public class JiebaWordPlugin extends Plugin implements AnalysisPlugin {

    // @Override
    // public Map<String, AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
    //     return singletonMap("jieba", JiebaWordNoOpTokenFilterFactory::new);
    // }

    @Override
    public Map<String, AnalysisProvider<TokenizerFactory>> getTokenizers() {
        Map<String, AnalysisProvider<TokenizerFactory>> extra = new HashMap<>();
        extra.put("jieba", JiebaWordTokenizerFactory::new);
        return extra;
    }

    @Override
    public Map<String, AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        return singletonMap("jieba", JiebaWordAnalyzerProvider::new);
    }
}