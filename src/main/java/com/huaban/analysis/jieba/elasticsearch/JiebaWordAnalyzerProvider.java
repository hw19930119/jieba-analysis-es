/*
 * @(#) JiebaWordAnalyzerProvider
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-22 14:12:40
 *
 */

package com.huaban.analysis.jieba.elasticsearch;

import com.huaban.analysis.jieba.lucene.JiebaWordAnalyzer;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

/**
 *
 */
public class JiebaWordAnalyzerProvider extends AbstractIndexAnalyzerProvider<JiebaWordAnalyzer> {

    private final JiebaWordAnalyzer analyzer = new JiebaWordAnalyzer();

    public JiebaWordAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
    }

    @Override
    public JiebaWordAnalyzer get() {
        return this.analyzer;
    }
}