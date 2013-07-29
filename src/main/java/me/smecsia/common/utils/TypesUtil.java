package me.smecsia.common.utils;

import java.util.*;

/**
 * User: smecsia
 */
public class TypesUtil {

    /**
     * Checks if the fieldType is integer
     *
     * @param type - java fieldType
     * @return true if the given fieldType is integer
     */
    public static boolean isInt(Class<?> type) {
        return Integer.class.isAssignableFrom(type) || (type.isPrimitive() && type.toString().equals("int"));

    }

    /**
     * Checks if the fieldType is double
     *
     * @param type - java fieldType
     * @return true if the given fieldType is double
     */
    public static boolean isDouble(Class<?> type) {
        return Double.class.isAssignableFrom(type) || (type.isPrimitive() && type.toString().equals("double"));
    }

    /**
     * Checks if the fieldType is float
     *
     * @param type - java fieldType
     * @return true if the given fieldType is float
     */
    public static boolean isFloat(Class<?> type) {
        return Float.class.isAssignableFrom(type) || (type.isPrimitive() && type.toString().equals("float"));
    }

    /**
     * Checks if the fieldType is Boolean
     *
     * @param type - java fieldType
     * @return true if the given fieldType is boolean
     */
    public static boolean isBoolean(Class<?> type) {
        return Boolean.class.isAssignableFrom(type) || (type.isPrimitive() && type.toString().equals("boolean"));
    }

    /**
     * Checks if the fieldType is long
     *
     * @param type - java fieldType
     * @return true if the given fieldType is long
     */
    public static boolean isString(Class<?> type) {
        return String.class.isAssignableFrom(type);
    }

    /**
     * Checks if the fieldType is long
     *
     * @param type - java fieldType
     * @return true if the given fieldType is long
     */
    public static boolean isLong(Class<?> type) {
        return Long.class.isAssignableFrom(type) || (type.isPrimitive() && type.toString().equals("long"));
    }

    /**
     * Returns the TYPE that is a generic for the ClassType
     *
     * @param objectType
     * @return
     */
    public static Class<?> getGenericType(Class<?> objectType) {
        if (isInt(objectType)) {
            return Integer.TYPE;
        } else if (isBoolean(objectType)) {
            return Boolean.TYPE;
        } else if (isDouble(objectType)) {
            return Double.TYPE;
        } else if (isLong(objectType)) {
            return Long.TYPE;
        } else if (isFloat(objectType)) {
            return Float.TYPE;
        }
        return objectType;
    }

    /**
     * Instantiate the collection by its class
     *
     * @param collClass
     * @return
     */
    public static Collection instantiateCollection(Class<? extends Collection> collClass) {
        if (List.class.isAssignableFrom(collClass)) {
            return new ArrayList();
        } else if (Set.class.isAssignableFrom(collClass)) {
            return new HashSet();
        } else {
            throw new RuntimeException("Cannot instantiate collection of a class: " + collClass + ": Not supported!");
        }
    }
}
