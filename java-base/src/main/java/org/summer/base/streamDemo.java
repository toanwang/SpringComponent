package org.summer.base;

import cn.hutool.json.JSONObject;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class streamDemo {

    public static void filter(){
        List<JSONObject> features = new LinkedList<JSONObject>();
        JSONObject a = new JSONObject().set("code", "brake").set("name", "碰撞");
        JSONObject b = new JSONObject().set("code", "acc").set("name", "加速");
        features.add(a);
        features.add(b);

        String filterCode = "brake";
        List<JSONObject> res = features.stream()
                .filter(obj -> obj.getStr("code") == filterCode)
                .collect(Collectors.toList());
        System.out.println(res);
    }

    public static void main(String[] args) {
        streamDemo.filter();
    }
}
