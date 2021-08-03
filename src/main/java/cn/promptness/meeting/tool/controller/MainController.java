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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class MainController {

    @FXML
    public TabPane tabPane;
    @Resource
    private SpringFxmlLoader springFxmlLoader;

    private final Map<Integer, TaskController> taskMap = new ConcurrentHashMap<>();

    public void initialize() {
        add();
    }

    public void add() {
        FXMLLoader loader = springFxmlLoader.getLoader("/fxml/task.fxml");
        Parent load = springFxmlLoader.load(loader);
        TaskController taskController = loader.getController();
        taskMap.put(taskController.getTarget(), taskController);
        Tab tab = this.buildTab(load, taskController);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        tabPane.setTabClosingPolicy(tabPane.getTabs().size() > 1 ? TabPane.TabClosingPolicy.SELECTED_TAB : TabPane.TabClosingPolicy.UNAVAILABLE);
    }

    private Tab buildTab(Parent load, TaskController taskController) {
        Tab tab = new Tab("执行面板", load);
        tab.setId(String.valueOf(taskController.getTarget()));
        tab.setOnCloseRequest(event -> {
            if (!taskController.close()) {
                event.consume();
                return;
            }
            taskMap.remove(taskController.getTarget());
        });
        tab.setOnClosed(event -> tabPane.setTabClosingPolicy(tabPane.getTabs().size() == 1 ? TabPane.TabClosingPolicy.UNAVAILABLE : TabPane.TabClosingPolicy.SELECTED_TAB));
        return tab;
    }

    @EventListener(value = TaskEvent.class)
    public void stopTask(TaskEvent taskEvent) {
        taskMap.get(taskEvent.getTarget()).stopTask(taskEvent.getResult());
    }

    public TaskController getCurrentTask() {
        return taskMap.get(Integer.valueOf(tabPane.getSelectionModel().getSelectedItem().getId()));
    }
}
