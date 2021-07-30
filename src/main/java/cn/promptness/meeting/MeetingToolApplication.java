package cn.promptness.meeting;

import cn.promptness.meeting.tool.SpringFxmlLoader;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.Style;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.awt.*;

@SpringBootApplication
@EnableScheduling
public class MeetingToolApplication extends Application implements ApplicationListener<ContextClosedEvent> {

    private ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        if (!SystemTray.isSupported()) {
            System.exit(1);
        }
        Application.launch(MeetingToolApplication.class, args);
    }

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder().sources(MeetingToolApplication.class).bannerMode(Banner.Mode.OFF).web(WebApplicationType.NONE).run(getParameters().getRaw().toArray(new String[0]));
    }


    @Override
    public void start(Stage primaryStage) {

        Parent root = applicationContext.getBean(SpringFxmlLoader.class).load("/fxml/main.fxml");
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(Style.LIGHT.getStyleStylesheetURL());
        primaryStage.setTitle(Constant.TITLE + " - Powered By Lynn");
        primaryStage.getIcons().add(new Image("/icon.png"));
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        SystemTrayUtil.systemTray(primaryStage, Constant.TITLE);
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        MeetingUtil.cache();
    }
}
