package com.bytedance.tools.codelocator.listener;

import com.android.ddmlib.IDevice;
import com.bytedance.tools.codelocator.device.Device;
import com.bytedance.tools.codelocator.device.DeviceManager;
import com.bytedance.tools.codelocator.model.CodeLocatorUserConfig;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

/**
 * Created by liujian.android on 2025/8/26
 * @author liujian.android@bytedance.com
 */
public class CodeLocatorProjectManagerListener implements ProjectManagerListener {

    @Override
    public void projectClosed(@NotNull Project project) {
        ProjectManagerListener.super.projectClosed(project);
        final Device currentDevice = DeviceManager.getCurrentDevice(project, true);
        if (currentDevice != null && currentDevice.getDevice() != null) {
            final String serialNumber = currentDevice.getDevice().getSerialNumber();
            CodeLocatorUserConfig.loadConfig().setLastDevice(serialNumber);
            DeviceManager.onProjectClose(project);
        } else if (currentDevice == null) {
            final IDevice device = DeviceManager.onProjectClose(project);
            if (device != null) {
                CodeLocatorUserConfig.loadConfig().setLastDevice(device.getSerialNumber());
            }
        } else {
            DeviceManager.onProjectClose(project);
        }
        CodeLocatorUserConfig.updateConfig(CodeLocatorUserConfig.loadConfig());
    }
}