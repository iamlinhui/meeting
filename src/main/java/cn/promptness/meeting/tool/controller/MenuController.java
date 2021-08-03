package cn.promptness.meeting.tool.controller;

import cn.promptness.meeting.tool.SpringFxmlLoader;
import cn.promptness.meeting.tool.cache.AccountCache;
import cn.promptness.meeting.tool.config.MeetingTaskProperties;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.service.CheckLoginService;
import cn.promptness.meeting.tool.service.MeetingRoomService;
import cn.promptness.meeting.tool.service.ValidateUserService;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import cn.promptness.meeting.tool.utils.TooltipUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.io.IOException;

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
    private TaskController taskController;

    public void initialize() {
        AccountCache.read();
        applicationContext.getBean(ValidateUserService.class).expect(event -> {
            accountAction.setText("注销");
            accountTitle.setText(event.getSource().getValue().toString());
        }).start();
    }

    @FXML
    public void clear() {
        boolean clearSuccess = taskController.clear();
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
        alert.setContentText("1.打开MOA扫码登录\n2.会议室的勾选顺序决定预定的顺序");
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
        if (!taskController.isRunning()) {
            alert.setContentText("请先开启任务!");
            alert.showAndWait();
            return;
        }
        applicationContext.getBean(ValidateUserService.class).expect(event -> {
            MeetingTaskProperties meetingTaskProperties = taskController.buildMeetingTaskProperties();
            alert.setContentText(meetingTaskProperties.toString());
            alert.showAndWait();
        }).start();
    }

    @FXML
    public void exit() {
        System.exit(0);
    }

    @FXML
    public void list() {
        applicationContext.getBean(MeetingRoomService.class).expect(event -> login()).start();
    }

    @FXML
    public void account() {
        // 有账户 点击时就是注销
        if (AccountCache.haveAccount()) {
            doLogout();
            return;
        }
        doLogin();
    }

    public void login() {
        doLogout();
        doLogin();
    }

    private void doLogin() {
        Stage primaryStage = SystemTrayUtil.getPrimaryStage();
        Parent root = primaryStage.getScene().getRoot();
        double x = TooltipUtil.getScreenX(root) + TooltipUtil.getWidth(root) / 3;
        double y = TooltipUtil.getScreenY(root) + TooltipUtil.getHeight(root) / 4;

        Scene scene = new Scene(springFxmlLoader.load("/fxml/login.fxml"));

        Stage loginStage = new Stage();
        loginStage.setTitle("MOA扫码登录");
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.initOwner(SystemTrayUtil.getPrimaryStage());
        loginStage.getIcons().add(new Image("/icon.png"));
        loginStage.setResizable(false);
        loginStage.setScene(scene);
        loginStage.setX(x);
        loginStage.setY(y);
        loginStage.show();

        applicationContext.getBean(CheckLoginService.class).setStage(loginStage).expect(event -> {
            accountAction.setText("注销");
            accountTitle.setText(event.getSource().getValue().toString());
        }).start();
    }

    private void doLogout() {
        AccountCache.logout();
        accountAction.setText("登录");
        accountTitle.setText("账户");
    }

    @Resource
    private MainController mainController;
    @FXML
    public void add(ActionEvent actionEvent)  {
        mainController.add();
    }
}
