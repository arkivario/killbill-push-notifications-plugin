package org.example.killbill.plugin.advancedpush;

import lombok.extern.slf4j.Slf4j;
import org.killbill.billing.notification.plugin.api.ExtBusEvent;
import org.killbill.billing.notification.plugin.api.NotificationPluginApiRetryException;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillEventDispatcher;
import org.killbill.billing.plugin.util.http.HttpClient;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
public class KillbillEventListener implements OSGIKillbillEventDispatcher.OSGIKillbillEventHandler {

    @Override
    public void handleKillbillEvent(final ExtBusEvent killbillEvent) { //todo implement
        log.info("Received event {} for object id {} of type {}",
                killbillEvent.getEventType(),
                killbillEvent.getObjectId(),
                killbillEvent.getObjectType());

        try (HttpClient httpClient = new HttpClient(null, null, null,null, null, false)){


        } catch (GeneralSecurityException e) {
            /*
            * GeneralSecurityException actually is never thrown while instantiating an HttpClient.
            * See https://github.com/killbill/killbill-plugin-framework-java/blob/ec3f76e982201bc0f7d3cf159a60db7cda22347b/src/main/java/org/killbill/billing/plugin/util/http/HttpClient.java#L143
            * */
            log.warn("A security exception occurred while creating http client", e);
            throw new NotificationPluginApiRetryException(e);

        } catch (IOException e) {
            /*
            * IOException actually is never thrown while closing an HttpClient.
            * See https://github.com/AsyncHttpClient/async-http-client/blob/7a370af58dc8895a27a14d0a81af2a3b91930651/client/src/main/java/org/asynchttpclient/DefaultAsyncHttpClient.java#L117
            * */
            log.warn("An I/O error occurred while closing http client", e);
        }

    }
}
