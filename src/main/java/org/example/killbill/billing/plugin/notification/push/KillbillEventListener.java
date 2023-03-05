package org.example.killbill.billing.plugin.notification.push;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.example.killbill.billing.plugin.notification.push.dao.CallbacksDao;
import org.example.killbill.billing.plugin.notification.push.json.NotificationJson;
import org.killbill.billing.notification.plugin.api.ExtBusEvent;
import org.killbill.billing.notification.plugin.api.ExtBusEventType;
import org.killbill.billing.notification.plugin.api.NotificationPluginApiRetryException;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillEventDispatcher;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
public class KillbillEventListener implements OSGIKillbillEventDispatcher.OSGIKillbillEventHandler {

    private static final Duration TIMEOUT_NOTIFICATION = Duration.ofSeconds(15); //todo: configure

    private final HttpClient httpClient;
    private final CallbacksDao dao;
    private final ObjectMapper objectMapper;

    public KillbillEventListener(CallbacksDao dao) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT_NOTIFICATION)
                .build();
        this.dao = dao;
        //todo: find out if KillBill customize objectMapper for it's PushNotificationListener
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handleKillbillEvent(final ExtBusEvent killbillEvent) {
        log.debug("Received event {} for object id {} of type {}",
                killbillEvent.getEventType(),
                killbillEvent.getObjectId(),
                killbillEvent.getObjectType());

        try {
            final List<String> callbacks = getCallbacksFor(killbillEvent.getTenantId(), killbillEvent.getEventType());
            if (callbacks.isEmpty()) {
                return;
            }

            dispatchCallbacks(killbillEvent, callbacks);

        } catch (SQLException e) {
            log.warn("Failed to retrieve push notification callbacks for tenant {}: A database access error occurred.",
                    killbillEvent.getTenantId(), e);

        } catch (JsonProcessingException e) {
            log.warn("Failed to push notification for tenantId='{}': Unable to write push notification body.",
                    killbillEvent.getTenantId(), e);
        }
    }

    private List<String> getCallbacksFor(final UUID kbTenantId, final ExtBusEventType eventType) throws SQLException {
        log.debug("Retrieving callbacks for tenant '{}' and event type '{}'", kbTenantId, eventType);

        final List<String> callbacks = dao.retrieveCallbacks(kbTenantId, eventType);
        log.trace("Retrieved callbacks: {}", callbacks);

        return callbacks == null ? Collections.emptyList() : callbacks;
    }

    private void dispatchCallbacks(final ExtBusEvent event, final Iterable<String> callbacks)
            throws JsonProcessingException {
        final UUID tenantId = event.getTenantId();
        final NotificationJson notification = new NotificationJson(event);

        final String body = objectMapper.writeValueAsString(notification);

        for (final String callback : callbacks) {
            doPost(tenantId, callback, body);
        }
    }

    private void doPost(final UUID tenantId, final String url, final String body) {
        log.info("Sending push notification url='{}', body='{}'", url, body);

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HttpHeaders.USER_AGENT, "KillBill/1.0")
                .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                .timeout(TIMEOUT_NOTIFICATION)
                .POST(HttpRequest.BodyPublishers.ofString(body)) //todo: body null check?
                .build();

        final HttpResponse<Void> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

        } catch (InterruptedException e) {
            log.warn("Failed to push notification url='{}', tenantId='{}': Thread was interrupted.",
                    url, tenantId);
            Thread.currentThread().interrupt();
            // Retrying: https://docs.killbill.io/latest/notification_plugin.html#_retries
            throw new NotificationPluginApiRetryException(e);

        } catch (IOException e) {
            log.warn("Failed to push notification url='{}', tenantId='{}': I/O exception.",
                    url, tenantId, e);
            throw new NotificationPluginApiRetryException(e);
        }

        if (!(response.statusCode() >= 200 && response.statusCode() < 300)) {
            throw new NotificationPluginApiRetryException(); //todo: would it be DEFAULT_RETRY_SCHEDULE or not?
        }
    }
}
