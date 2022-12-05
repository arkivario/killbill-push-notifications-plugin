package org.example.killbill.plugin.advancedpush;

import lombok.RequiredArgsConstructor;
import org.example.killbill.plugin.advancedpush.persist.domain.Tables;
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

    private final DataSource dataSource;

    @Override
    public HealthStatus getHealthStatus(@Nullable final Tenant tenant, @Nullable final Map properties) {
        try (Connection connection = dataSource.getConnection()) {
            ResultSet resultSet = connection.getMetaData().getTables(null, null,
                            Tables.ADVANCEDPUSH_CONFIG.getName(),
                            new String[]{"TABLE"});

            if (!resultSet.next()) {
                resultSet.close();
                return HealthStatus.unHealthy(String.format(
                        "Current database schema '%s' does not contain the required table '%s'",
                        connection.getSchema(), Tables.ADVANCEDPUSH_CONFIG.getName())
                );
            }

            resultSet.close();
            return HealthStatus.healthy();

        } catch (SQLException e) {
            return HealthStatus.unHealthy(e.getMessage());
        }
    }
}
