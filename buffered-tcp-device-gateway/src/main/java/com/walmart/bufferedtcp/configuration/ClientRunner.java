package com.walmart.bufferedtcp.configuration;

import com.walmart.bufferedtcp.configuration.settings.BufferedTcpDeviceSettings;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 Author: Nick Robinson
 Date: 10/21/21
 Contents:
    OPC-UA manager uses this run function to get tag value updates
    This is where the initialized instance of the BufferedTcpClient is
 */

public class ClientRunner implements Runnable{

    public BufferedTcpClient client;
    private final Map<String, Variant> trackedValues = new ConcurrentHashMap<>();

    public ClientRunner(BufferedTcpDeviceSettings settings) {
        client = new BufferedTcpClient(
                settings.getIpAddress(),
                settings.getPort()
        );
    }

    public void addTrackedValue(String key, String initial) {
        trackedValues.put(key, new Variant(initial));
    }

    public DataValue getTrackedValue(String key) {
        Variant current = trackedValues.get(key);
        if (current != null) {
            return new DataValue(current);
        } else {
            return new DataValue(new Variant(null), StatusCode.BAD);
        }
    }

    @Override
    public void run() {
        for(Map.Entry<String, Variant> stringVariantEntry : trackedValues.entrySet()) {
            String key = stringVariantEntry.getKey();
            trackedValues.put(key, new Variant(client.readBuffer()));
        }
    }
}
