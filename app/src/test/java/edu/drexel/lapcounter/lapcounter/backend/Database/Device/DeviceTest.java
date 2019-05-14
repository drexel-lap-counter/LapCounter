package edu.drexel.lapcounter.lapcounter.backend.Database.Device;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DeviceTest {

    Device device;

    @Before
    public void setUp() throws Exception {
        device = new Device();
    }

    @Test
    public void getName_retrieves_setName() {
        String name = "foo";
        device.setName(name);

        assertEquals(device.getName(), name);
    }

    @Test
    public void getMacAddress_retrieves_setMacAssress() {
        String mac = "bar";
        device.setMacAddress(mac);

        assertEquals(device.getMacAddress(), mac);
    }

    @Test
    public void getThreshold_retrieves_setThreshold() {
        double threshold = 31.04;
        device.setThreshold(threshold);

        assertEquals(threshold, device.getThreshold(), .25);
    }

    @Test
    public void equals_matches_same_object() {
        assertTrue(device.equals(device));
    }

    @Test
    public void equals_does_not_match_null() {
        assertFalse(device.equals(null));
    }

    @Test
    public void equals_does_not_match_other_object() {
        assertFalse(device.equals("new String"));
    }

    @Test
    public void equals_matches_if_mac_and_name_match() {
        String name = "foo";
        String mac = "bar";
        device.setName(name);
        device.setMacAddress(mac);
        Device device2 = new Device();
        device2.setName(name);
        device2.setMacAddress(mac);

        assertTrue(device.equals(device2));
    }

    @Test
    public void hashCode_returns_unique_int() {
        String name = "foo";
        String mac = "bar";
        String mac2 = "baz";
        device.setName(name);
        device.setMacAddress(mac);
        Device device2 = new Device();
        device2.setName(name);
        device2.setMacAddress(mac2);

        assertNotEquals(device.hashCode(), device2.hashCode());
    }
}