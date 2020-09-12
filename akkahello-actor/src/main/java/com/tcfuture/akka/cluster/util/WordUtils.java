package com.tcfuture.akka.cluster.util;

/**
 * @author liulv
 *
 * 单词生成工具类
 */
public class WordUtils {

    private final static int MIN_LETTERS = 3;
    private final static int MAX_LETTERS = 8;
    private final static int TEXT_DEFAULT_WORD_SIZE = 5;

    /**
     * 创建随机的单词文本
     *
     * @param wordSize 单词文本的单词数
     * @return 随机的单词的文本
     */
    public static String createText(int wordSize) {
        if(wordSize == 0) wordSize = TEXT_DEFAULT_WORD_SIZE;
        StringBuffer wordBuffer = new StringBuffer();
        for (int i = 0; i < wordSize; i++) {
            wordBuffer.append(createWord(MIN_LETTERS, MAX_LETTERS)).append(" ");
        }
        return wordBuffer.subSequence(0, wordBuffer.length() - 1).toString();
    }

    /**
     *
     * @param min 最后位数
     * @param max 最大位数
     * @return 生成随机的单词
     */
    public static String createWord(int min, int max) {
        int count = (int) (Math.random() * (max - min + 1)) + min;
        String str = "";
        for (int i = 0; i < count; i++) {
            str += (char) ((int) (Math.random() * 26) + 'a');
        }
        return str;
    }
}
