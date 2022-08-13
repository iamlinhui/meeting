package cn.promptness.meeting.tool.utils;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;

public class TooltipUtil {

    public static void show(String message) {
        Stage primaryStage = SystemTrayUtil.getPrimaryStage();
        if (primaryStage == null) {
            return;
        }
        Parent root = primaryStage.getScene().getRoot();
        Circle circle = new Circle(10, Color.rgb(255, 127, 79));
        double x = getScreenX(root) + getWidth(root) / 2 - getWidth(message) / 1.5 - circle.getRadius();
        double y = getScreenY(root) + getHeight(root) / 2;
        Tooltip tooltip = new Tooltip(message);
        tooltip.setAutoHide(true);
        tooltip.setWrapText(true);
        tooltip.setGraphic(circle);
        tooltip.show(primaryStage, x, y);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(tooltip::hide);
            }
        }, 2000);
    }

    public static double getScreenX(Node control) {
        return control.getScene().getWindow().getX() + control.getScene().getX() + control.localToScene(0, 0).getX();
    }

    public static double getScreenY(Node control) {
        return control.getScene().getWindow().getY() + control.getScene().getY() + control.localToScene(0, 0).getY();
    }

    public static double getWidth(Node control) {
        return control.getBoundsInParent().getWidth();
    }

    public static double getHeight(Node control) {
        return control.getBoundsInParent().getHeight();
    }

    public static double getWidth(String message) {
        return new Text(message).getBoundsInLocal().getWidth();
    }
}
