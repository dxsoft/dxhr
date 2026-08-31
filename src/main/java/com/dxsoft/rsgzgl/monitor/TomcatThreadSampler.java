package com.dxsoft.rsgzgl.monitor;

import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.boot.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class TomcatThreadSampler {

    private volatile ProtocolHandler handler;

    @EventListener
    void onReady(WebServerInitializedEvent event) {
        if (event.getWebServer() instanceof TomcatWebServer tomcat) {
            handler = tomcat.getTomcat().getConnector().getProtocolHandler();
        }
    }

    int[] snapshot() {
        ProtocolHandler current = handler;
        if (!(current instanceof AbstractProtocol<?> protocol)) {
            return new int[] {0, 0, 0};
        }
        int max = protocol.getMaxThreads();
        if (protocol.getExecutor() instanceof ThreadPoolExecutor pool) {
            return new int[] {pool.getActiveCount(), pool.getPoolSize(), max};
        }
        return new int[] {0, 0, max};
    }
}
