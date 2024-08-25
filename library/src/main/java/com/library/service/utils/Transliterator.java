package com.library.service.utils;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Transliterator {

    private static final Map<Character, String> transliterationMap = new HashMap<>();

    static {
        transliterationMap.put('а', "a");
        transliterationMap.put('б', "b");
        transliterationMap.put('в', "v");
        transliterationMap.put('г', "g");
        transliterationMap.put('д', "d");
        transliterationMap.put('е', "e");
        transliterationMap.put('ё', "yo");
        transliterationMap.put('ж', "zh");
        transliterationMap.put('з', "z");
        transliterationMap.put('и', "i");
        transliterationMap.put('й', "y");
        transliterationMap.put('к', "k");
        transliterationMap.put('л', "l");
        transliterationMap.put('м', "m");
        transliterationMap.put('н', "n");
        transliterationMap.put('о', "o");
        transliterationMap.put('п', "p");
        transliterationMap.put('р', "r");
        transliterationMap.put('с', "s");
        transliterationMap.put('т', "t");
        transliterationMap.put('у', "u");
        transliterationMap.put('ф', "f");
        transliterationMap.put('х', "kh");
        transliterationMap.put('ц', "ts");
        transliterationMap.put('ч', "ch");
        transliterationMap.put('ш', "sh");
        transliterationMap.put('щ', "shch");
        transliterationMap.put('ь', "");
        transliterationMap.put('ъ', "");
        transliterationMap.put('ы', "y");
        transliterationMap.put('э', "e");
        transliterationMap.put('ю', "yu");
        transliterationMap.put('я', "ya");
        transliterationMap.put(' ',"-");
    }

    public  String transliterate(String input) {
        StringBuilder result = new StringBuilder();
        String lowerCase = input.toLowerCase();

        for (char c : lowerCase.toCharArray()) {
            String replacement = transliterationMap.get(c);
            if (replacement != null) {
                result.append(replacement);
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

}

