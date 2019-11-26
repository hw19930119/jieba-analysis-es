/*
 * @(#) ChineseWordNoOpTokenFilterFactory
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-22 14:12:40
 *
 */

package com.huaban.analysis.jieba.elasticsearch;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

/**
 * 分词结果已做处理，不需要过滤器链
 */
public class JiebaWordNoOpTokenFilterFactory extends AbstractTokenFilterFactory {

    public JiebaWordNoOpTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return tokenStream;
    }
}
