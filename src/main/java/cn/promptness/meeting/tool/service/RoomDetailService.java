package cn.promptness.meeting.tool.service;

import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.httpclient.HttpResult;
import cn.promptness.meeting.tool.cache.AccountCache;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.pojo.Response;
import cn.promptness.meeting.tool.pojo.Room;
import cn.promptness.meeting.tool.pojo.RoomTime;
import cn.promptness.meeting.tool.utils.ProgressUtil;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import cn.promptness.meeting.tool.utils.TooltipUtil;
import com.google.gson.reflect.TypeToken;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RoomDetailService extends BaseService<Response<Room>> {

    @Resource
    private HttpClientUtil httpClientUtil;
    @Resource
    private ConfigurableApplicationContext applicationContext;
    private String meetingDate;
    private String roomId;

    @Override
    protected Task<Response<Room>> createTask() {
        return new Task<Response<Room>>() {
            @Override
            protected Response<Room> call() throws Exception {
                HttpResult httpResult = httpClientUtil.doGet(String.format("https://m.oa.fenqile.com/restful/get/meeting/meeting_room_address_room.json?address=中国储能大厦&meeting_date=%s", meetingDate), AccountCache.getHeaderList());
                return httpResult.getContent(new TypeToken<Response<Room>>() {
                }.getType());
            }
        };
    }

    public RoomDetailService setParam(String meetingDate, String roomId) {
        this.meetingDate = meetingDate;
        this.roomId = roomId;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Service<Response<Room>> expect(Callback callback) {
        super.setOnSucceeded(event -> {
            Response<Room> response = (Response<Room>) event.getSource().getValue();
            if (response.isSuccess()) {
                List<Room> result = response.getResult();
                if (CollectionUtils.isEmpty(result)) {
                    return;
                }
                for (Room room : result) {
                    if (Objects.equals(roomId, room.getRoomId())) {
                        showRoom(room);
                        return;
                    }
                }
                TooltipUtil.show("当天无此会议室!");
                return;
            }
            if (callback != null) {
                callback.call(event);
            }
        });
        return this;
    }

    private void showRoom(Room room) {
        List<String> timeList = new ArrayList<>();
        ButtonType confirm = new ButtonType("预定");
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(Constant.ROOM_INFO_MAP.get(roomId));
        dialog.setHeaderText(meetingDate);
        dialog.initOwner(SystemTrayUtil.getPrimaryStage());
        dialog.getDialogPane().getButtonTypes().add(confirm);
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(20));
        for (int i = 0; i < Constant.TIME_LIST.size() - 1; i++) {
            if (i % 2 == 0) {
                grid.add(new Text(Constant.TIME_LIST.get(i)), i / 2, 0);
            }
        }
        List<RoomTime> roomTimeList = room.getTime();
        for (int i = 0; i < roomTimeList.size(); i++) {
            Node node = getNode(roomTimeList.get(i), timeList);
            if (i % 2 == 0) {
                grid.add(node, i / 2, 1);
            } else {
                grid.add(node, i / 2, 2);
            }
        }
        dialog.getDialogPane().setContent(grid);
        ButtonType buttonType = dialog.showAndWait().orElse(null);
        if (Objects.equals(confirm, buttonType)) {
            if (CollectionUtils.isEmpty(timeList)) {
                return;
            }
            ConfirmRoomService confirmRoomService = applicationContext.getBean(ConfirmRoomService.class).param(roomId, meetingDate, timeList);
            ProgressUtil.of(SystemTrayUtil.getPrimaryStage(), confirmRoomService).show();
        }
    }

    private Node getNode(RoomTime roomTime, List<String> timeList) {
        // 1  时间段可用 0 此时间段不可用
        if (roomTime.getFlag() > 0) {
            CheckBox checkBox = new CheckBox();
            checkBox.setId(roomTime.getStartTime());
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                timeList.remove(checkBox.getId());
                if (checkBox.isSelected()) {
                    timeList.add(checkBox.getId());
                }
            });
            return checkBox;
        } else {
            Rectangle rectangle = new Rectangle(20, 20, Color.DARKGRAY);
            if (StringUtils.hasText(roomTime.getMin())) {
                rectangle.setOnMouseClicked(event -> TooltipUtil.show(roomTime.getMin()));
            }
            return rectangle;
        }
    }

}
