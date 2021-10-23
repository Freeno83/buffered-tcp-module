package com.walmart.bufferedtcp;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.opcua.server.api.AbstractDeviceModuleHook;
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceType;
import com.walmart.bufferedtcp.configuration.BufferedTcpDeviceType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

import static org.python.google.common.collect.Lists.newArrayList;

public class ModuleHook extends AbstractDeviceModuleHook {

    @Override
    public void setup(@NotNull GatewayContext context) {
        super.setup(context);
        BundleUtil.get().addBundle(BufferedTcpDevice.class);
    }

    @Override
    public void startup(@NotNull LicenseState activationState) {super.startup(activationState);}

    @Override
    public void shutdown() {
        super.shutdown();
        BundleUtil.get().removeBundle(BufferedTcpDevice.class);
    }

    @Nonnull
    @Override
    protected List<DeviceType> getDeviceTypes() {

        return newArrayList(BufferedTcpDeviceType.INSTANCE);
    }

    @Override
    public boolean isFreeModule() {
        return true;
    }
}
