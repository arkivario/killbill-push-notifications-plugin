package org.example.killbill.plugin.anotherpush.json;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.killbill.billing.notification.plugin.api.ExtBusEvent;

import java.util.UUID;

/*
* This is a slightly simplified replica of KillBill's NotificationJson.java
* made for avoiding import a whole org.kill-bill.billing:killbill-jaxrs.
* Original: https://github.com/killbill/killbill/blob/b99b07ef61dfe300827e53efdb744fd02cbccb94/jaxrs/src/main/java/org/killbill/billing/jaxrs/json/NotificationJson.java
* */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class NotificationJson {

    private final String eventType;
    private final UUID accountId;
    private final String objectType;
    private final UUID objectId;
    private final String metaData;

    public NotificationJson(final ExtBusEvent event) {
        this(event.getEventType().toString(),
                event.getAccountId(),
                event.getObjectType().toString(),
                event.getObjectId(),
                event.getMetaData());
    }
}
