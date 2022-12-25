package org.example.killbill.billing.plugin.notification.push;

import lombok.RequiredArgsConstructor;
import org.example.killbill.billing.plugin.notification.push.dao.gen.Tables;
import org.killbill.billing.osgi.api.Healthcheck;
import org.killbill.billing.tenant.api.Tenant;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@RequiredArgsConstructor
public class PluginHealthcheck implements Healthcheck {

    private static final String[] TYPE_TABLE = new String[]{"TABLE"};

    private final DataSource dataSource;

    @Override
    public HealthStatus getHealthStatus(@Nullable final Tenant tenant, @Nullable final Map properties) {
        try (Connection connection = dataSource.getConnection()) {

            try (ResultSet resultSet = connection.getMetaData().getTables(null, null,
                    Tables.ANOTHERPUSH_CONFIG.getName(), TYPE_TABLE)) {
                if (!resultSet.next()) {
                    return HealthStatus.unHealthy(String.format(
                            "Current database schema '%s' does not contain the required table '%s'",
                            connection.getSchema(), Tables.ANOTHERPUSH_CONFIG.getName())
                    );
                }
            }
            return HealthStatus.healthy();

        } catch (SQLException e) {
            return HealthStatus.unHealthy(e.getMessage());
        }
    }
}
