/*
 * @(#) ChineseWordTokenizer
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
import com.huaban.analysis.jieba.SegToken;
import com.huaban.analysis.jieba.WordDictionary;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.function.Predicate;

/**
 * Lucene中文分词器，版本7.7
 * Lucene组件概念介绍https://blog.csdn.net/dingzfang/article/details/42742171
 * @author chenhao
 */
public class JiebaWordTokenizer extends Tokenizer {

    private final CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAttribute = addAttribute(OffsetAttribute.class);
    private final PositionIncrementAttribute positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);

    private JiebaSegmenter segmentation = null;
    private BufferedReader reader = null;
    private final Queue<SegToken> words = new LinkedList<>();
    private int startOffset = 0;

    public JiebaWordTokenizer() {
        super();
        segmentation = new JiebaSegmenter();
    }

    public JiebaWordTokenizer(JiebaSegmenter segmentation) {
        super();
        this.segmentation = segmentation;
    }

    private SegToken getToken() throws IOException {
        SegToken word = words.poll();
        if (word == null) { //第一次读取token
            if (reader == null) {
                reader = new BufferedReader(input);
            }
            String line;
            StringBuilder intSB = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                intSB.append(line);
            }
            Predicate<SegToken> lengthRule = (token) -> token.word.length() > 1;
            Predicate<SegToken> noStopword = (token) -> !WordDictionary.getInstance().getStopWordsSet().contains(token.word);
            Predicate<SegToken> andCondition = lengthRule.and(noStopword);
            List wordList = segmentation.processWithFilterRule(intSB.toString(), JiebaSegmenter.SegMode.SEARCH, andCondition);
            words.addAll(wordList);

            System.out.print(String.format(Locale.getDefault(), "\n%s %s\n%s",
                LocalDateTime.now(), intSB.toString(), wordList.toString()));

            startOffset = 0;
            word = words.poll();
        }
        return word;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        //清除所有的词元属性
        clearAttributes();
        SegToken token = getToken();
        if (token != null) {
            offsetAttribute.setOffset(token.startOffset, token.endOffset);
            positionIncrementAttribute.setPositionIncrement(1);

            charTermAttribute.setEmpty().append(token.word);
            return true;
        }
        return false;
    }
}