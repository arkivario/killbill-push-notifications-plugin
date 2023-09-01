package org.example.killbill.billing.plugin.notification.push.dao;

import org.killbill.billing.platform.test.PlatformDBTestingHelper;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.sql.SQLException;

public class CallbacksDaoTest {

    @BeforeSuite(groups = "slow")
    public void setUpDB() throws SQLException, IOException {
        PlatformDBTestingHelper.get().start();
    }

    @BeforeMethod(groups = "slow")
    public void resetDB() throws IOException {
        PlatformDBTestingHelper.get().getInstance().cleanupAllTables();
    }

    @AfterSuite(groups = "slow")
    public void tearDownDB() throws IOException {
        PlatformDBTestingHelper.get().getInstance().stop();
    }
}
