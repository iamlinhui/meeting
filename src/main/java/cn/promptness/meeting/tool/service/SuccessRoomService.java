package cn.promptness.meeting.tool.service;

import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.httpclient.HttpResult;
import cn.promptness.meeting.tool.cache.AccountCache;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.pojo.Response;
import cn.promptness.meeting.tool.pojo.Room;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import cn.promptness.meeting.tool.utils.ProgressUtil;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import com.google.gson.reflect.TypeToken;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SuccessRoomService extends BaseService<Response<Room>> {

    @Resource
    private HttpClientUtil httpClientUtil;
    @Resource
    private ConfigurableApplicationContext applicationContext;

    @Override
    protected Task<Response<Room>> createTask() {
        return new Task<Response<Room>>() {
            @Override
            protected Response<Room> call() throws Exception {
                HttpResult httpResult = httpClientUtil.doGet("https://m.oa.fenqile.com/meeting/main/query_rooms.json", AccountCache.getHeaderList());
                return httpResult.getContent(new TypeToken<Response<Room>>() {}.getType());
            }
        };
    }

    private void listRoom(List<Room> roomList) {
        ArrayList<String> cancelList = new ArrayList<>();
        ButtonType cancel = new ButtonType("退订");

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
        for (int i = 0; i < roomList.size(); i++) {
            Room room = roomList.get(i);
            CheckBox checkBox = new CheckBox(room.getFloor() + "F" + room.getRoomName());
            checkBox.setId(room.getMeetingId().toString());

            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                cancelList.remove(checkBox.getId());
                if (checkBox.isSelected()) {
                    cancelList.add(checkBox.getId());
                }
            });

            grid.add(checkBox, 0, i + 1);
            grid.add(new Text(room.getMeetingDate()), 1, i + 1);
            grid.add(new Text(room.getStartTime()), 2, i + 1);
            grid.add(new Text(room.getEndTime()), 3, i + 1);
            grid.add(new Text(MeetingUtil.dateToWeek(room.getMeetingDate())), 4, i + 1);

            dialog.getDialogPane().setContent(grid);
        }

        ButtonType buttonType = dialog.showAndWait().orElse(null);
        if (Objects.equals(cancel, buttonType)) {
            if (CollectionUtils.isEmpty(cancelList)) {
                return;
            }
            CancelRoomService cancelMeetingRoomService = applicationContext.getBean(CancelRoomService.class).setCancelList(cancelList);
            ProgressUtil.of(SystemTrayUtil.getPrimaryStage(), cancelMeetingRoomService).show();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Service<Response<Room>> expect(Callback callback) {
        super.setOnSucceeded(event -> {
            Response<Room> response = (Response<Room>) event.getSource().getValue();
            if (response.isSuccess()) {
                listRoom(response.getResult());
                return;
            }
            if (callback != null) {
                callback.call(event);
            }
        });
        return this;
    }
}
