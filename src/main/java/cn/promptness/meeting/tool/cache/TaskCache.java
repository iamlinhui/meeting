package cn.promptness.meeting.tool.cache;

import cn.promptness.meeting.tool.config.MeetingTaskProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TaskCache {

    private static final Logger log = LoggerFactory.getLogger(TaskCache.class);

    private static final String TASK_FILE = "task.dat";

    private static final List<MeetingTaskProperties> TASK_LIST = new CopyOnWriteArrayList<>();

    public static void cache(List<MeetingTaskProperties> meetingTaskPropertiesList) {
        File task = new File(TASK_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(task))) {
            for (MeetingTaskProperties meetingTaskProperties : meetingTaskPropertiesList) {
                oos.writeObject(meetingTaskProperties);
            }
            oos.writeObject(null);
            oos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static void read() {
        File task = new File(TASK_FILE);
        if (!task.exists()) {
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(task))) {
            Object object;
            while ((object = ois.readObject()) != null) {
                TASK_LIST.add((MeetingTaskProperties) object);
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage());
        }
    }

    public static Integer size() {
        return TASK_LIST.size();
    }

    public static MeetingTaskProperties getMeetingTaskProperties(Integer target) {
        if (target >= TASK_LIST.size()) {
            return null;
        }
        return TASK_LIST.get(target);
    }
}