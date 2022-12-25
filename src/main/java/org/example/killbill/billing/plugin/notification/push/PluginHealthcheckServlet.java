package org.example.killbill.billing.plugin.notification.push;

import lombok.RequiredArgsConstructor;
import org.jooby.Result;
import org.jooby.mvc.GET;
import org.jooby.mvc.Local;
import org.jooby.mvc.Path;
import org.killbill.billing.tenant.api.Tenant;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Singleton
@Path("/healthcheck")
public class PluginHealthcheckServlet extends org.killbill.billing.plugin.core.resources.PluginHealthcheck {

    private final PluginHealthcheck healthcheck;

    @GET
    public Result check(@Local @Named("killbill_tenant") final Optional<Tenant> tenant) {
        return check(healthcheck, tenant.orElse(null), null);
    }
}
