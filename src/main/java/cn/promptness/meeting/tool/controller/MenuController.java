package cn.promptness.meeting.tool.controller;

import cn.promptness.meeting.tool.SpringFxmlLoader;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.service.CancelMeetingRoomService;
import cn.promptness.meeting.tool.service.CheckLoginService;
import cn.promptness.meeting.tool.service.MeetingRoomService;
import cn.promptness.meeting.tool.service.ValidateUserService;
import cn.promptness.meeting.tool.task.MeetingTaskProperties;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Objects;

@Controller
public class MenuController {


    @FXML
    public Menu accountTitle;
    @FXML
    public MenuItem accountAction;
    @Resource
    private BuildProperties buildProperties;
    @Resource
    private ConfigurableApplicationContext applicationContext;
    @Resource
    private SpringFxmlLoader springFxmlLoader;
    @Resource
    private MainController mainController;

    public void initialize() {

    }

    @FXML
    public void clear() {
        boolean clearSuccess = mainController.clear();
        if (!clearSuccess) {
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle(Constant.TITLE);
            alert.setHeaderText("重置配置");
            alert.initOwner(SystemTrayUtil.getPrimaryStage());
            alert.getButtonTypes().add(ButtonType.CLOSE);
            alert.setContentText("请先暂停任务!");
            alert.showAndWait();
        }
    }

    @FXML
    public void about() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(Constant.TITLE);
        alert.setHeaderText("关于");
        alert.setContentText("Version " + buildProperties.getVersion() + "\nPowered By Lynn");
        alert.initOwner(SystemTrayUtil.getPrimaryStage());
        alert.getButtonTypes().add(ButtonType.CLOSE);
        alert.showAndWait();
    }

    @FXML
    public void instruction() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(Constant.TITLE);
        alert.setHeaderText("使用说明");
        alert.setContentText("1.打开MOA扫码登录\n2.会议室的勾选顺序决定预定的顺序\n3.每次执行中按预约的顺序成功预定一间即结束");
        alert.initOwner(SystemTrayUtil.getPrimaryStage());
        alert.getButtonTypes().add(ButtonType.CLOSE);
        alert.showAndWait();
    }

    @FXML
    public void show() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(Constant.TITLE);
        alert.setHeaderText("运行状态");
        alert.initOwner(SystemTrayUtil.getPrimaryStage());
        alert.getButtonTypes().add(ButtonType.CLOSE);
        if (mainController.isRunning()) {
            ValidateUserService validateUserService = applicationContext.getBean(ValidateUserService.class);
            validateUserService.start();
            validateUserService.setOnSucceeded(event -> {
                if (StringUtils.isEmpty(event.getSource().getValue())) {
                    login();
                    return;
                }
                MeetingTaskProperties meetingTaskProperties = mainController.buildMeetingTaskProperties();
                alert.setContentText(meetingTaskProperties.toString() + meetingTaskProperties.mockCron());
                alert.showAndWait();
            });
        } else {
            alert.setContentText("请先开启任务!");
            alert.showAndWait();
        }
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
                login();
                return;
            }
            JSONArray value = (JSONArray) event.getSource().getValue();

            ArrayList<String> cancelList = new ArrayList<>();
            ButtonType cancel = new ButtonType("取消会议室");

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(Constant.TITLE);
            dialog.setHeaderText("成功列表");
            dialog.initOwner(SystemTrayUtil.getPrimaryStage());
            dialog.getDialogPane().getButtonTypes().add(cancel);


            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));
            grid.add(new Text("会议地点"), 0, 0);
            grid.add(new Text("会议日期"), 1, 0);
            grid.add(new Text("开始时间"), 2, 0);
            grid.add(new Text("结束时间"), 3, 0);
            grid.add(new Text("星期"), 4, 0);
            for (int i = 0; i < value.length(); i++) {

                try {
                    JSONObject jsonObject = value.getJSONObject(i);
                    CheckBox checkBox = new CheckBox(jsonObject.get("floor") + "F" + jsonObject.getString("room_name"));
                    checkBox.setId(jsonObject.get("meeting_id").toString());

                    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        cancelList.remove(checkBox.getId());
                        if (checkBox.isSelected()) {
                            cancelList.add(checkBox.getId());
                        }
                    });

                    grid.add(checkBox, 0, i + 1);
                    String meetingDate = jsonObject.getString("meeting_date");
                    grid.add(new Text(meetingDate), 1, i + 1);
                    grid.add(new Text(jsonObject.getString("start_time")), 2, i + 1);
                    grid.add(new Text(jsonObject.getString("end_time")), 3, i + 1);
                    grid.add(new Text(MeetingUtil.dateToWeek(meetingDate)), 4, i + 1);

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


    @FXML
    public void account() {

        if (MeetingUtil.haveAccount()) {
            login();
            return;
        }

        Scene scene = new Scene(springFxmlLoader.load("/fxml/login.fxml"));

        Stage loginStage = new Stage();
        loginStage.setTitle("MOA扫码登录");
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.initOwner(SystemTrayUtil.getPrimaryStage());
        loginStage.getIcons().add(new Image("/icon.jpg"));
        loginStage.setResizable(false);
        loginStage.setScene(scene);
        loginStage.show();

        CheckLoginService checkLoginService = applicationContext.getBean(CheckLoginService.class).setStage(loginStage);
        checkLoginService.start();

        checkLoginService.setOnSucceeded(event -> {
            if (Objects.equals(Boolean.TRUE, event.getSource().getValue())) {
                ValidateUserService validateUserService = applicationContext.getBean(ValidateUserService.class);
                validateUserService.start();
                validateUserService.setOnSucceeded(validateEvent -> {
                    if (!StringUtils.isEmpty(validateEvent.getSource().getValue())) {
                        loginStage.close();
                        accountAction.setText("注销");
                        accountTitle.setText(validateEvent.getSource().getValue().toString());
                    }
                });
            }
        });
    }

    public void login() {
        MeetingUtil.logout();
        accountAction.setText("登录");
        accountTitle.setText("账户");
        account();
    }

}
