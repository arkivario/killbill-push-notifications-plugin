package org.example.killbill.billing.plugin.notification.push;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooby.mvc.GET;
import org.jooby.mvc.Local;
import org.jooby.mvc.Path;
import org.killbill.billing.tenant.api.Tenant;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;

@Slf4j
@NoArgsConstructor
@Singleton
@Path("/")
public class PluginServlet {

    @GET
    public void ping(@Local @Named("killbill_tenant") final Optional<Tenant> tenant) {
        log.info("Hello from KillBill Another push notification plugin");

        if(tenant != null && tenant.isPresent() ) {
            log.info("tenant is available");
            Tenant t1 = tenant.get();
            log.info("tenant id:"+t1.getId());
        }
        else {
            log.info("tenant is not available");
        }
    }
}
