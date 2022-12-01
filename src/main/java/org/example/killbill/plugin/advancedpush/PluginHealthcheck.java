package org.example.killbill.plugin.advancedpush;

import org.killbill.billing.osgi.api.Healthcheck;
import org.killbill.billing.tenant.api.Tenant;

import javax.annotation.Nullable;
import java.util.Map;

public class PluginHealthcheck implements Healthcheck {

    @Override
    public HealthStatus getHealthStatus(@Nullable final Tenant tenant, @Nullable final Map properties) {
        return HealthStatus.healthy(); //todo implement
    }
}
