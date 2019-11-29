/*
 * @(#) JiebaSegmenterTest11
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-15 11:50:56
 *
 */

/**
 *
 */

package com.huaban.analysis.jieba;

import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.qianxinyao.analysis.jieba.keyword.Keyword;
import com.qianxinyao.analysis.jieba.keyword.TFIDFAnalyzer;

import junit.framework.TestCase;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;


/**
 * @author matrix
 *
 */
public class JiebaSegmenterTest11 extends TestCase {
    private JiebaSegmenter segmenter = new JiebaSegmenter();
    String[] sentences =
        new String[]{
            "这个把手该换了，我不喜欢日本和服，别把手放在我的肩膀上，######工信处女干事孙悟空每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作", "一审裁定见习律师兼职律师27日，全国人大常委会第三次审议侵权责任法草案，删除了有关医疗损害责任“举证倒置”的规定。在医患纠纷中本已处于弱势地位的消费者由此将陷入万劫不复的境地", "11月13日下午，记者在建胜镇龙桥花苑小区门口看到，流动摊贩不见了，路面干净整洁，赢得了过往群众的交口称赞。但在整治之前，这里的环境却让居民颇有怨言。 \n" +
            "\n" +
            "据了解，2016年业主李梅，身份证号：500102198802012212，电话：18600213245，固定电话：02387898765，家住：建胜镇龙桥花苑小区12栋27-2，陆续入住龙桥花苑小区，随着入住人员增多，一些小商小贩瞄准商机，到此挤占人行道摆摊设点，售卖蔬菜、水果、猪肉等商品，形成“马路市场”，阻碍行人通行，影响了周边环境卫生。“以前，在小区门口的人行道上，曾经同时有30多个小摊贩出摊，不仅造成人行道堵塞，产生的噪声也严重影响居民休息。而且流动摊贩每天产生的大量垃圾，还影响了周边的居住环境。请街道协同区环保局、城管局和执法部门进行整改。” 家住龙桥花苑小区的刘金生说。", "11月12日，福建省明溪县人民法院公开开庭审理一起被告人多达15人的电信诈骗案件，涉案金额高达470余万元。\n" +
            "\n" +
            "　　据公诉机关指控，2018年7月，被告人邱某开、胡某伟、罗某平商量利用微信推广虚假炒外汇信息，骗取微信好友投资进行诈骗。该团伙从某技术人员手中购买用于虚假炒外汇APP程序，并利用他人身份证信息办理多张银行卡。2018年11月至2019年3月，该犯罪团伙到辽宁省多地区开展犯罪活动，被告人邱某开将该团伙分成4个窝点，分工明确开展诈骗。被告人罗某城、罗某强、邱飞某等人负责在微信群里发布信息，充当微信推广角色；被告人黄某坤等人负责为各个诈骗点送诈骗提成；被告人邱坤某等人作为客服，联系客户充钱；被告人童某涛为后台程序员，负责操纵炒汇数据；被告人廖某龙、赵某农等人负责提取诈骗款后洗钱。\n" +
            "\n" +
            "　　该诈骗团伙在微信群里宣称炒汇APP出现漏洞，该漏洞炒汇稳赚不赔，并发布截图吸引被害人投资。被害人有意投资后，通过客服聊天，引诱被害人下载APP并注册。客户投资后，由程序员在后台操作，制造被害人投资操作失误的假象，让被害人输钱，从而诈骗被害人钱财。\n" +
            "\n" +
            "　　公诉机关认为，被告人邱某开、胡某伟等犯罪团伙已触犯《中华人民共和国刑法》相关规定，犯罪事实清楚，证据确实充分，数额巨大，应当以诈骗罪追究其刑事责任。\n" +
            "\n" +
            "　　目前，该案还在进一步审理中，并将择期公开宣判。",
            "2019年10月10号凌晨2点半，大渡口区春晖路街道涟水交警接警后，发现现场除了受害者李梅上官风，身份证号500102198702014291，电话18600213245，固定电话02387898765，家住：大渡口区建胜镇龙桥花苑小区2栋7-3的血迹、一块机动车号牌苏A F001U和苏AF001U少量玻璃碎片外，没有其他有价值的线索。大渡口区经信委，重庆市秋田齿轮有限公司，令人悲痛的是，第二天上午10点，伤者也因伤势过重经抢救无效身亡。"
        };
    String[] longSentences = new String[]{
        "我他妈偏不信我他妈偏"
    };


