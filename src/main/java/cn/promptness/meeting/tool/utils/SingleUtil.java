package cn.promptness.meeting.tool.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Objects;

public class SingleUtil {

    public static String getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String pidName = runtime.getName();
        return runtime.getName().substring(0, pidName.indexOf("@"));
    }

    public static String getProgramName(String pid) throws IOException {
        try (InputStream inputStream = Runtime.getRuntime().exec("TASKLIST /NH /FO CSV /FI \"PID EQ " + pid + "\"").getInputStream()) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    // "java.exe","19376","Console","1","33,792 K"
                    return bufferedReader.readLine().split(",")[0].replace("\"", "");
                }
            }
        }
    }

    public static boolean isSingle(String programName, String pid) throws IOException {
        try (InputStream inputStream = Runtime.getRuntime().exec("TASKLIST /NH /FO CSV /FI \"IMAGENAME EQ " + programName + "\"").getInputStream()) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String currentPid = line.split(",")[1].replace("\"", "");
                        if (!Objects.equals(pid, currentPid)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
