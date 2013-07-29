package me.smecsia.common.utils;

import org.apache.commons.collections.Transformer;

import java.util.*;

import static me.smecsia.common.utils.RandomUtil.randomInt;
import static org.apache.commons.collections.CollectionUtils.collect;

/**
 * @author Ilya Sadykov
 */
public class EnumUtil {

    private static final Map<Class<? extends Enum>, Set<String>> cache = new HashMap<Class<? extends Enum>, Set<String>>();

    /**
     * Checks that an String element is contained by the enumclass
     *
     * @return true if enumClass contains value
     */
    public static boolean enumContains(Class<? extends Enum> enumClass, String value) {
        if (!cache.containsKey(enumClass)) {
            Set<String> options = new HashSet<String>();
            for (Enum opt : enumClass.getEnumConstants()) {
                options.add(String.valueOf(opt));
            }
            cache.put(enumClass, options);
        }
        return cache.get(enumClass).contains(value);
    }

    /**
     * Get the enum value from its string value
     */
    public static <T extends Enum<T>> T fromString(Class<T> enumClass, String value) {
        if (!enumContains(enumClass, value)) {
            throw new IllegalArgumentException("Wrong value provided to the enum: " + enumClass + " : " + value + "!");
        }
        return Enum.valueOf(enumClass, value);
    }


    /**
     * Get the enum value from its ordinal value
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T fromOrdinal(Class<T> enumClass, Integer value) {
        Enum<T>[] values = (enumClass).getEnumConstants();
        if (value > values.length - 1 || value < 0) {
            throw new IllegalArgumentException("Wrong value provided for the enum " + enumClass + " : " + value + "!");
        }
        return (T) values[value];
    }


    /**
     * Converts list of strings into list of enum
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> Collection<Enum<T>> fromStringCollection(final Class<T> enumClass,
                                                                               Collection<String> values) {
        return collect(values, new Transformer() {
            @Override
            public Object transform(Object o) {
                return fromString(enumClass, o.toString());
            }
        });
    }

    /**
     * Converts collection of enums to collection of strings
     */
    @SuppressWarnings("unchecked")
    public static Collection<String> toStringCollection(Collection<Enum> values) {
        return collect(values, new Transformer() {
            @Override
            public Object transform(Object o) {
                return ((Enum) o).name();
            }
        });
    }


    /**
     * Returns random value from the enum
     */
    public static <T extends Enum<T>> T random(Class<T> enumClass) {
        int rand = randomInt(enumClass.getEnumConstants().length);
        return fromOrdinal(enumClass, rand);
    }
}
