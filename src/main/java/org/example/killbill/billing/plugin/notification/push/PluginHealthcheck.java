package org.example.killbill.billing.plugin.notification.push;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
            /*
             * connection.getMetaData().getConnection(): this is a workaround to acquire
             * the real connection object, because the wrapper object
             * (net.sf.log4jdbc.sql.jdbcapi.ConnectionSpy) throws a java.lang.AbstractMethodError
             * on method getSchema() at runtime.
             * */
            final String catalogName = connection.getMetaData().getConnection().getCatalog();
            // turns into "null" string in case of getSchema() returns null
            final String schemaName = String.valueOf(connection.getMetaData().getConnection().getSchema());

            try (ResultSet resultSet = connection.getMetaData().getTables(null, null,
                    Tables.PUSHNOTIFICATIONS_CONFIG.getName(), TYPE_TABLE)) {
                if (!resultSet.next()) {
                    return new HealthStatus(false, ImmutableMap.of(
                            "message", "Required tables are missing",
                            "details", ImmutableMap.of(
                                    "catalogName", catalogName,
                                    "schemaName", schemaName,
                                    "missingTables", ImmutableList.of(Tables.PUSHNOTIFICATIONS_CONFIG.getName())
                            )));
                }
            }
            return HealthStatus.healthy();

        } catch (SQLException e) {
            return HealthStatus.unHealthy(e.getMessage());
        }
    }
}
