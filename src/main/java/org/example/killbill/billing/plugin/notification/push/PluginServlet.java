package org.example.killbill.billing.plugin.notification.push;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.killbill.billing.plugin.notification.push.dao.CallbacksDao;
import org.example.killbill.billing.plugin.notification.push.dto.PostCallbacksDto;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.*;
import org.killbill.billing.notification.plugin.api.ExtBusEventType;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.plugin.api.PluginTenantContext;
import org.killbill.billing.tenant.api.Tenant;
import org.killbill.billing.tenant.api.TenantApiException;
import org.killbill.billing.tenant.api.TenantKV;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/*
* todo: deal with security;
* todo: should I use Optional<Tenant>?
* */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Singleton
@Path("/")
public class PluginServlet {

    private static final Result NO_TENANT_401 = Results.with(
        Collections.singletonMap(
            "message", "Make sure to set the X-Killbill-ApiKey and X-Killbill-ApiSecret headers"
        ),
        Status.UNAUTHORIZED //todo: 403??
    );
    private static final Result CALLBACKS_ALREADY_SET_USING_KILLBILL_400 = Results.with(
        Collections.singletonMap(
            "message",
            "Callbacks already set using KillBill's push notification mechanism. Clear it before using this plugin."
        ),
        Status.BAD_REQUEST //todo: another 4xx code??
    );

    private final OSGIKillbillAPI killbillAPI;
    private final CallbacksDao dao;

    @GET
    public Result getAll(@Local @Named("killbill_tenant") final Tenant tenant) {
        if (Objects.isNull(tenant)) {
            return Results.ok(); //todo: temporary, need return all callbacks for all tenants
        }

        try {
            Map<ExtBusEventType, List<String>> result = dao.retrieveCallbacks(tenant.getId());
            return Results.ok(result);
        } catch (SQLException e) {
            log.warn("Failed to retrieve callbacks for tenant: {}", tenant.getId(), e);
            return Results.with(Collections.singletonMap("message", e.getMessage()), Status.SERVER_ERROR);
        }
    }

    @POST
    public Result postCallbacks(@Local @Named("killbill_tenant") final Tenant tenant,
                                @Body final PostCallbacksDto body) {
        if (Objects.isNull(tenant)) {
            return NO_TENANT_401;
        }

        try {
            List<String> killbillCallbacks = killbillAPI.getTenantUserApi().getTenantValuesForKey(
                    String.valueOf(TenantKV.TenantKey.PUSH_NOTIFICATION_CB),
                    new PluginTenantContext(null, tenant.getId())
            );
            if (!Objects.isNull(killbillCallbacks) && !killbillCallbacks.isEmpty())
                return CALLBACKS_ALREADY_SET_USING_KILLBILL_400;
        } catch (TenantApiException e) {
            /*
            * Actually is never thrown when calling getTenantValuesForKey with KillBill's default implementation
            * org.killbill.billing.tenant.api.user.DefaultTenantUserApi
            * */
            log.warn("Tenant API call failed.", e);
            return Results.with(Collections.singletonMap("message", e.getMessage()), Status.SERVER_ERROR);
        }

        try {
            dao.registerCallbacks(tenant.getId(), body.getEventTypes(), body.getCallbackUrl());
        } catch (SQLException e) {
            log.warn("Unable to save configuration.", e);
            return Results.with(Collections.singletonMap("message", e.getMessage()), Status.SERVER_ERROR);
        }

        return Results.with(Status.CREATED);
    }
    @DELETE
    public Result deleteCallback(@Local @Named("killbill_tenant") final Tenant tenant) {
        if (Objects.isNull(tenant)) {
            return NO_TENANT_401;
        }

        try {
            dao.clearCallbacks(tenant.getId());
        } catch (SQLException e) {
            log.warn("Unable to clear callbacks for tenant {}.", tenant.getId(), e);
            return Results.with(Collections.singletonMap("message", e.getMessage()), Status.SERVER_ERROR);
        }

        return Results.with(Status.NO_CONTENT);
    }
}
