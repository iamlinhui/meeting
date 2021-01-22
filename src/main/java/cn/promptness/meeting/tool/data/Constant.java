package cn.promptness.meeting.tool.data;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Constant {

    public static final String TITLE = "会议室助手";

    public static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36";

    public static final Map<String, String> ROOM_INFO_LIST = new LinkedHashMap<>();

    public static final Map<String, String> CRON_LIST = new LinkedHashMap<>();

    public static final List<String> ITEMS;

    static {
        String property = System.getProperty("os.name").toLowerCase();
        if (property.contains("mac")) {
            USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36";
        }
        if (property.contains("windows 10")) {
            USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36";
        }
        if (property.contains("linux")) {
            USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36";
        }
    }

    static {
        ROOM_INFO_LIST.put("26", "18F 乐信大学");
        ROOM_INFO_LIST.put("59", "18F 洛杉矶·威尔谢大道");
        ROOM_INFO_LIST.put("35", "20F 波士顿·金融中心");
        ROOM_INFO_LIST.put("79", "20F 慕尼黑·考芬格大街");
        ROOM_INFO_LIST.put("80", "20F 曼彻斯特·国王大街");
        ROOM_INFO_LIST.put("81", "20F 圣保罗·保利斯塔大道");
        ROOM_INFO_LIST.put("58", "21F 布鲁塞尔·上城");
        ROOM_INFO_LIST.put("7", "23F 巴黎·拉德芳斯");
        ROOM_INFO_LIST.put("8", "23F 上海·陆家嘴");
        ROOM_INFO_LIST.put("32", "23F 东京·新宿");
        ROOM_INFO_LIST.put("27", "24F 法兰克福.银行区");
        ROOM_INFO_LIST.put("28", "24F 首尔·明洞");
        ROOM_INFO_LIST.put("15", "25F 悉尼·乔治街");
        ROOM_INFO_LIST.put("29", "25F 迪拜·国际金融中心");
        ROOM_INFO_LIST.put("45", "25F 新加坡·金融区");
        ROOM_INFO_LIST.put("60", "25F 蒙特利尔·圣雅克街");
        ROOM_INFO_LIST.put("61", "25F 纽约·华尔街");
        ROOM_INFO_LIST.put("17", "26F 苏黎世·班霍夫大街");
        ROOM_INFO_LIST.put("19", "26F 北京·金融街");
        ROOM_INFO_LIST.put("47", "26F 卢森堡·皇家大道");
    }

    static {
        CRON_LIST.put("每周一1天/次", "0 0 0 ? * 1");
        CRON_LIST.put("每周二1天/次", "0 0 0 ? * 2");
        CRON_LIST.put("每周三1天/次", "0 0 0 ? * 3");
        CRON_LIST.put("每周四1天/次", "0 0 0 ? * 4");
        CRON_LIST.put("每周五1天/次", "0 0 0 ? * 5");
        CRON_LIST.put("工作日1天/次", "0 0 0 ? * 1-5");
        CRON_LIST.put("工作日3时/次", "0 0 0/3 ? * 1-5");
        CRON_LIST.put("工作日1时/次", "0 0 0/1 ? * 1-5");
        CRON_LIST.put("工作日10分/次", "0 0/10 * ? * 1-5");
        CRON_LIST.put("工作日3分/次", "0 0/3 * ? * 1-5");
        CRON_LIST.put("工作日1分/次", "0 0/1 * ? * 1-5");
        CRON_LIST.put("工作日30秒/次", "0/30 * * ? * 1-5");
        CRON_LIST.put("工作日10秒/次", "0/10 * * ? * 1-5");
    }

    static {
        ITEMS = Arrays.asList(
                "09:00",
                "09:30",
                "10:00",
                "10:30",
                "11:00",
                "11:30",
                "12:00",
                "12:30",
                "13:00",
                "13:30",
                "14:00",
                "14:30",
                "15:00",
                "15:30",
                "16:00",
                "16:30",
                "17:00",
                "17:30",
                "18:00",
                "18:30",
                "19:00",
                "19:30",
                "20:00",
                "20:30",
                "21:00"
        );
    }
}
