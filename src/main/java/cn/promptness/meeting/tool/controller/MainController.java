package cn.promptness.meeting.tool.controller;

import cn.promptness.meeting.tool.MySystemTray;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.service.ValidateUserService;
import cn.promptness.meeting.tool.task.MeetingTask;
import cn.promptness.meeting.tool.task.MeetingTaskProperties;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledFuture;

@Controller
public class MainController {

    @Resource
    private TaskScheduler taskScheduler;
    @Resource
    private ConfigurableApplicationContext applicationContext;
    @FXML
    private BorderPane pane;
    @FXML
    private Button okButton;

    private final ArrayBlockingQueue<ScheduledFuture<?>> taskFutures = new ArrayBlockingQueue<>(1);
    private final ChoiceBox<Integer> plusDays = new ChoiceBox<>(FXCollections.observableArrayList(7, 6, 5, 4, 3, 2, 1, 0));
    private final ChoiceBox<String> startTime = new ChoiceBox<>(FXCollections.observableArrayList(Constant.ITEMS));
    private final ChoiceBox<String> endTime = new ChoiceBox<>(FXCollections.observableArrayList(Constant.ITEMS));
    private final ChoiceBox<String> cronDescription = new ChoiceBox<>(FXCollections.observableArrayList(Constant.CRON_LIST.keySet()));
    private final ArrayList<String> roomIdList = new ArrayList<>();
    private final ArrayList<CheckBox> checkBoxList = new ArrayList<>();
    private final boolean[] flag = {false, false, false, false};

