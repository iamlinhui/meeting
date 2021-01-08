package cn.promptness.meeting.tool.task;

import cn.promptness.meeting.tool.data.Constant;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MeetingTaskProperties {

    private Integer plusDays;
    private String startTime;
    private String endTime;
    private List<String> roomIdList;
    private String cronDescription;

    public MeetingTaskProperties(Integer plusDays, String startTime, String endTime, List<String> roomIdList, String cronDescription) {
        this.plusDays = plusDays;
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomIdList = roomIdList;
        this.cronDescription = cronDescription;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("偏移天数:\n").append(plusDays).append("\n\n");
        stringBuilder.append("会议时间:\n").append(startTime).append("-").append(endTime).append("\n\n");
        stringBuilder.append("预定列表:\n");
        for (String roomId : roomIdList) {
            stringBuilder.append(Constant.ROOM_INFO_LIST.get(roomId)).append("\n");
        }
        stringBuilder.append("\n触发周期:\n").append(cronDescription).append("\n\n");
        return stringBuilder.toString();
    }

    public String mockCron() {
        StringBuilder format = new StringBuilder("即将运行的时间:\n");
        Date now = new Date();
        for (int i = 0; i < BigDecimal.TEN.intValue(); i++) {
            CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(getCron());
            now = cronSequenceGenerator.next(now);
            format.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now)).append("\n");
        }
        return format.toString();
    }

    public String getCron() {
        if (StringUtils.isEmpty(cronDescription)) {
            return null;
        }
        return Constant.CRON_LIST.get(cronDescription);
    }

    public Integer getPlusDays() {
        return plusDays;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public List<String> getRoomIdList() {
        return roomIdList;
    }

    public String getCronDescription() {
        return cronDescription;
    }

    public void setPlusDays(Integer plusDays) {
        this.plusDays = plusDays;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setRoomIdList(List<String> roomIdList) {
        this.roomIdList = roomIdList;
    }

    public void setCronDescription(String cronDescription) {
        this.cronDescription = cronDescription;
    }
}
