package cn.promptness.meeting.tool.controller;

import cn.promptness.meeting.tool.cache.TaskCache;
import cn.promptness.meeting.tool.config.MeetingTaskProperties;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.service.RoomDetailService;
import cn.promptness.meeting.tool.service.ValidateUserService;
import cn.promptness.meeting.tool.task.MeetingTask;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import cn.promptness.meeting.tool.utils.ProgressUtil;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import cn.promptness.meeting.tool.utils.TooltipUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Controller
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TaskController {

    /**
     * 当前task编号
     */
    private final Integer target = MeetingUtil.next();

    @Resource
    private TaskScheduler taskScheduler;
    @Resource
    private ConfigurableApplicationContext applicationContext;
    @FXML
    public GridPane gridPane;
    @FXML
    private DatePicker meetingDate;
    @FXML
    private ComboBox<String> startTime;
    @FXML
    private ComboBox<String> endTime;
    @FXML
    private Button okButton;
    @Resource
    private MenuController menuController;

    private final ArrayBlockingQueue<ScheduledFuture<?>> taskFutures = new ArrayBlockingQueue<>(1);
    private final ArrayList<String> roomIdList = new ArrayList<>();
    private final ArrayList<CheckBox> checkBoxList = new ArrayList<>();
    private final boolean[] flag = {false, false, false};

    public void initialize() {
        meetingDate.setDayCellFactory(param -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(LocalDate.now())) {
                    setStyle("-fx-background-color: #ffc0cb;");
                    setDisable(true);
                }
            }
        });
        startTime.setItems(FXCollections.observableArrayList(Constant.TIME_LIST));
        endTime.setItems(FXCollections.observableArrayList(Constant.TIME_LIST));
        for (Map.Entry<String, String> entry : Constant.ROOM_INFO_MAP.entrySet()) {
            CheckBox checkBox = new CheckBox();
            checkBox.setText(entry.getValue());
            checkBox.setId(entry.getKey());
            checkBox.setOnMouseClicked(event -> {
                // 右键
                if (event.getButton().equals(MouseButton.SECONDARY)) {
                    LocalDate localDate = meetingDate.getValue();
                    if (localDate == null) {
                        TooltipUtil.show("请先选择日期!");
                        return;
                    }
                    String meetingId = ((CheckBox) event.getSource()).getId();
                    ProgressUtil.of(SystemTrayUtil.getPrimaryStage(), applicationContext.getBean(RoomDetailService.class).setParam(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), meetingId).expect(e -> menuController.login())).show();
                }
            });
            checkBoxList.add(checkBox);
        }

        addListener();
        initGridPane();
        initTask(TaskCache.getMeetingTaskProperties(getTarget()));
    }

    @FXML
    public void submit() {
        // 确认信息
        MeetingTaskProperties meetingTaskProperties = buildMeetingTaskProperties();
        if (isRunning()) {
            if (alertStop(meetingTaskProperties)) {
                stopTask(true);
            }
            return;
        }
        applicationContext.getBean(ValidateUserService.class).expect(event -> {
            if (alertStart(meetingTaskProperties)) {
                startTask(meetingTaskProperties);
            }
        }).start();
    }

    private boolean alertStart(MeetingTaskProperties meetingTaskProperties) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("开启");
        alert.setHeaderText("配置信息");
        alert.setContentText(meetingTaskProperties.toString());

        alert.initOwner(SystemTrayUtil.getPrimaryStage());
        ButtonType buttonType = alert.showAndWait().orElse(null);
        return Objects.equals(ButtonType.OK, buttonType);
    }

    private boolean alertStop(MeetingTaskProperties meetingTaskProperties) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("暂停");
        alert.setHeaderText("确定?");
        alert.setContentText(meetingTaskProperties.toString());
        alert.initOwner(SystemTrayUtil.getPrimaryStage());
        ButtonType buttonType = alert.showAndWait().orElse(null);
        return Objects.equals(ButtonType.OK, buttonType);
    }

    public void stopTask(Boolean success) {
        taskFutures.remove().cancel(true);
        disable(false);
        TooltipUtil.show(Objects.equals(success, Boolean.FALSE) ? "会议已经开始!" : "已关闭!");
    }

    private void startTask(MeetingTaskProperties meetingTaskProperties) {
        if (!meetingTaskProperties.checkTimeIsOk()) {
            TooltipUtil.show("会议已经开始!");
            return;
        }
        MeetingTask meetingTask = new MeetingTask(meetingTaskProperties, applicationContext);
        Trigger trigger = meetingTaskProperties.isEnable() ? new PeriodicTrigger(1, TimeUnit.MINUTES) : new CronTrigger("0 0/1 * * * *");
        taskFutures.add(taskScheduler.schedule(meetingTask, trigger));
        disable(true);
        TooltipUtil.show("开启成功!");
    }

    private void disable(boolean disable) {
        meetingDate.setDisable(disable);
        startTime.setDisable(disable);
        endTime.setDisable(disable);
        for (CheckBox checkBox : checkBoxList) {
            checkBox.setDisable(disable);
        }
        okButton.setText(disable ? "暂停" : "开启");
    }

    private void initGridPane() {
        int halfSize = (checkBoxList.size() % 2 == 0 ? 0 : 1) + checkBoxList.size() / 2;
        for (int i = 0; i < checkBoxList.size(); i++) {
            if (i < halfSize) {
                gridPane.add(checkBoxList.get(i), 1, 1 + i);
            } else {
                gridPane.add(checkBoxList.get(i), 2, 1 + i - halfSize);
            }
        }
        gridPane.add(okButton, 5, halfSize);
    }

    public boolean isRunning() {
        return !taskFutures.isEmpty();
    }

    public MeetingTaskProperties buildMeetingTaskProperties() {
        return new MeetingTaskProperties(meetingDate.getValue(), startTime.getValue(), endTime.getValue(), roomIdList, target, isRunning());
    }

    private void checkTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        try {
            if (StringUtils.isEmpty(startTime.getValue()) || StringUtils.isEmpty(endTime.getValue())) {
                flag[1] = false;
            } else {
                Date parseStartTime = simpleDateFormat.parse(startTime.getValue());
                Date parseEndTime = simpleDateFormat.parse(endTime.getValue());
                flag[1] = parseStartTime.before(parseEndTime);
                if (!flag[1]) {
                    TooltipUtil.show("时间范围不正确!");
                }
            }
        } catch (ParseException e) {
            flag[1] = false;
        }
    }

    private void checkSubmit() {
        boolean result = true;
        for (boolean b : flag) {
            result &= b;
        }
        okButton.setDisable(!result);
    }


    private void addListener() {
        meetingDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            flag[0] = newValue != null;
            checkSubmit();
        });

        startTime.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            checkTime();
            checkSubmit();
        });
        endTime.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            checkTime();
            checkSubmit();
        });

        for (CheckBox checkBox : checkBoxList) {
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                roomIdList.remove(checkBox.getId());
                if (checkBox.isSelected()) {
                    roomIdList.add(checkBox.getId());
                }
                flag[2] = !CollectionUtils.isEmpty(roomIdList);
                if (!flag[2]) {
                    TooltipUtil.show("至少选择一个!");
                }
                checkSubmit();
            });
        }
    }

    public void initTask(MeetingTaskProperties meetingTaskProperties) {
        if (meetingTaskProperties == null) {
            return;
        }
        meetingDate.setValue(meetingTaskProperties.getMeetingDate());
        startTime.setValue(meetingTaskProperties.getStartTime());
        endTime.setValue(meetingTaskProperties.getEndTime());

        for (String roomId : meetingTaskProperties.getRoomIdList()) {
            for (CheckBox checkBox : checkBoxList) {
                if (Objects.equals(roomId, checkBox.getId())) {
                    // 这里会自动触发监听函数
                    checkBox.setSelected(true);
                }
            }
        }
        if (Objects.equals(Boolean.TRUE, meetingTaskProperties.getRunning())) {
            Platform.runLater(() -> startTask(meetingTaskProperties));
        }
    }

    public Integer getTarget() {
        return target;
    }
}
