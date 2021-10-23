package com.walmart.bufferedtcp.configuration;

import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField;
import com.inductiveautomation.ignition.gateway.opcua.server.api.Device;
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext;
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord;
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceType;
import com.walmart.bufferedtcp.BufferedTcpDevice;
import com.walmart.bufferedtcp.configuration.settings.BufferedTcpDeviceSettings;

import javax.annotation.Nonnull;

public class BufferedTcpDeviceType extends DeviceType {

    public static final BufferedTcpDeviceType INSTANCE = new BufferedTcpDeviceType();

    public static final String TYPE_ID = "Buffered TCP Driver";

    public BufferedTcpDeviceType() {
        super(
                TYPE_ID,
                "BufferedTcpDevice.Meta.DisplayName",
                "BufferedTcpDevice.Meta.Description"
        );
    }

    @Override
    public RecordMeta<? extends PersistentRecord> getSettingsRecordType() {
        return BufferedTcpDeviceSettings.META;
    }

    @Override
    public ReferenceField<?> getSettingsRecordForeignKey() {
        return BufferedTcpDeviceSettings.DEVICE_SETTINGS;
    }

    @Nonnull
    @Override
    public Device createDevice(
            @Nonnull DeviceContext deviceContext,
            @Nonnull DeviceSettingsRecord deviceSettingsRecord) {

        BufferedTcpDeviceSettings settings = findProfileSettingsRecord(
                deviceContext.getGatewayContext(),
                deviceSettingsRecord
        );

        return new BufferedTcpDevice(this, deviceContext, settings);
    }
}
