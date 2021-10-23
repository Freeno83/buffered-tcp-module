package com.walmart.bufferedtcp;

import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext;
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceType;
import com.inductiveautomation.ignition.gateway.opcua.server.api.ManagedDevice;
import com.walmart.bufferedtcp.configuration.BufferedTcpDeviceType;
import com.walmart.bufferedtcp.configuration.ClientRunner;
import com.walmart.bufferedtcp.configuration.settings.BufferedTcpDeviceSettings;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilters;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
Author: Nick Robinson
Date: 10/21/21
Contents:
    TCP client driver which behaves like the Ignition TCP driver, but has buffering
    Since the Buffered TCP client is on the runner, it is called like: runner.client.openSocket()
    This is necessary since the lifecycle functions happen in this driver (startup, shutdown)
 */

public class BufferedTcpDevice extends ManagedDevice {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int SCAN_RATE_SEC = 1;
    private final DeviceContext deviceContext;
    public BufferedTcpDeviceSettings settings;

    public ClientRunner runner;
    private final SubscriptionModel subscriptionModel;

    public BufferedTcpDevice(
            DeviceType deviceType,
            DeviceContext deviceContext,
            BufferedTcpDeviceSettings settings
    ) {
        super(deviceType, deviceContext);
        this.deviceContext = deviceContext;
        this.settings = settings;

        subscriptionModel = new SubscriptionModel(deviceContext.getServer(), this);

        getLifecycleManager().addStartupTask(this::onStartup);
        getLifecycleManager().addShutdownTask(this::onShutdown);
    }

    @Nonnull
    @Override
    public String getStatus() {
        return runner.client.isConnected() ? "1/1 Connected" : "0/1 Connected";
    }

    private void onStartup() {
        subscriptionModel.startup();
        runner = new ClientRunner(settings);
        runner.client.openSocket();

        // Create a tag updater service
        deviceContext.getGatewayContext()
            .getExecutionManager()
            .registerAtFixedRate(
                    BufferedTcpDeviceType.TYPE_ID,
                    deviceContext.getName(),
                    runner,
                    SCAN_RATE_SEC,
                    TimeUnit.SECONDS
            );

        // Create a folder for the configured device
        UaFolderNode rootNode = new UaFolderNode(
            getNodeContext(),
            deviceContext.nodeId(getName()),
            deviceContext.qualifiedName(String.format("[%s]", getName())),
            new LocalizedText(String.format("[%s]", getName()))
        );

        // Add the folder note to the server
        getNodeManager().addNode(rootNode);

        // Add reference to the root "Devices" folder node
        rootNode.addReference(new Reference(
            rootNode.getNodeId(),
            Identifiers.Organizes,
            deviceContext.getRootNodeId().expanded(),
            Reference.Direction.INVERSE
        ));

        // Add message tag: folder is PORT which contains the message tag
        addMessageNode(rootNode, String.valueOf(settings.getPort()));

        // fire initial subscription creation
        List<DataItem> dataItems = deviceContext.getSubscriptionModel().getDataItems(getName());
        onDataItemsCreated(dataItems);
    }

    private void onShutdown() {
        runner.client.closeSocket();
        subscriptionModel.shutdown();

        deviceContext.getGatewayContext()
            .getExecutionManager()
            .unRegister(BufferedTcpDeviceType.TYPE_ID, deviceContext.getName());
    }

    private void addMessageNode(UaFolderNode rootNode, String name) {
        UaFolderNode folder = new UaFolderNode(
                getNodeContext(),
                deviceContext.nodeId(name),
                deviceContext.qualifiedName(name),
                new LocalizedText(name)
        );
        getNodeManager().addNode(folder);

        // addOrganizes is a helper method to an OPC UA "Organizes" references to a folder node
        rootNode.addOrganizes(folder);

        String messageName = "Message";
        UaVariableNode node = UaVariableNode.builder(getNodeContext())
                .setNodeId(deviceContext.nodeId(name + "/" + messageName))
                .setBrowseName(deviceContext.qualifiedName(messageName))
                .setDisplayName(new LocalizedText(messageName))
                .setDataType(BuiltinDataType.String.getNodeId())
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .setAccessLevel(AccessLevel.READ_ONLY)
                .setUserAccessLevel(AccessLevel.READ_ONLY)
                .build();

        // This tells the runner to keep track of message
        runner.addTrackedValue(messageName, "");

        // When the node is asked for its value it will point to the runner
        node.getFilterChain().addLast(
                AttributeFilters.getValue(
                        getAttributeContext -> runner.getTrackedValue(messageName)
                )
        );

        getNodeManager().addNode(node);
        folder.addOrganizes(node);
    }

    @Override
    public void onDataItemsCreated(List<DataItem> dataItems) {subscriptionModel.onDataItemsCreated((dataItems));}

    @Override
    public void onDataItemsModified(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsModified(dataItems);
    }

    @Override
    public void onDataItemsDeleted(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsDeleted(dataItems);
    }

    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
        subscriptionModel.onMonitoringModeChanged(monitoredItems);
    }
}
