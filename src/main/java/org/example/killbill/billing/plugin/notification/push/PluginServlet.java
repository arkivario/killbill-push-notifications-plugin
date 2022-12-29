package org.example.killbill.billing.plugin.notification.push;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.killbill.billing.plugin.notification.push.dao.CallbacksDao;
import org.example.killbill.billing.plugin.notification.push.dto.PostCallbacksDto;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.*;
import org.killbill.billing.plugin.api.PluginTenantContext;
import org.killbill.billing.tenant.api.Tenant;
import org.killbill.billing.tenant.api.TenantApiException;
import org.killbill.billing.tenant.api.TenantKV;
import org.killbill.billing.tenant.api.TenantUserApi;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Singleton
@Path("/")
public class PluginServlet {

    private static final Result NO_TENANT_ERROR = Results.with(
            Collections.singletonMap(
                    "message", "Make sure to set the X-Killbill-ApiKey and X-Killbill-ApiSecret headers"
            ),
            Status.UNAUTHORIZED
    );
    private static final Result CALLBACKS_ALREADY_SET_USING_KILLBILL_ERROR = Results.with(
            Collections.singletonMap(
                    "message", "Callbacks already set using KillBill's push notification mechanism. Clear it before using this plugin."
            ),
            Status.BAD_REQUEST
    );

    private final TenantUserApi tenantUserApi;
    private final CallbacksDao dao;

    @GET
    public void ping(@Local @Named("killbill_tenant") final Optional<Tenant> tenant) {
        log.info("Hello from KillBill Another push notification plugin");

        if(tenant != null && tenant.isPresent() ) {
            log.info("tenant is available");
            Tenant t1 = tenant.get();
            log.info("tenant id:"+t1.getId());
        }
        else {
            log.info("tenant is not available");
        }
    }

    @POST
    public Result registerCallbacks(@Local @Named("killbill_tenant") final Tenant tenant,
                                    @Body final PostCallbacksDto body) {
        if (Objects.isNull(tenant)) {
            return NO_TENANT_ERROR;
        }

        try {
            List<String> nativeKillbillCallbacks = tenantUserApi.getTenantValuesForKey(
                    String.valueOf(TenantKV.TenantKey.PUSH_NOTIFICATION_CB),
                    new PluginTenantContext(null, tenant.getId())
            );
            if (!nativeKillbillCallbacks.isEmpty())
                return CALLBACKS_ALREADY_SET_USING_KILLBILL_ERROR;
        } catch (TenantApiException e) {
            log.warn("Tenant API call failed.", e);
            return Results.with(Collections.singletonMap("message", e.getMessage()), Status.SERVER_ERROR);
        }



        return Results.with(Status.CREATED);
    }
    @DELETE
    public Result deleteCallback(@Local @Named("killbill_tenant") final Tenant tenant) {
        return Results.with(Status.NO_CONTENT);
    }


}
