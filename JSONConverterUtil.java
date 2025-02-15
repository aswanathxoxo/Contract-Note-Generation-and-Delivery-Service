package com.gtl.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;

public class JSONConverterUtil {

    private static final Gson gson = new GsonBuilder().serializeNulls().create();

    public static Map<String,Object> jsonStringToMapConverter(String jsonString) {
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        return gson.fromJson(jsonString, type);
    }

}