    @Override
    protected void setUp() throws Exception {
        // WordDictionary.getInstance().init(Paths.get("conf"));
        // WordDictionary.getInstance().addWord("孙小艾");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testCutForSearch() {

        Predicate<SegToken> lengthRule = (token) -> token.word.length() > 1;
        Predicate<SegToken> noStopword = (token) -> !WordDictionary.getInstance().getStopWordsSet().contains(token.word);
        Predicate<SegToken> andCondition = lengthRule.and(noStopword);

        // for (String sentence : sentences) {
        String sentence = "2019年10月10号凌晨2点半，大渡口区春晖路街道涟水交警接警后，发现现场除了受害者李梅上官风，身份证号500102198702014291，电话18600213245，固定电话02387898765，家住：大渡口区建胜镇龙桥花苑小区2栋7-3的血迹、一块机动车号牌苏A F001U和苏AF001U少量玻璃碎片外，没有其他有价值的线索。大渡口区经信委，重庆市秋田齿轮有限公司，令人悲痛的是，第二天上午10点，伤者也因伤势过重经抢救无效身亡。";
        List<SegToken> tokens = segmenter.processWithFilterRule(sentence, SegMode.SEARCH, andCondition);
        System.out.print(String.format(Locale.getDefault(), "\n%s  %s\n%s",
            LocalDateTime.now(), sentence, tokens.toString()));
        // }

        // for (String sentence : sentences) {
        //     List<SegToken> tokens = segmenter.processWithFilterRule(sentence, SegMode.SEARCH, andCondition);
        //     System.out.print(String.format(Locale.getDefault(), "\n%s\n%s",
        //         sentence, tokens.toString()));
        // }

    }

    @Test
    public void testTfidfAnalyzer() {
        int topN = 500;
        TFIDFAnalyzer tfidfAnalyzer = new TFIDFAnalyzer();
        List<Keyword> list = tfidfAnalyzer.analyze(sentences[4], topN);
        for (Keyword word : list) {
            System.out.println(word.getName() + ":" + word.getTfidfvalue() + ",");
        }

        System.out.println("词频统计==========");
        Map<String, Integer> freqMap = tfidfAnalyzer.getWordFreqMap();
        for (Map.Entry entry : freqMap.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }


    @Test
    public void testCutForIndex() {
        System.out.println("-----");
        // for (String sentence : sentences) {
        String sentence = "2019年10月10号凌晨2点半，大渡口区春晖路街道涟水交警接警后，发现现场除了受害者李梅上官风，身份证号500102198702014291，电话18600213245，固定电话02387898765，家住：大渡口区建胜镇龙桥花苑小区2栋7-3的血迹、一块机动车号牌苏A F001U和苏AF001U少量玻璃碎片外，没有其他有价值的线索。大渡口区经信委，重庆市秋田齿轮有限公司，令人悲痛的是，第二天上午10点，伤者也因伤势过重经抢救无效身亡。";
        List<SegToken> tokens = segmenter.process(sentence, SegMode.INDEX);
        System.out.print(String.format(Locale.getDefault(), "\n%s\n%s", sentence, tokens.toString()));
        // }
    }


    @Test
    public void testBugSentence() {
        String[] bugs =
            new String[]{
                "UTF-8",
                "iphone5",
                "鲜芋仙 3",
                "RT @laoshipukong : 27日，",
                "AT&T是一件不错的公司，给你发offer了吗？",
                "干脆就把那部蒙人的闲法给废了拉倒！RT @laoshipukong : 27日，全国人大常委会第三次审议侵权责任法草案，删除了有关医疗损害责任“举证倒置”的规定。在医患纠纷中本已处于弱势地位的消费者由此将陷入万劫不复的境地。 "};
        for (String sentence : bugs) {
            List<SegToken> tokens = segmenter.process(sentence, SegMode.SEARCH);
            System.out.print(String.format(Locale.getDefault(), "\n%s\n%s", sentence, tokens.toString()));
        }
    }


}
