package cn.promptness.meeting.tool.cache;

import cn.promptness.meeting.tool.config.MeetingTaskProperties;

import java.io.*;

public class TaskCache {

    private static final String TASK_FILE = "task.dat";

    public static void cache(MeetingTaskProperties meetingTaskProperties) {
        File task = new File(TASK_FILE);
        if (task.exists()) {
            task.delete();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(task))) {
            oos.writeObject(meetingTaskProperties);
            oos.writeObject(null);
            oos.flush();
        } catch (Exception ignored) {

        }
    }

    public static MeetingTaskProperties read() {
        File task = new File(TASK_FILE);
        if (task.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(task))) {
                Object object;
                if ((object = ois.readObject()) != null) {
                    return (MeetingTaskProperties) object;
                }
            } catch (Exception ignored) {

            } finally {
                task.delete();
            }
        }
        return null;
    }
}
