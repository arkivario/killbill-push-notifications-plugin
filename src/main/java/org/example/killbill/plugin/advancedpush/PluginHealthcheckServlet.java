package org.example.killbill.plugin.advancedpush;

import org.jooby.Result;
import org.jooby.mvc.GET;
import org.jooby.mvc.Local;
import org.jooby.mvc.Path;
import org.killbill.billing.tenant.api.Tenant;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
@Path("/healthcheck")
public class PluginHealthcheckServlet extends org.killbill.billing.plugin.core.resources.PluginHealthcheck {

    private final PluginHealthcheck healthcheck;

    public PluginHealthcheckServlet(final PluginHealthcheck healthcheck) {
        this.healthcheck = healthcheck;
    }

    @GET
    public Result check(@Local @Named("killbill_tenant") final Optional<Tenant> tenant) {
        return check(healthcheck, tenant.orElse(null), null);
    }
}
