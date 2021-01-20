package cn.promptness.meeting.tool.controller;

import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.service.ValidateUserService;
import cn.promptness.meeting.tool.task.MeetingTask;
import cn.promptness.meeting.tool.task.MeetingTaskProperties;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.FutureTask;
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
    @FXML
    public RadioButton multipleChoice;
    @Resource
    private MenuController menuController;

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
        MeetingTaskProperties meetingTaskProperties = new MeetingTaskProperties(plusDays.getValue(), startTime.getValue(), endTime.getValue(), roomIdList, cronDescription.getValue(), multipleChoice.isSelected());
        if (isRunning() && alertStop(meetingTaskProperties)) {
            stopTask();
        } else {
            ValidateUserService validateUserService = applicationContext.getBean(ValidateUserService.class);
            validateUserService.start();
            validateUserService.setOnSucceeded(event -> {
                if (StringUtils.isEmpty(event.getSource().getValue())) {
                    menuController.login();
                    return;
                }
                if (alertStart(meetingTaskProperties)) {
                    startTask(meetingTaskProperties);
                }
            });
        }
    }

    private boolean alertStart(MeetingTaskProperties meetingTaskProperties) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("开启会议室助手");
        alert.setHeaderText("配置信息");
        alert.setContentText(meetingTaskProperties.toString() + meetingTaskProperties.mockCron());

        alert.initOwner(SystemTrayUtil.getPrimaryStage());
        ButtonType buttonType = alert.showAndWait().orElse(null);
        return Objects.equals(ButtonType.OK, buttonType);
    }

    private boolean alertStop(MeetingTaskProperties meetingTaskProperties) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("暂停会议室助手");
        alert.setHeaderText("确定?");
        alert.setContentText(meetingTaskProperties.toString() + meetingTaskProperties.mockCron());
        alert.initOwner(SystemTrayUtil.getPrimaryStage());
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
        if (isRunning()) {
            ScheduledFuture<?> scheduledFuture = taskFutures.poll();
            scheduledFuture.cancel(true);
            disable(false);
        }
    }

    private void startTask(MeetingTaskProperties meetingTaskProperties) {
        if (!isRunning()) {
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
        multipleChoice.setDisable(disable);
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


    public boolean clear() {
        if (!isRunning()) {
            plusDays.setValue(null);
            startTime.setValue(null);
            endTime.setValue(null);
            cronDescription.setValue(null);
            multipleChoice.setSelected(Boolean.FALSE);
            for (CheckBox checkBox : checkBoxList) {
                checkBox.setSelected(false);
            }
            roomIdList.clear();
            okButton.setDisable(true);
            Arrays.fill(flag, false);
            return true;
        }
        return false;
    }

    public boolean isRunning() {
        return !taskFutures.isEmpty();
    }

    public MeetingTaskProperties buildMeetingTaskProperties() {
        return new MeetingTaskProperties(plusDays.getValue(), startTime.getValue(), endTime.getValue(), roomIdList, cronDescription.getValue(), multipleChoice.isSelected());
    }
}
