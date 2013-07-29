package me.smecsia.common.utils;

import org.junit.Test;

import static me.smecsia.common.utils.EnumUtil.fromOrdinal;
import static me.smecsia.common.utils.EnumUtil.fromString;
import static me.smecsia.common.utils.EnumUtil.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ilya Sadykov
 */
public class EnumUtilTest {

    public static enum TestEnum {
        first,
        second
    }

    @Test
    public void testFromOrdinal() {
        assertEquals(TestEnum.first, fromOrdinal(TestEnum.class, 0));
        assertEquals(TestEnum.second, fromOrdinal(TestEnum.class, 1));
    }

    @Test
    public void testFromString() {
        assertEquals(TestEnum.first, fromString(TestEnum.class, "first"));
    }

    @Test
    public void testRandom() {
        for (int i = 0; i < 100; ++i) {
            assertTrue(EnumUtil.enumContains(TestEnum.class, random(TestEnum.class).name()));
        }
    }
}
