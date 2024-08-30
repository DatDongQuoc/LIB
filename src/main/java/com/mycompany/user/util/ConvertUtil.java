package com.mycompany.user.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConvertUtil {
    public static <T,R> R convertObject(T t, Function<T, R> func){
        return func.apply(t);
    }
    public static <T,R> List<R> convertListObject(List<T> list, Function<T, R> func){
        return list.stream()
                .map(func)
                .collect(Collectors.toList());
    }
}
