package cn.promptness.meeting.tool.controller;

import cn.promptness.meeting.tool.SpringFxmlLoader;
import cn.promptness.meeting.tool.cache.AccountCache;
import cn.promptness.meeting.tool.config.MeetingTaskProperties;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.service.CheckLoginService;
import cn.promptness.meeting.tool.service.SuccessRoomService;
import cn.promptness.meeting.tool.service.ValidateUserService;
import cn.promptness.meeting.tool.utils.ProgressUtil;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import cn.promptness.meeting.tool.utils.TooltipUtil;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

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
        AccountCache.read();
        applicationContext.getBean(ValidateUserService.class).expect(event -> {
            accountAction.setText("注销");
            accountTitle.setText(event.getSource().getValue().toString());
        }).start();
    }

    @FXML
    public void about() {
        Stage primaryStage = SystemTrayUtil.getPrimaryStage();
        final Hyperlink hyperlink = new Hyperlink("https://pan.holmes.cn/s/1nHN");
        hyperlink.setOnAction(t -> {
            HostServices hostServices = (HostServices) primaryStage.getProperties().get(primaryStage);
            hostServices.showDocument(hyperlink.getText());
        });
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(Constant.TITLE);
        alert.setHeaderText("关于");
        alert.setGraphic(hyperlink);
        alert.setContentText("Version " + buildProperties.getVersion() + "\nPowered By Lynn");
        alert.initOwner(primaryStage);
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
        alert.setHeaderText("控制台状态");
        alert.initOwner(SystemTrayUtil.getPrimaryStage());
        alert.getButtonTypes().add(ButtonType.CLOSE);
        if (!mainController.getCurrentTask().isRunning()) {
            alert.setContentText("请先开启任务!");
            alert.showAndWait();
            return;
        }
        applicationContext.getBean(ValidateUserService.class).expect(event -> {
            MeetingTaskProperties meetingTaskProperties = mainController.getCurrentTask().buildMeetingTaskProperties();
            alert.setContentText(meetingTaskProperties.toString());
            alert.showAndWait();
        }).start();
    }

    @FXML
    public void add() {
        mainController.addTab(true);
    }

    @FXML
    public void close() {
        System.exit(0);
    }

    @FXML
    public void list() {
        ProgressUtil.of(SystemTrayUtil.getPrimaryStage(), applicationContext.getBean(SuccessRoomService.class).expect(event -> login())).show();
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

    @FXML
    public void logger(ActionEvent actionEvent) {
        MenuItem menuItem = (MenuItem) actionEvent.getSource();
        LoggingSystem system = LoggingSystem.get(LoggingSystem.class.getClassLoader());
        system.setLogLevel("cn.promptness.meeting", LogLevel.valueOf(menuItem.getText().toUpperCase()));
    }
}
