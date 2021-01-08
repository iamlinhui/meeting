package cn.promptness.meeting;

import cn.promptness.meeting.tool.SpringFxmlLoader;
import cn.promptness.meeting.tool.controller.MenuController;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.task.ContinuationTask;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
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
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader fxmlLoader = applicationContext.getBean(SpringFxmlLoader.class).getLoader("/fxml/main.fxml");
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        primaryStage.setTitle(Constant.TITLE + " - Powered By Lynn");
        primaryStage.getIcons().add(new Image("/icon.jpg"));
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        SystemTrayUtil.getInstance(primaryStage);
        applicationContext.getBean(MenuController.class).login();

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ContinuationTask.class);
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        defaultListableBeanFactory.registerBeanDefinition(ContinuationTask.class.getName(), beanDefinitionBuilder.getBeanDefinition());
        applicationContext.getBean(ContinuationTask.class);
    }
}
