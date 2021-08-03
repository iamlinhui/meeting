package cn.promptness.meeting.tool.controller;

import cn.promptness.meeting.tool.SpringFxmlLoader;
import cn.promptness.meeting.tool.config.MeetingTaskProperties;
import cn.promptness.meeting.tool.pojo.TaskEvent;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class MainController {

    @FXML
    public TabPane tabPane;
    @Resource
    private SpringFxmlLoader springFxmlLoader;

    private final Map<Integer, TaskController> taskMap = new ConcurrentHashMap<>();

    public void initialize() {
        addTab();
    }

    public void addTab() {
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
        Tab tab = new Tab("控制台", load);
        tab.setId(String.valueOf(taskController.getTarget()));
        tab.setOnCloseRequest(event -> {
            if (!taskController.isRunning()) {
                taskMap.remove(taskController.getTarget());
                return;
            }
            if (alertClose(taskController.buildMeetingTaskProperties())) {
                taskController.stopTask(true);
                taskMap.remove(taskController.getTarget());
                return;
            }
            event.consume();
        });
        tab.setOnClosed(event -> tabPane.setTabClosingPolicy(tabPane.getTabs().size() == 1 ? TabPane.TabClosingPolicy.UNAVAILABLE : TabPane.TabClosingPolicy.SELECTED_TAB));
        return tab;
    }

    private boolean alertClose(MeetingTaskProperties meetingTaskProperties) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("关闭");
        alert.setHeaderText("确定?");
        alert.setContentText(meetingTaskProperties.toString());
        alert.initOwner(SystemTrayUtil.getPrimaryStage());
        ButtonType buttonType = alert.showAndWait().orElse(null);
        return Objects.equals(ButtonType.OK, buttonType);
    }

    @EventListener(value = TaskEvent.class)
    public void stopTask(TaskEvent taskEvent) {
        taskMap.get(taskEvent.getTarget()).stopTask(taskEvent.getResult());
    }

    public TaskController getCurrentTask() {
        return taskMap.get(Integer.valueOf(tabPane.getSelectionModel().getSelectedItem().getId()));
    }
}
