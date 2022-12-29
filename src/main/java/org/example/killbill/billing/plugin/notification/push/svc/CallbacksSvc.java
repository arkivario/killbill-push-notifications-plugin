package org.example.killbill.billing.plugin.notification.push.svc;

import lombok.RequiredArgsConstructor;
import org.example.killbill.billing.plugin.notification.push.dao.CallbacksDao;

@RequiredArgsConstructor
public class CallbacksSvc {

    private final CallbacksDao dao;

    public void registerCallbacks() {

    }
}
