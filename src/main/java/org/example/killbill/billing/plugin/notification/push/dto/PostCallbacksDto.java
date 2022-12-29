package org.example.killbill.billing.plugin.notification.push.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.killbill.billing.notification.plugin.api.ExtBusEventType;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostCallbacksDto {
    private String callbackUrl;
    private List<ExtBusEventType> eventTypes;
}
