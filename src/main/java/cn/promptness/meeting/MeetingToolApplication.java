package cn.promptness.meeting;

import cn.promptness.meeting.tool.MySystemTray;
import cn.promptness.meeting.tool.SpringFXMLLoader;
import cn.promptness.meeting.tool.service.ValidateUserService;
import com.github.windpapi4j.WinDPAPI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.awt.*;

@SpringBootApplication
@EnableScheduling
public class MeetingToolApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        if (!WinDPAPI.isPlatformSupported() || !SystemTray.isSupported()) {
            System.exit(1);
        }
        Application.launch(MeetingToolApplication.class, args);
    }

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder().sources(MeetingToolApplication.class).bannerMode(Banner.Mode.OFF).web(WebApplicationType.NONE).run(getParameters().getRaw().toArray(new String[0]));
        applicationContext.getBean(ValidateUserService.class).start();
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader fxmlLoader = applicationContext.getBean(SpringFXMLLoader.class).getLoader("/fxml/main.fxml");
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        primaryStage.setTitle("会议室助手 - Powered By Lynn");
        primaryStage.getIcons().add(new Image("/icon.jpg"));
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        // 获取屏幕可视化的宽高（Except TaskBar），把窗体设置在可视化的区域居中
        primaryStage.setX((Screen.getPrimary().getVisualBounds().getWidth() - primaryStage.getWidth()) / 2.0);
        primaryStage.setY((Screen.getPrimary().getVisualBounds().getHeight() - primaryStage.getHeight()) / 2.0);


        MySystemTray.getInstance(primaryStage);
    }
}
