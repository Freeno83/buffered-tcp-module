package com.walmart.bufferedtcp.configuration.settings;

import com.inductiveautomation.ignition.gateway.localdb.persistence.*;
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord;
import simpleorm.dataset.SFieldFlags;

/**
 Author: Nick Robinson
 Date: 10/21/21
 Contents:
    Definition of the settings record (settings page fields)
    Data is stored in two separate tables:
        ADDRESS, PORT: BUFFEREDTCPDEVICESETTINGS
        NAME, DESCRIPTION, ENABLED: DEVICESETTINGS
 */

public class BufferedTcpDeviceSettings extends PersistentRecord{

    public static final RecordMeta<BufferedTcpDeviceSettings> META
            = new RecordMeta<>(
            BufferedTcpDeviceSettings.class, "BufferedTcpDeviceSettings"
    );

    // Reference to parent DeviceSettingsRecord
    public static final LongField DEVICE_SETTINGS_ID =
            new LongField(META, "DeviceSettingsId", SFieldFlags.SPRIMARY_KEY);

    // Link device settings record to the device record
    public static final ReferenceField<DeviceSettingsRecord> DEVICE_SETTINGS = new ReferenceField<>(
            META,
            DeviceSettingsRecord.META,
            "DeviceSettings",
            DEVICE_SETTINGS_ID
    );

    // Field Definitions
    public static final StringField IpAddress = new StringField(META, "Address", SFieldFlags.SMANDATORY);
    public static final IntField Port = new IntField(META, "Port", SFieldFlags.SMANDATORY);

    // User Interface Layout
    static final Category Configuration = new Category("BufferedTcpDeviceSettings.ConfigCategory", 1001)
            .include(IpAddress, Port);

    static {DEVICE_SETTINGS.getFormMeta().setVisible(false);}

    // Access Methods
    public String getIpAddress() {return getString(IpAddress);}
    public int getPort() {return getInt(Port);}

    @Override
    public RecordMeta<?> getMeta() {return META;}
}
