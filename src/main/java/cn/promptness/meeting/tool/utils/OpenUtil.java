package cn.promptness.meeting.tool.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OpenUtil {


    public static void open(int code) {
        // 未登录或登录超时，请重新登录
        if (90001002 == code) {
            try {
                String path = "C:/Program Files (x86)/Google/Chrome/Application/chrome.exe";
                String target = "http://oa.fenqile.com/";
                List<String> cmd = new ArrayList<>();
                cmd.add(path);
                cmd.add("--start-maximized");
                cmd.add(target);
                ProcessBuilder process = new ProcessBuilder(cmd);
                process.start();
            } catch (Exception e) {
                try {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler http://oa.fenqile.com/");
                } catch (IOException ignored) {
                }
            }
        }
    }
}
