package cn.promptness.meeting.tool.controller;

import cn.promptness.meeting.tool.SpringFxmlLoader;
import cn.promptness.meeting.tool.pojo.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class MainController {

    @FXML
    public TabPane tabPane;
    @Resource
    private SpringFxmlLoader springFxmlLoader;

    private final Map<Integer, TaskController> TASK_MAP = new ConcurrentHashMap<>();

    public void initialize() {
        add();
    }

    public void add() {
        try {
            FXMLLoader loader = springFxmlLoader.getLoader("/fxml/task.fxml");
            Parent load = loader.load();
            TaskController taskController = loader.getController();
            TASK_MAP.put(taskController.getTarget(), taskController);
            tabPane.getTabs().add(new Tab(String.valueOf(taskController.getTarget()), load));
        } catch (IOException ignored) {

        }
    }

    @EventListener(value = Event.class)
    public void stopTask(Event event) {
        TASK_MAP.get(event.getTarget()).stopTask(event.getResult());
    }
}
