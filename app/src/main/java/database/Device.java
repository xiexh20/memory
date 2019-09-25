package database;

import java.util.Objects;

/**
 * local data type corresponding to database table: Devices
 */

public class Device extends BaseDataType{
    private String deviceID;    //unique ID of this android device
    private String deviceName;  //the name of this android device: composed of brand-product-service

    public Device(String deviceID, String deviceName) {
        this.deviceID = deviceID;
        this.deviceName = deviceName;
    }

    public Device(String deviceID) {
        this.deviceID = deviceID;
        deviceName = null;      // no device name
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String toString() {
        return "Device{" +
                "deviceID='" + deviceID + '\'' +
                ", deviceName='" + deviceName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return deviceID.equals(device.deviceID) &&
                Objects.equals(deviceName, device.deviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceID, deviceName);
    }
}
