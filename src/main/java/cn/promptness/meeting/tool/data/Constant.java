package cn.promptness.meeting.tool.data;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Constant {

    public static final String TITLE = "会议室助手";

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36";

    public static final Map<String, String> ROOM_INFO_MAP = new LinkedHashMap<>();

    public static final Map<String, String> CRON_MAP = new LinkedHashMap<>();

    public static final List<String> TIME_LIST;

    static {
        ROOM_INFO_MAP.put("26", "18F 乐信大学");
        ROOM_INFO_MAP.put("59", "18F 洛杉矶·威尔谢大道");
        ROOM_INFO_MAP.put("31", "20F 芝加哥·拉歇尔大街");
        ROOM_INFO_MAP.put("35", "20F 波士顿·金融中心");
        ROOM_INFO_MAP.put("79", "20F 慕尼黑·考芬格大街");
        ROOM_INFO_MAP.put("80", "20F 曼彻斯特·国王大街");
        ROOM_INFO_MAP.put("81", "20F 圣保罗·保利斯塔大道");
        ROOM_INFO_MAP.put("58", "21F 布鲁塞尔·上城");
        ROOM_INFO_MAP.put("7", "23F 巴黎·拉德芳斯");
        ROOM_INFO_MAP.put("8", "23F 上海·陆家嘴");
        ROOM_INFO_MAP.put("32", "23F 东京·新宿");
        ROOM_INFO_MAP.put("27", "24F 法兰克福.银行区");
        ROOM_INFO_MAP.put("28", "24F 首尔·明洞");
        ROOM_INFO_MAP.put("15", "25F 悉尼·乔治街");
        ROOM_INFO_MAP.put("29", "25F 迪拜·国际金融中心");
        ROOM_INFO_MAP.put("45", "25F 新加坡·金融区");
        ROOM_INFO_MAP.put("60", "25F 蒙特利尔·圣雅克街");
        ROOM_INFO_MAP.put("61", "25F 纽约·华尔街");
        ROOM_INFO_MAP.put("17", "26F 苏黎世·班霍夫大街");
        ROOM_INFO_MAP.put("19", "26F 北京·金融街");
        ROOM_INFO_MAP.put("47", "26F 卢森堡·皇家大道");
    }

    static {
        CRON_MAP.put("工作日1天/次", "0 0 0 ? * 1-5");
        CRON_MAP.put("每周一1天/次", "0 0 0 ? * 1");
        CRON_MAP.put("每周二1天/次", "0 0 0 ? * 2");
        CRON_MAP.put("每周三1天/次", "0 0 0 ? * 3");
        CRON_MAP.put("每周四1天/次", "0 0 0 ? * 4");
        CRON_MAP.put("每周五1天/次", "0 0 0 ? * 5");

        CRON_MAP.put("工作日10分/次", "0 0/10 * ? * 1-5");
        CRON_MAP.put("每周一10分/次", "0 0/10 * ? * 1");
        CRON_MAP.put("每周二10分/次", "0 0/10 * ? * 2");
        CRON_MAP.put("每周三10分/次", "0 0/10 * ? * 3");
        CRON_MAP.put("每周四10分/次", "0 0/10 * ? * 4");
        CRON_MAP.put("每周五10分/次", "0 0/10 * ? * 5");

        CRON_MAP.put("工作日3分/次", "0 0/3 * ? * 1-5");
        CRON_MAP.put("每周一3分/次", "0 0/3 * ? * 1");
        CRON_MAP.put("每周二3分/次", "0 0/3 * ? * 2");
        CRON_MAP.put("每周三3分/次", "0 0/3 * ? * 3");
        CRON_MAP.put("每周四3分/次", "0 0/3 * ? * 4");
        CRON_MAP.put("每周五3分/次", "0 0/3 * ? * 5");

        CRON_MAP.put("工作日1分/次", "0 0/1 * ? * 1-5");
        CRON_MAP.put("每周一1分/次", "0 0/1 * ? * 1");
        CRON_MAP.put("每周二1分/次", "0 0/1 * ? * 2");
        CRON_MAP.put("每周三1分/次", "0 0/1 * ? * 3");
        CRON_MAP.put("每周四1分/次", "0 0/1 * ? * 4");
        CRON_MAP.put("每周五1分/次", "0 0/1 * ? * 5");

        CRON_MAP.put("工作日3时/次", "0 0 0/3 ? * 1-5");
        CRON_MAP.put("工作日1时/次", "0 0 0/1 ? * 1-5");
        CRON_MAP.put("工作日30秒/次", "0/30 * * ? * 1-5");
        CRON_MAP.put("工作日10秒/次", "0/10 * * ? * 1-5");
    }

    static {
        TIME_LIST = Arrays.asList(
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
