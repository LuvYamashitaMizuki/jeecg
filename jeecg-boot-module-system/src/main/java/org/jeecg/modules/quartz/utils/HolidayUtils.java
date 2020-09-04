package org.jeecg.modules.quartz.utils;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class HolidayUtils {
    //当接口返回数组时，默认返回的是json。 https://www.kancloud.cn/xiaoggvip/holiday_free/1606802
    //但对于一天返回的还是int（对于使用的这个api来说）
    // 0为工作日，1为周末，2为节假日

    public static Integer HolidayGet(String currentDay){

        //http 获取RestTemplate实例
        RestTemplate restTemplate = new RestTemplate();
        //https 获取RestTemplate实例，需要自定义HttpsClientRequestFactory()
        //RestTemplate restTemplate = new RestTemplate(new HttpsClientRequestFactory());
        String UrlDay = "http://tool.bitefu.net/jiari/?d=" + currentDay;
        ResponseEntity<Integer> res = restTemplate.getForEntity(UrlDay,Integer.class);
        return res.getBody();
    }
}
