package com.library.service.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class BookUniqueCodeGenerator {

    Transliterator transliterator;

    public String generateBookUniqueCode(String bookTitle){
        String transliterate = transliterator.transliterate(bookTitle);
        String uuid = UUID.randomUUID().toString().substring(0,6);
        return transliterate+"-"+uuid;
    }

}
