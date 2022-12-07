package org.example.killbill.plugin.advancedpush.persist.dao;

import org.example.killbill.plugin.advancedpush.persist.domain.tables.records.AdvancedpushConfigRecord;
import org.killbill.billing.notification.plugin.api.ExtBusEventType;
import org.killbill.billing.plugin.dao.PluginDao;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.example.killbill.plugin.advancedpush.persist.domain.Tables.ADVANCEDPUSH_CONFIG;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.using;

public class CallbacksDao extends PluginDao {

    protected static final String TENANT_ID = "tenant_id";
    protected static final String EVENT_TYPE = "event_type";
    protected static final String CALLBACK_URL = "callback_url";

    public CallbacksDao(final DataSource dataSource) throws SQLException {
        super(dataSource);
    }

    public List<String> retrieveCallbacks(final UUID kbTenantId, final ExtBusEventType eventType) throws SQLException {
        return execute(dataSource.getConnection(),
                conn -> using(conn, dialect, settings)
                        .selectFrom(ADVANCEDPUSH_CONFIG)
                        .where(field(TENANT_ID).equal(kbTenantId.toString()))
                            .and(field(EVENT_TYPE).equal(eventType.toString()))
                        .fetch()
                        .map(AdvancedpushConfigRecord::getCallbackUrl)
        );
    }

    public void registerCallback(final UUID kbTenantId,
                                 final ExtBusEventType eventType,
                                 final String callbackUrl) throws SQLException {
        execute(dataSource.getConnection(), conn -> {
            using(conn, dialect, settings).transaction(configuration -> using(configuration)
                    .insertInto(ADVANCEDPUSH_CONFIG, field(TENANT_ID), field(EVENT_TYPE), field(CALLBACK_URL))
                    .values(kbTenantId.toString(), eventType.toString(), callbackUrl)
                    .execute()
            );
            return null;
        });
    }
}
