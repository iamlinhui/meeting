package cn.promptness.meeting.tool.task;

import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MeetingTaskProperties {

    private Integer plusDays;
    private String startTime;
    private String endTime;
    private List<String> roomIdList;
    private String cronDescription;
    private Boolean multipleChoice;

    public MeetingTaskProperties(Integer plusDays, String startTime, String endTime, List<String> roomIdList, String cronDescription, Boolean multipleChoice) {
        this.plusDays = plusDays;
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomIdList = roomIdList;
        this.cronDescription = cronDescription;
        this.multipleChoice = multipleChoice;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("偏移天数:\n").append(plusDays).append("\n\n");
        LocalDateTime firstTime = new CronSequenceGenerator(getCron()).next(new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String meetingDate = firstTime.plusDays(plusDays).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        stringBuilder.append("会议时间:\n").append(meetingDate).append("(").append(MeetingUtil.dateToWeek(meetingDate)).append(")").append(startTime).append("-").append(endTime).append("\n\n");
        stringBuilder.append("多选开关:\n").append(Boolean.TRUE.equals(multipleChoice) ? "开" : "关").append("\n\n");
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
        for (int i = 0; i < Calendar.DATE; i++) {
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

    public Boolean getMultipleChoice() {
        return multipleChoice;
    }

    public void setMultipleChoice(Boolean multipleChoice) {
        this.multipleChoice = multipleChoice;
    }

    public Boolean isEnable() {
        LocalDateTime firstTime = LocalDateTime.now();
        LocalTime localTime = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime start = firstTime.plusDays(plusDays).toLocalDate().atTime(localTime);

        return firstTime.isBefore(start);
    }
}
