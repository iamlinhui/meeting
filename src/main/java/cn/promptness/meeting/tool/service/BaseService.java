package cn.promptness.meeting.tool.service;

import javafx.concurrent.Service;

public abstract class BaseService<V> extends Service<V> {
    public Service<V> expect(Callback callback) {
        super.setOnSucceeded(callback::call);
        return this;
    }
}
