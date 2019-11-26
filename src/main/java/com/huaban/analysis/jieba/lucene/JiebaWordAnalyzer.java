/*
 * @(#) ChineseWordAnalyzer
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-22 14:13:03
 *
 */

package com.huaban.analysis.jieba.lucene;

import com.huaban.analysis.jieba.JiebaSegmenter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

/**
 * Lucene中文分析器
 * @author chenhao
 */
public class JiebaWordAnalyzer extends Analyzer {
    private JiebaSegmenter segmenter = new JiebaSegmenter();

    public JiebaWordAnalyzer() {

    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new JiebaWordTokenizer(segmenter);
        return new TokenStreamComponents(tokenizer);
    }

    public static void main(String args[]) throws IOException {
        Analyzer analyzer = new JiebaWordAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream("text", "这个把手该换了，我不喜欢日本和服，别把手放在我的肩膀上，工信处女干事孙悟空每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作");
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
            OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
            PositionIncrementAttribute positionIncrementAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
            // System.out.println(charTermAttribute.toString() + " (" + offsetAttribute.startOffset() + " - " + offsetAttribute.endOffset() + ") " + positionIncrementAttribute.getPositionIncrement());
            System.out.print("[" + charTermAttribute.toString() + " ," + offsetAttribute.startOffset() + ", " + offsetAttribute.endOffset() + "] ,");

        }
        tokenStream.close();
    }
}