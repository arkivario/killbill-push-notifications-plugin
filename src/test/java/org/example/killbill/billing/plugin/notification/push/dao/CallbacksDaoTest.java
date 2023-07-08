package org.example.killbill.billing.plugin.notification.push.dao;

import org.killbill.billing.platform.test.PlatformDBTestingHelper;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.sql.SQLException;

/*
* todo: generate ddl script using Liquibase changelog,
*  place it at target/generated-test-resources/org/killbill/billing/beatrix/ddl-<dbEngineName>.sql
*  (see org.killbill.billing.platform.test.PlatformDBTestingHelper.executePostStartupScripts())
* */
public class CallbacksDaoTest {

    @BeforeSuite(groups = "dbi")
    public void setUpDB() throws SQLException, IOException {
        PlatformDBTestingHelper.get().start();
    }

    @BeforeMethod(groups = "dbi")
    public void resetDB() throws IOException {
        PlatformDBTestingHelper.get().getInstance().cleanupAllTables();
    }

    @AfterSuite(groups = "dbi")
    public void tearDownDB() throws IOException {
        PlatformDBTestingHelper.get().getInstance().stop();
    }
}
