package com.huaban.analysis.jieba;

/**
 * 分词结构
 */
public class SegToken {
    public String word;

    public int startOffset;

    public int endOffset;

    public int entity = -1;

    public static final int ENTITY_CHEPAI = 0; //车牌号
    public static final int ENTITY_SFZH = 1;//身份证
    public static final int ENTITY_MOBILE = 2;//移动电话
    public static final int ENTITY_PHONE = 3;//固定电话

    public static final int ENTITY_PNAME = 4;//人名
    public static final int ENTITY_DNAME = 5;//地名
    public static final int ENTITY_GNAME = 6;//结构名
    public static final int ENTITY_ZNAME = 7;//专用名

    public SegToken(String word, int startOffset, int endOffset) {
        this.word = word;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public SegToken(String word, int startOffset, int endOffset, int entity) {
        this.word = word;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "[" + word + ", " + startOffset + ", " + endOffset + ", " + entity + "]";
    }

}
