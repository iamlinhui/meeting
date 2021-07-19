package cn.promptness.meeting.tool.service;

import javafx.concurrent.WorkerStateEvent;

@FunctionalInterface
public interface Callback {

    void call(WorkerStateEvent event);
}
