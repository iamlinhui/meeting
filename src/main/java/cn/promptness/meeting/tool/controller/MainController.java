package cn.promptness.meeting.tool.controller;

import cn.promptness.meeting.tool.SpringFxmlLoader;
import cn.promptness.meeting.tool.pojo.TaskEvent;
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

    private final Map<Integer, TaskController> taskMap = new ConcurrentHashMap<>();

    public void initialize() throws IOException {
        add();
    }

    public void add() throws IOException {
        FXMLLoader loader = springFxmlLoader.getLoader("/fxml/task.fxml");
        Parent load = loader.load();
        TaskController taskController = loader.getController();
        taskMap.put(taskController.getTarget(), taskController);
        Tab tab = new Tab("执行面板", load);
        tab.setId(String.valueOf(taskController.getTarget()));
        tab.setOnCloseRequest(event -> {
            boolean close = taskController.close();
            if (!close) {
                event.consume();
            }
            taskMap.remove(taskController.getTarget());
        });
        tabPane.getTabs().add(tab);

    }

    @EventListener(value = TaskEvent.class)
    public void stopTask(TaskEvent taskEvent) {
        taskMap.get(taskEvent.getTarget()).stopTask(taskEvent.getResult());
    }

    public TaskController getCurrentTask() {
        return taskMap.get(Integer.valueOf(tabPane.getSelectionModel().getSelectedItem().getId()));
    }
}
