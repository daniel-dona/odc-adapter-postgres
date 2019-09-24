package de.fraunhofer.fokus.ids.services.sqlite;

import de.fraunhofer.fokus.ids.services.util.Constants;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.serviceproxy.ServiceBinder;

public class SQLiteServiceVerticle extends AbstractVerticle {

    private Logger LOGGER = LoggerFactory.getLogger(de.fraunhofer.fokus.ids.services.database.DatabaseServiceVerticle.class.getName());

    @Override
    public void start(Future<Void> startFuture) {

        ConfigStoreOptions confStore = new ConfigStoreOptions()
                .setType("env");

        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(confStore);

        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

        retriever.getConfig(ar -> {
            if (ar.succeeded()) {
                JsonObject env = ar.result();
                JsonObject config = new JsonObject()
                        .put("url", "jdbc:sqlite:"+env.getString("REPOSITORY")+"db")
                        .put("driver_class", "org.sqlite.jdbcDriver")
                        .put("max_pool_size", 30);
                SQLClient jdbc = JDBCClient.createShared(vertx, config);
                SQLiteService.create(jdbc, ready -> {
                    if (ready.succeeded()) {
                        ServiceBinder binder = new ServiceBinder(vertx);
                        binder
                                .setAddress(Constants.SQLITE_SERVICE)
                                .register(SQLiteService.class, ready.result());
                        startFuture.complete();
                    } else {
                        startFuture.fail(ready.cause());
                    }
                });
            } else {
                LOGGER.error("Config could not be retrieved.");
            }
        });
    }

}
