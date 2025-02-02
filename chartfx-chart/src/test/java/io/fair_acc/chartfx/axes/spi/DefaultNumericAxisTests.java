package io.fair_acc.chartfx.axes.spi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fair_acc.chartfx.axes.LogAxisType;

/**
 * @author rstein
 */
public class DefaultNumericAxisTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNumericAxisTests.class);

    @Test
    public void basicConstructorTests() {
        assertDoesNotThrow((ThrowingSupplier<DefaultNumericAxis>) DefaultNumericAxis::new);
        assertDoesNotThrow(() -> new DefaultNumericAxis(-10, +10, 1.0));
        assertDoesNotThrow(() -> new DefaultNumericAxis("axis name"));
        assertDoesNotThrow(() -> new DefaultNumericAxis("axis name", -10, +10, 1.0));
        assertDoesNotThrow(() -> new DefaultNumericAxis("axis name", "axis unit"));

        final DefaultNumericAxis axis1 = new DefaultNumericAxis(-10, +10, 1.0);
        assertEquals(-10, axis1.getMin());
        assertEquals(+10, axis1.getMax());
        assertEquals(+1, axis1.getTickUnit());

        final DefaultNumericAxis axis2 = new DefaultNumericAxis("axis name");
        assertEquals("axis name", axis2.getName());
        assertTrue(axis2.isAutoRanging(), "auto-ranging default 'true' if invalid or no ranges are set");

        final DefaultNumericAxis axis3 = new DefaultNumericAxis("axis name", -10, +10, 1.0);
        assertEquals("axis name", axis3.getName());
        assertEquals(-10, axis3.getMin());
        assertEquals(+10, axis3.getMax());
        assertEquals(+1, axis3.getTickUnit());
        assertFalse(axis3.isAutoRanging(), "auto-ranging default 'off' if ranges are set");

        final DefaultNumericAxis axis4 = new DefaultNumericAxis("axis name", "axis unit");
        assertEquals("axis name", axis4.getName());
        assertEquals("axis unit", axis4.getUnit());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.atInfo().log("basicConstructorTests() - done");
        }
    }

    /**
     * tests parameters not already covered in AbstractAxisParameterTests
     */
    @Test
    public void parameterTests() {
        final DefaultNumericAxis axis = new DefaultNumericAxis("axis name", -10, +10, 1.0);
        assertEquals("axis name", axis.getName());
        assertEquals(-10, axis.getMin());
        assertEquals(+10, axis.getMax());
        assertEquals(+1, axis.getTickUnit());
        assertFalse(axis.isAutoRanging(), "auto-ranging default 'off' if ranges are set");

        // tick unit for large enough axis that can accommodate the tick units
        assertEquals(1, axis.computePreferredTickUnit(1000));

        assertFalse(axis.isForceZeroInRange());
        axis.setForceZeroInRange(true);
        assertTrue(axis.isForceZeroInRange());
        axis.setForceZeroInRange(false);

        assertFalse(axis.isLogAxis());
        assertEquals(LogAxisType.LINEAR_SCALE, axis.getLogAxisType());
        axis.setLogAxis(true);
        axis.calculateMinorTickValues();
        assertTrue(axis.isLogAxis());
        assertEquals(LogAxisType.LOG10_SCALE, axis.getLogAxisType());
        axis.setMin(0.1);
        axis.updateCachedVariables();
        axis.layoutChildren();
        assertEquals(axis.getZeroPosition(), axis.getDisplayPosition(axis.getMin()));
        assertEquals(10, axis.getLogarithmBase());
        axis.setLogarithmBase(2);
        assertEquals(2, axis.getLogarithmBase());

        axis.setLogAxis(false);
        assertFalse(axis.isLogAxis());
        axis.updateCachedVariables();
        axis.calculateMinorTickValues();
    }
}
