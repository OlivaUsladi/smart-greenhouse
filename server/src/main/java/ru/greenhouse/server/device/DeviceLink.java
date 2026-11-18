package ru.greenhouse.server.device;

import java.io.IOException;

public interface DeviceLink {

    void start(DeviceListener listener) throws IOException;

    void stop();
}
