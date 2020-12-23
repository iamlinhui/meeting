package cn.promptness.meeting.tool.controller;

import cn.promptness.meeting.tool.MySystemTray;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.service.ValidateUserService;
import cn.promptness.meeting.tool.task.MeetingTask;
import cn.promptness.meeting.tool.task.MeetingTaskProperties;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledFuture;

@Controller
public class MainController {

    @Resource
    private TaskScheduler taskScheduler;
    @Resource
    private ConfigurableApplicationContext applicationContext;
    @FXML
    private Pane pane;
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
            flag[0] = newValue >= 0 && newValue <= 7;
            checkSubmit(flag, okButton);
        });

        startTime.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            extracted(startTime, endTime, flag);
            checkSubmit(flag, okButton);
        });
        endTime.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            extracted(startTime, endTime, flag);
            checkSubmit(flag, okButton);
        });

        cronDescription.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String value = Constant.CRON_LIST.get(newValue);
            flag[2] = CronSequenceGenerator.isValidExpression(value);
            checkSubmit(flag, okButton);
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
                checkSubmit(flag, okButton);
            });
            checkBoxList.add(checkBox);
        }
        GridPane grid = getGridPane(okButton, plusDays, startTime, endTime, cronDescription, checkBoxList);

        pane.getChildren().add(grid);
    }

    private boolean alert(MeetingTaskProperties meetingTaskProperties) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("确认开启会议室助手");
        alert.setHeaderText("配置信息");
        alert.setContentText(meetingTaskProperties.toString() + meetingTaskProperties.mockCron());

        alert.initOwner(MySystemTray.getPrimaryStage());
        ButtonType buttonType = alert.showAndWait().orElse(null);
        return Objects.equals(ButtonType.OK, buttonType);
    }

    public void submit() {
        if (taskFutures.isEmpty()) {
            // 确认信息
            MeetingTaskProperties meetingTaskProperties = new MeetingTaskProperties(plusDays.getValue(), startTime.getValue(), endTime.getValue(), roomIdList, cronDescription.getValue());
            if (alert(meetingTaskProperties)) {
                applicationContext.getBean(ValidateUserService.class).start();
                start(meetingTaskProperties);
                plusDays.setDisable(true);
                startTime.setDisable(true);
                endTime.setDisable(true);
                cronDescription.setDisable(true);
                for (CheckBox checkBox : checkBoxList) {
                    checkBox.setDisable(true);
                }
                okButton.setText("暂停");
            }
        } else {
            delete();
            plusDays.setDisable(false);
            startTime.setDisable(false);
            endTime.setDisable(false);
            cronDescription.setDisable(false);
            for (CheckBox checkBox : checkBoxList) {
                checkBox.setDisable(false);
            }
            okButton.setText("开启");
        }
    }

    private void extracted(ChoiceBox<String> startTime, ChoiceBox<String> endTime, boolean[] flag) {
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


    public void delete() {
        if (!taskFutures.isEmpty()) {
            ScheduledFuture<?> scheduledFuture = taskFutures.poll();
            scheduledFuture.cancel(true);
        }
    }

    public void start(MeetingTaskProperties meetingTaskProperties) {
        if (taskFutures.isEmpty()) {
            MeetingTask meetingTask = new MeetingTask(meetingTaskProperties);
            ScheduledFuture<?> schedule = taskScheduler.schedule(meetingTask, new CronTrigger(meetingTaskProperties.getCron()));
            taskFutures.add(schedule);
        }
    }

    private void checkSubmit(boolean[] flag, Node okButton) {
        boolean result = true;
        for (boolean b : flag) {
            result &= b;
        }
        okButton.setDisable(!result);
    }

    private GridPane getGridPane(Button okButton, ChoiceBox<Integer> plusDays, ChoiceBox<String> startTime, ChoiceBox<String> endTime, ChoiceBox<String> cron, ArrayList<CheckBox> checkBoxList) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 50, 20, 10));

        grid.add(new Label("偏移天数:"), 0, 0);
        grid.add(plusDays, 1, 0);

        grid.add(new Label("会议时间:"), 2, 0);
        grid.add(startTime, 3, 0);
        grid.add(new Label("~"), 4, 0);
        grid.add(endTime, 5, 0);

        grid.add(new Label("触发周期:"), 0, 1);
        grid.add(cron, 1, 1);

        grid.add(new Label("预定列表:"), 0, 2);
        for (int i = 0; i < checkBoxList.size(); i++) {
            grid.add(checkBoxList.get(i), 1 + i / 10, 2 + i - (i / 10) * 10);
        }
        grid.add(okButton, 5, 2 + checkBoxList.size() - 1 - ((checkBoxList.size() - 1) / 10) * 10);
        return grid;
    }

}
