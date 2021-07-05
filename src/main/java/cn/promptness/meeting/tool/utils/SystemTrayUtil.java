package cn.promptness.meeting.tool.utils;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 自定义系统托盘(单例模式)
 */
public class SystemTrayUtil {

    private static final Logger log = LoggerFactory.getLogger(SystemTrayUtil.class);

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    private static Stage primaryStage;

    public static void systemTray(Stage stage, String toolTip) {
        primaryStage = stage;
        if (!SystemTray.isSupported()) {
            log.info("系统托盘不支持");
            return;
        }
        Platform.setImplicitExit(false);
        MenuItem showItem = new MenuItem("打开");
        MenuItem exitItem = new MenuItem("退出");

        //设置悬停提示信息
        Image trayIconImage = Toolkit.getDefaultToolkit().getImage(SystemTrayUtil.class.getResource("/icon.png"));
        int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
        TrayIcon trayIcon = new TrayIcon(trayIconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH));

        //设置图标尺寸自动适应
        trayIcon.setImageAutoSize(true);
        //系统托盘
        SystemTray tray = SystemTray.getSystemTray();
        //弹出式菜单组件
        final PopupMenu popup = new PopupMenu();
        popup.add(showItem);
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);
        //鼠标移到系统托盘,会显示提示文本
        trayIcon.setToolTip(toolTip);

        //给菜单项添加事件
        showItem.addActionListener(e -> Platform.runLater(() -> {
            if (stage.isIconified()) {
                stage.setIconified(false);
            }
            if (!stage.isShowing()) {
                stage.show();
            }
            stage.toFront();
        }));
        exitItem.addActionListener(e -> System.exit(0));
        addMouseListener(trayIcon);
        try {
            tray.add(trayIcon);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    private static void addMouseListener(TrayIcon trayIcon) {
        //给系统托盘添加鼠标响应事件
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //鼠标左键
                if (e.getButton() == MouseEvent.BUTTON1) {
                    //点击系统托盘,
                    Platform.runLater(() -> {
                        if (primaryStage.isIconified()) {
                            primaryStage.setIconified(false);
                        }
                        if (!primaryStage.isShowing()) {
                            primaryStage.show();
                        }
                        primaryStage.toFront();
                    });
                }
            }
        });
    }
}