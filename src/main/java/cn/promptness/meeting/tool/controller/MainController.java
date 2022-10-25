package cn.promptness.meeting.tool.controller;

import cn.promptness.meeting.tool.SpringFxmlLoader;
import cn.promptness.meeting.tool.cache.TaskCache;
import cn.promptness.meeting.tool.config.MeetingTaskProperties;
import cn.promptness.meeting.tool.pojo.TaskEvent;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.TabPaneSkin;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Controller
public class MainController {

    @FXML
    public TabPane tabPane;
    @Resource
    private SpringFxmlLoader springFxmlLoader;

    private final Map<Integer, TaskController> taskMap = new ConcurrentHashMap<>();

    @PreDestroy
    public void cache() {
        TaskCache.cache(taskMap.values().stream().map(TaskController::buildMeetingTaskProperties).collect(Collectors.toList()));
    }

    public void initialize() {
        int max = Math.max(1, TaskCache.size());
        for (int i = 0; i < max; i++) {
            addTab(false);
        }
    }

    public void addTab(boolean copy) {
        FXMLLoader loader = springFxmlLoader.getLoader("/fxml/task.fxml");
        Parent load = springFxmlLoader.load(loader);
        TaskController taskController = loader.getController();
        if (copy) {
            MeetingTaskProperties meetingTaskProperties = taskMap.get(Integer.valueOf(tabPane.getSelectionModel().getSelectedItem().getId())).buildMeetingTaskProperties();
            taskController.initTask(meetingTaskProperties);
        }
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

    @EventListener(value = Class.class)
    public void addCtrlClose() {
        // SHORTCUT在windows会处理成ctrl,在苹果上会处理成Command
        SystemTrayUtil.getPrimaryStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN),
                () -> {
                    if (tabPane.getTabs().size() > 1) {
                        TabPaneBehavior tabPaneBehavior = ((TabPaneSkin) tabPane.getSkin()).getBehavior();
                        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                        if (tabPaneBehavior.canCloseTab(selectedTab)) {
                            tabPaneBehavior.closeTab(selectedTab);
                        }
                    }
                });
    }
}
