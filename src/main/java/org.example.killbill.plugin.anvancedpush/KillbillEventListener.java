package org.example.killbill.plugin.anvancedpush;

import lombok.extern.slf4j.Slf4j;
import org.killbill.billing.notification.plugin.api.ExtBusEvent;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillEventDispatcher;

@Slf4j
public class KillbillEventListener implements OSGIKillbillEventDispatcher.OSGIKillbillEventHandler {

    @Override
    public void handleKillbillEvent(final ExtBusEvent killbillEvent) {
        log.info("Received event {} for object id {} of type {}",
                killbillEvent.getEventType(),
                killbillEvent.getObjectId(),
                killbillEvent.getObjectType());
    }
}
