package edu.drexel.lapcounter.lapcounter.backend.Database.Units;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UnitsTest {

    Units units;

    @Before
    public void setUp() throws Exception {
        units = new Units();
    }

    @Test
    public void getUnitName_retrieves_setUnit_Name() {
        String name = "foo";
        units.setUnitName(name);

        assertEquals(name, units.getUnitName());
    }
}