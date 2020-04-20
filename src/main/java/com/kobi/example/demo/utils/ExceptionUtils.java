package com.kobi.example.demo.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionUtils {

    @FunctionalInterface
    public interface ExceptionalCodeBlock {
        void invoke() throws Exception;
    }

    public static void swallowException(ExceptionalCodeBlock codeBlock, String error) {
        try {
            codeBlock.invoke();
        } catch ( Exception e ) {
            log.error(error, e);
        }
    }

}
