package org.example.killbill.billing.plugin.notification.push;

import org.example.killbill.billing.plugin.notification.push.dao.CallbacksDao;
import org.killbill.billing.osgi.api.Healthcheck;
import org.killbill.billing.osgi.api.OSGIPluginProperties;
import org.killbill.billing.osgi.libs.killbill.KillbillActivatorBase;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillEventDispatcher;
import org.killbill.billing.plugin.core.resources.jooby.PluginApp;
import org.killbill.billing.plugin.core.resources.jooby.PluginAppBuilder;
import org.osgi.framework.BundleContext;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.util.Hashtable;

public class PluginActivator extends KillbillActivatorBase {

    public static final String PLUGIN_NAME = "push-notifications";

    private OSGIKillbillEventDispatcher.OSGIKillbillEventHandler killbillEventHandler;

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);

        // Create an event listener
        killbillEventHandler = new KillbillEventListener(new CallbacksDao(dataSource.getDataSource()));

        // Expose a healthcheck
        final Healthcheck healthcheck = new PluginHealthcheck(dataSource.getDataSource());
        registerHealthcheck(context, healthcheck);

        // Register a servlet
        final PluginApp pluginApp = new PluginAppBuilder(PLUGIN_NAME, killbillAPI, dataSource, clock, configProperties)
                .withRouteClass(PluginServlet.class)
                .withService(new CallbacksDao(dataSource.getDataSource()))
                .withRouteClass(PluginHealthcheckServlet.class)
                .withService(healthcheck)
                .build();
        final HttpServlet httpServlet = PluginApp.createServlet(pluginApp);
        registerServlet(context, httpServlet);

        registerHandlers();
    }

    private void registerHealthcheck(final BundleContext context, final Healthcheck healthcheck) {
        final Hashtable<String, String> props = new Hashtable<>();
        props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
        registrar.registerService(context, Healthcheck.class, healthcheck, props);
    }

    private void registerServlet(final BundleContext context, final Servlet servlet) {
        final Hashtable<String, String> props = new Hashtable<>();
        props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
        registrar.registerService(context, Servlet.class, servlet, props);
    }

    private void registerHandlers() {
        dispatcher.registerEventHandlers((OSGIKillbillEventDispatcher.OSGIFrameworkEventHandler) () ->
                dispatcher.registerEventHandlers(killbillEventHandler)
        );
    }
}
