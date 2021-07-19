package cn.promptness.meeting.tool.service;

import javafx.concurrent.Service;

public abstract class BaseService<V> extends Service<V> {
    public abstract Service<V> expect(Callback callback);
}
