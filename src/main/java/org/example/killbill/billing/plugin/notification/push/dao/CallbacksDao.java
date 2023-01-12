package org.example.killbill.billing.plugin.notification.push.dao;

import org.example.killbill.billing.plugin.notification.push.dao.gen.tables.records.PushnotificationsConfigRecord;
import org.killbill.billing.notification.plugin.api.ExtBusEventType;
import org.killbill.billing.plugin.dao.PluginDao;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.example.killbill.billing.plugin.notification.push.dao.gen.Tables.PUSHNOTIFICATIONS_CONFIG;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.using;

public class CallbacksDao extends PluginDao {

    protected static final String TENANT_ID = "tenant_id";
    protected static final String EVENT_TYPE = "event_type";
    protected static final String CALLBACK_URL = "callback_url";

    public CallbacksDao(final DataSource dataSource) throws SQLException {
        super(dataSource);
    }

    public Map<ExtBusEventType, List<String>> retrieveCallbacks(final UUID kbTenantId) throws SQLException {
        return execute(dataSource.getConnection(), conn -> using(conn, dialect, settings)
                .selectFrom(PUSHNOTIFICATIONS_CONFIG)
                .where(field(TENANT_ID).equal(kbTenantId.toString()))
                .fetchGroups(
                        record -> ExtBusEventType.valueOf(record.getEventType()),
                        PushnotificationsConfigRecord::getCallbackUrl
                )
        );
    }

    public List<String> retrieveCallbacks(final UUID kbTenantId, final ExtBusEventType eventType) throws SQLException {
        return execute(dataSource.getConnection(),
                conn -> using(conn, dialect, settings)
                        .selectFrom(PUSHNOTIFICATIONS_CONFIG)
                        .where(field(TENANT_ID).equal(kbTenantId.toString()))
                            .and(field(EVENT_TYPE).equal(eventType.toString()))
                        .fetch()
                        .map(PushnotificationsConfigRecord::getCallbackUrl)
        );
    }

    public void registerCallbacks(final UUID kbTenantId,
                                  final List<ExtBusEventType> eventTypes,
                                  final String callbackUrl) throws SQLException {
        execute(dataSource.getConnection(), conn -> {
            using(conn, dialect, settings).transaction(configuration -> {
                for (ExtBusEventType eventType : eventTypes) {
                    using(configuration)
                        .insertInto(PUSHNOTIFICATIONS_CONFIG,
                                field(TENANT_ID), field(EVENT_TYPE), field(CALLBACK_URL))
                        .values(kbTenantId.toString(), eventType.toString(), callbackUrl)
                        .execute();
                }
            });
            return null;
        });
    }

    public void clearCallbacks(final UUID kbTenantId) throws SQLException {
        execute(dataSource.getConnection(), conn -> {
            using(conn, dialect, settings).transaction(configuration -> using(configuration)
                    .deleteFrom(PUSHNOTIFICATIONS_CONFIG)
                    .where(field(TENANT_ID).equal(kbTenantId.toString()))
                    .execute()
            );
            return null;
        });
    }
}
