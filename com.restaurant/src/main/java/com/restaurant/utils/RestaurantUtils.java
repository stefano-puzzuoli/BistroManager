package com.restaurant.utils;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.bytebuddy.description.method.MethodDescription;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

public class RestaurantUtils {

    private RestaurantUtils() {

    }

    public static ResponseEntity<String> getResponseEntity(String responseMessage, HttpStatus httpStatus) {
        return new ResponseEntity<String>("{\"message\":\"" + responseMessage + "\"}", httpStatus);
    }

    public static String getUUID() {
        Date data = new Date();
        long time = data.getTime();
        return "BILL-" + time;
    }

    public static Map<String, Object> getMapFromJson(String data) {
        if (!Strings.isNullOrEmpty(data)) {
            return new Gson().fromJson(data, new TypeToken<Map<String, Object>>() {
            }.getType());
        }
        return Collections.emptyMap();
    }
}
