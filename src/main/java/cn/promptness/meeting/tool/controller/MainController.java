package cn.promptness.meeting.tool.controller;

import cn.promptness.meeting.tool.MySystemTray;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.service.CancelMeetingRoomService;
import cn.promptness.meeting.tool.service.MeetingRoomService;
import cn.promptness.meeting.tool.service.ValidateUserService;
import cn.promptness.meeting.tool.task.MeetingTask;
import cn.promptness.meeting.tool.task.MeetingTaskProperties;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
    private Button okButton;
    @FXML
    public GridPane gridPane;
    @FXML
    private ChoiceBox<Integer> plusDays;
    @FXML
    private ChoiceBox<String> startTime;
    @FXML
    private ChoiceBox<String> endTime;
    @FXML
    private ChoiceBox<String> cronDescription;

    private final ArrayBlockingQueue<ScheduledFuture<?>> taskFutures = new ArrayBlockingQueue<>(1);
    private final ArrayList<String> roomIdList = new ArrayList<>();
    private final ArrayList<CheckBox> checkBoxList = new ArrayList<>();
    private final boolean[] flag = {false, false, false, false};

    public void initialize() {

        plusDays.setItems(FXCollections.observableArrayList(7, 6, 5, 4, 3, 2, 1, 0));
        startTime.setItems(FXCollections.observableArrayList(Constant.ITEMS));
        endTime.setItems(FXCollections.observableArrayList(Constant.ITEMS));
        cronDescription.setItems(FXCollections.observableArrayList(Constant.CRON_LIST.keySet()));

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

        initGridPane();
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

    private void initGridPane() {
        for (int i = 0; i < checkBoxList.size(); i++) {
            gridPane.add(checkBoxList.get(i), 1 + i / 10, 2 + i - (i / 10) * 10);
        }
        gridPane.add(okButton, 5, 2 + checkBoxList.size() - 1 - ((checkBoxList.size() - 1) / 10) * 10);
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

    @FXML
    public void exit() {
        System.exit(0);
    }

    @FXML
    public void list() {

        MeetingRoomService meetingRoomService = applicationContext.getBean(MeetingRoomService.class);
        meetingRoomService.start();

        meetingRoomService.setOnSucceeded(event -> {
            if (event.getSource().getValue() == null) {
                return;
            }
            JSONArray value = (JSONArray) event.getSource().getValue();

            ArrayList<String> cancelList = new ArrayList<>();
            ButtonType cancel = new ButtonType("取消会议室");

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("会议室助手");
            dialog.setHeaderText("成功列表");
            dialog.initOwner(MySystemTray.getPrimaryStage());
            dialog.getDialogPane().getButtonTypes().add(cancel);


            GridPane grid = new GridPane();
            grid.getColumnConstraints().addAll(new ColumnConstraints(50), new ColumnConstraints(200), new ColumnConstraints(150), new ColumnConstraints(100), new ColumnConstraints(100));
            grid.setAlignment(Pos.CENTER);
            grid.setPadding(new Insets(20));
            grid.add(new Text(), 0, 0);
            grid.add(new Text("会议地点"), 1, 0);
            grid.add(new Text("会议日期"), 2, 0);
            grid.add(new Text("开始时间"), 3, 0);
            grid.add(new Text("结束时间"), 4, 0);
            for (int i = 0; i < value.length(); i++) {

                try {
                    JSONObject jsonObject = value.getJSONObject(i);
                    CheckBox checkBox = new CheckBox();
                    checkBox.setId(jsonObject.get("meeting_id").toString());

                    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        cancelList.remove(checkBox.getId());
                        if (checkBox.isSelected()) {
                            cancelList.add(checkBox.getId());
                        }
                    });

                    grid.add(checkBox, 0, i + 1);
                    grid.add(new Text(jsonObject.get("floor") + "F" + jsonObject.getString("room_name")), 1, i + 1);
                    grid.add(new Text(jsonObject.getString("meeting_date")), 2, i + 1);

                    grid.add(new Text(jsonObject.getString("start_time")), 3, i + 1);
                    grid.add(new Text(jsonObject.getString("end_time")), 4, i + 1);

                } catch (JSONException ignored) {
                }

                dialog.getDialogPane().setContent(grid);
            }

            ButtonType buttonType = dialog.showAndWait().orElse(null);
            if (Objects.equals(cancel, buttonType)) {
                for (String meetingId : cancelList) {
                    applicationContext.getBean(CancelMeetingRoomService.class).setMeetingId(meetingId).start();
                }
            }
        });


    }
}
