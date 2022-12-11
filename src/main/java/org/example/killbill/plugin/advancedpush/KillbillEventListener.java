package org.example.killbill.plugin.advancedpush;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;
import org.example.killbill.plugin.advancedpush.persist.dao.CallbacksDao;
import org.killbill.billing.jaxrs.json.NotificationJson;
import org.killbill.billing.notification.plugin.api.ExtBusEvent;
import org.killbill.billing.notification.plugin.api.ExtBusEventType;
import org.killbill.billing.notification.plugin.api.NotificationPluginApiRetryException;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillEventDispatcher;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class KillbillEventListener implements OSGIKillbillEventDispatcher.OSGIKillbillEventHandler {

    private static final int TIMEOUT_NOTIFICATION_SEC = 15; //todo: configure

    private final AsyncHttpClient httpClient;
    private final CallbacksDao dao;
    private final ObjectMapper objectMapper;

    public KillbillEventListener(CallbacksDao dao) {
        this.httpClient = new DefaultAsyncHttpClient(new DefaultAsyncHttpClientConfig.Builder()
                .setConnectTimeout(TIMEOUT_NOTIFICATION_SEC * 1000)
                .setRequestTimeout(TIMEOUT_NOTIFICATION_SEC * 1000).build());
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

    public void shutdown() {
        try {
            httpClient.close();
        } catch (IOException e) {
            /*
             * IOException actually is never thrown while closing a DefaultAsyncHttpClient.
             * See https://github.com/AsyncHttpClient/async-http-client/blob/7a370af58dc8895a27a14d0a81af2a3b91930651/client/src/main/java/org/asynchttpclient/DefaultAsyncHttpClient.java#L117
             * */
            log.warn("An I/O error occurred while closing http client", e);
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
            doPost(tenantId, callback, body, TIMEOUT_NOTIFICATION_SEC);
        }
    }

    private void doPost(final UUID tenantId, final String url, final String body, final int timeoutSec) {
        log.info("Sending push notification url='{}', body='{}'", url, body);

        final BoundRequestBuilder builder = httpClient.preparePost(url);
        builder.setBody(body); //todo: body null check?
        builder.addHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

        final ListenableFuture<Response> future = builder.execute(new AsyncCompletionHandler<Response>() {
            @Override
            public Response onCompleted(final Response response) throws Exception {
                return response;
            }
        });

        final Response response;
        try {
            response = future.get(timeoutSec, TimeUnit.SECONDS);

        } catch (ExecutionException e) { //todo: deal with it
            throw new RuntimeException(e);

        } catch (InterruptedException e) {
            log.warn("Failed to push notification url='{}', tenantId='{}': Thread was interrupted.",
                    url, tenantId);
            Thread.currentThread().interrupt();
            return;

        } catch (TimeoutException e) {
            log.warn("Failed to push notification url='{}', tenantId='{}': Request timed out.",
                    url, tenantId, e);
            // Retrying: https://docs.killbill.io/latest/notification_plugin.html#_retries
            throw new NotificationPluginApiRetryException(e);
        }

        if (!(response.getStatusCode() >= 200 && response.getStatusCode() < 300)) {
            throw new NotificationPluginApiRetryException(); //todo: would it be DEFAULT_RETRY_SCHEDULE or not?
        }
    }
}