    public void initialize() {

        plusDays.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            flag[0] = newValue != null && newValue >= 0 && newValue <= 7;
            checkSubmit();
        });

        startTime.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            extracted();
            checkSubmit();
        });
        endTime.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            extracted();
            checkSubmit();
        });

        cronDescription.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String value = Constant.CRON_LIST.get(newValue);
            flag[2] = CronSequenceGenerator.isValidExpression(value);
            checkSubmit();
        });

        for (Map.Entry<String, String> entry : Constant.ROOM_INFO_LIST.entrySet()) {
            CheckBox checkBox = new CheckBox();
            checkBox.setText(entry.getValue());
            checkBox.setId(entry.getKey());
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                roomIdList.remove(checkBox.getId());
                if (checkBox.isSelected()) {
                    roomIdList.add(checkBox.getId());
                }
                flag[3] = !CollectionUtils.isEmpty(roomIdList);
                checkSubmit();
            });
            checkBoxList.add(checkBox);
        }
        pane.setCenter(buildGridPane());
    }

    @FXML
    public void submit() {
        // 确认信息
        MeetingTaskProperties meetingTaskProperties = new MeetingTaskProperties(plusDays.getValue(), startTime.getValue(), endTime.getValue(), roomIdList, cronDescription.getValue());
        if (taskFutures.isEmpty()) {
            if (alertStart(meetingTaskProperties)) {
                applicationContext.getBean(ValidateUserService.class).start();
                startTask(meetingTaskProperties);
                MySystemTray.getTrayIcon().displayMessage("会议室助手", "开启成功", TrayIcon.MessageType.INFO);
            }
        } else {
            if (alertStop(meetingTaskProperties)) {
                stopTask();
                MySystemTray.getTrayIcon().displayMessage("会议室助手", "暂停成功", TrayIcon.MessageType.INFO);
            }
        }
    }

    private boolean alertStart(MeetingTaskProperties meetingTaskProperties) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("开启会议室助手");
        alert.setHeaderText("配置信息");
        alert.setContentText(meetingTaskProperties.toString() + meetingTaskProperties.mockCron());

        alert.initOwner(MySystemTray.getPrimaryStage());
        ButtonType buttonType = alert.showAndWait().orElse(null);
        return Objects.equals(ButtonType.OK, buttonType);
    }

    private boolean alertStop(MeetingTaskProperties meetingTaskProperties) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("暂停会议室助手");
        alert.setHeaderText("确定?");
        alert.setContentText(meetingTaskProperties.toString() + meetingTaskProperties.mockCron());
        alert.initOwner(MySystemTray.getPrimaryStage());
        ButtonType buttonType = alert.showAndWait().orElse(null);
        return Objects.equals(ButtonType.OK, buttonType);
    }

    private void extracted() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        try {
            if (StringUtils.isEmpty(startTime.getValue()) || StringUtils.isEmpty(endTime.getValue())) {
                flag[1] = false;
            } else {
                Date parseStartTime = simpleDateFormat.parse(startTime.getValue());
                Date parseEndTime = simpleDateFormat.parse(endTime.getValue());
                flag[1] = parseStartTime.before(parseEndTime);
            }
        } catch (ParseException e) {
            flag[1] = false;
        }
    }


    private void stopTask() {
        if (!taskFutures.isEmpty()) {
            ScheduledFuture<?> scheduledFuture = taskFutures.poll();
            scheduledFuture.cancel(true);
            disable(false);
        }
    }

    private void startTask(MeetingTaskProperties meetingTaskProperties) {
        if (taskFutures.isEmpty()) {
            MeetingTask meetingTask = new MeetingTask(meetingTaskProperties);
            ScheduledFuture<?> schedule = taskScheduler.schedule(meetingTask, new CronTrigger(meetingTaskProperties.getCron()));
            taskFutures.add(schedule);
            disable(true);
        }
    }

    private void disable(boolean disable) {
        plusDays.setDisable(disable);
        startTime.setDisable(disable);
        endTime.setDisable(disable);
        cronDescription.setDisable(disable);
        for (CheckBox checkBox : checkBoxList) {
            checkBox.setDisable(disable);
        }
        okButton.setText(disable ? "暂停" : "开启");
    }

    private void checkSubmit() {
        boolean result = true;
        for (boolean b : flag) {
            result &= b;
        }
        okButton.setDisable(!result);
    }

    private GridPane buildGridPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 50, 20, 10));

        grid.add(new Label("偏移天数:"), 0, 0);
        grid.add(plusDays, 1, 0);

        grid.add(new Label("会议时间:"), 2, 0);
        grid.add(startTime, 3, 0);
        grid.add(new Label("~"), 4, 0);
        grid.add(endTime, 5, 0);

        grid.add(new Label("触发周期:"), 0, 1);
        grid.add(cronDescription, 1, 1);

        grid.add(new Label("预定列表:"), 0, 2);
        for (int i = 0; i < checkBoxList.size(); i++) {
            grid.add(checkBoxList.get(i), 1 + i / 10, 2 + i - (i / 10) * 10);
        }
        grid.add(okButton, 5, 2 + checkBoxList.size() - 1 - ((checkBoxList.size() - 1) / 10) * 10);
        return grid;
    }

    @FXML
    public void clear() {
        if (taskFutures.isEmpty()) {
            plusDays.setValue(null);
            startTime.setValue(null);
            endTime.setValue(null);
            cronDescription.setValue(null);
            for (CheckBox checkBox : checkBoxList) {
                checkBox.setSelected(false);
            }
            roomIdList.clear();
            okButton.setDisable(true);
            Arrays.fill(flag, false);
        } else {
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle("会议室助手");
            alert.setHeaderText("重置配置");
            alert.initOwner(MySystemTray.getPrimaryStage());
            alert.getButtonTypes().add(ButtonType.CLOSE);
            alert.setContentText("请先暂停任务!");
            alert.showAndWait();
        }
    }

    @FXML
    public void about() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("会议室助手");
        alert.setHeaderText("关于");
        alert.setContentText("Version 1.0.0\nPowered By Lynn");
        alert.initOwner(MySystemTray.getPrimaryStage());
        alert.getButtonTypes().add(ButtonType.CLOSE);
        alert.showAndWait();
    }

    @FXML
    public void instruction() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("会议室助手");
        alert.setHeaderText("使用说明");
        alert.setContentText("1.保持Chrome浏览器中http://oa.fenqile.com/地址能登录成功\n2.会议室的勾选顺序决定预定的顺序\n3.每次执行中按预约的顺序成功预定一间即结束");
        alert.initOwner(MySystemTray.getPrimaryStage());
        alert.getButtonTypes().add(ButtonType.CLOSE);
        alert.showAndWait();
    }

    @FXML
    public void show() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("会议室助手");
        alert.setHeaderText("运行状态");
        alert.initOwner(MySystemTray.getPrimaryStage());
        alert.getButtonTypes().add(ButtonType.CLOSE);
        if (taskFutures.isEmpty()) {
            alert.setContentText("请先开启任务!");
        } else {
            MeetingTaskProperties meetingTaskProperties = new MeetingTaskProperties(plusDays.getValue(), startTime.getValue(), endTime.getValue(), roomIdList, cronDescription.getValue());
            alert.setContentText(meetingTaskProperties.toString() + meetingTaskProperties.mockCron());
        }
        alert.showAndWait();
    }
}
