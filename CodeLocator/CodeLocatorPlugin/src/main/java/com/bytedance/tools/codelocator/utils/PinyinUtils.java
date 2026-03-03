package com.bytedance.tools.codelocator.utils;

import com.hankcs.hanlp.dictionary.py.Pinyin;
import com.hankcs.hanlp.dictionary.py.PinyinDictionary;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PinyinUtils {


    public static Set<String> getAllPinyinStr(String str) {
        if (str == null || str.trim().isEmpty()) {
            return Collections.emptySet();
        }
        HashSet<String> resultPinyinSet = new HashSet<>();
        HashSet<String> tmpPinyinSet = new HashSet<>();
        StringBuilder resultPinyinBuilder = new StringBuilder("");
        for (int i = 0; i < str.length(); i++) {
            tmpPinyinSet.clear();
            // 获取该字所有拼音列表
            List<Pinyin> pinyinList = PinyinDictionary.convertToPinyin("" + str.charAt(i));
            if (pinyinList != null && pinyinList.size() > 0) {
                for (Pinyin py : pinyinList) {
                    tmpPinyinSet.add(py.getPinyinWithoutTone());
                }
            }
            if (tmpPinyinSet.size() > 0) {
                for (String charPinyin : tmpPinyinSet) {
                    resultPinyinBuilder.append(charPinyin);
                }
            } else {
                final String currentCharStr = String.valueOf(str.charAt(i)).toLowerCase();
                resultPinyinBuilder.append(currentCharStr);
            }
        }
        resultPinyinSet.add(resultPinyinBuilder.toString());
        return resultPinyinSet;
    }

}
