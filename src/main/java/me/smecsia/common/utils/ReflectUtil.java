package me.smecsia.common.utils;

import java.lang.reflect.*;
import java.util.*;

import static me.smecsia.common.utils.TypesUtil.instantiateCollection;
import static org.apache.commons.lang.ClassUtils.getAllInterfaces;


/**
 * @author: Ilya Sadykov
 */
public class ReflectUtil {

    public static interface ExceptionHandler {
        Object handleException(Throwable e) throws Throwable;
    }

    /**
     * Create a proxy object implementing some interface from a specified class loader
     */
    @SuppressWarnings("unchecked")
    public static <C, I extends C> I classLoaderProxy(ClassLoader cl, C object, Class<I> classInterface)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return (I) Proxy.newProxyInstance(classInterface.getClassLoader(), new Class[]{classInterface}, new ThroughClassLoaderProxyHandler(object, cl));
    }


    /**
     * An invocation handler that passes on any calls made to it directly to its delegate.
     * This is useful to handle identical classes loaded in different classloaders - the
     * VM treats them as different classes, but they have identical signatures.
     * <p/>
     * Note this is using class.getMethod, which will only work on public methods.
     * Note this is using the arguments that are of primitive types/ types from java.lang / classes implementing one
     * interface exactly
     */
    private static class ThroughClassLoaderProxyHandler implements InvocationHandler {
        private final Object delegate;
        private final ClassLoader guestClassLoader;
        private final ClassLoader hostClassLoader;
        private final ExceptionHandler exceptionHandler;

        public ThroughClassLoaderProxyHandler(Object delegate, ClassLoader classLoader, ExceptionHandler handler) {
            this.delegate = delegate;
            this.guestClassLoader = classLoader;
            this.hostClassLoader = getClass().getClassLoader();
            this.exceptionHandler = handler;
        }

        public ThroughClassLoaderProxyHandler(Object delegate, ClassLoader classLoader) {
            this(delegate, classLoader, null);
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            try {
                Method delegateMethod = delegate.getClass().getMethod(method.getName(),
                        wrapTypesForClassLoader(guestClassLoader, method.getParameterTypes()));
                return wrapObjectForClassLoader(
                        hostClassLoader,
                        guestClassLoader,
                        delegateMethod.invoke(delegate,
                                wrapArgsForClassLoader(guestClassLoader, hostClassLoader, delegateMethod, args)
                        ),
                        null
                );
            } catch (Exception e) {
                return throwRootExceptionFromClassLoader(hostClassLoader, e, exceptionHandler);
            }
        }
    }

    /**
     * Collect all interfaces of a class
     */
    private static Set<Class> collectAllClassInterfaces(final Class objClazz) {
        Set<Class> interfaces = new HashSet<Class>();
        Class clazz = objClazz;
        // search through superclasses
        while (clazz != null) {
            interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
            clazz = clazz.getSuperclass();
        }
        return interfaces;
    }

    /**
     * Throw an exception which is a root cause of the problem, creating the same instance in the host class loader
     */
    @SuppressWarnings("unchecked")
    private static Object throwRootExceptionFromClassLoader(ClassLoader cl, Exception e, ExceptionHandler handler)
            throws Throwable {
        Throwable rootE = getRootException(e);
        Class<? extends Throwable> eClass = (Class<? extends Throwable>) cl.loadClass(rootE.getClass().getName());
        Throwable resultException = e;
        try {
            resultException = eClass.getConstructor(Throwable.class).newInstance(rootE);
        } catch (NoSuchMethodException ignored) {
            try {
                resultException = (eClass.getConstructor(String.class).newInstance(rootE.getMessage()));
            } catch (NoSuchMethodException ignored2) {
                resultException = eClass.newInstance();
            }
        }
        if (handler != null) {
            return handler.handleException(resultException);
        }
        resultException.setStackTrace(rootE.getStackTrace());
        throw resultException;
    }

    /**
     * Retrieve the root exception through getCause()
     */
    private static Throwable getRootException(Exception e) {
        Throwable rootE = e;
        while (rootE.getCause() != null) {
            rootE = rootE.getCause();
        }
        return rootE;
    }

    /**
     * Load type's class from a specified classloader.
     * If a type is basic or from basic "java" package return itself.
     */
    private static Class<?> classLoaderType(ClassLoader cl, Class<?> type) throws ClassNotFoundException {
        return isBasicJavaType(type) ? type : cl.loadClass(type.getName());
    }

    /**
     * returns true if a type is a basic java type
     */
    private static boolean isBasicJavaType(Class<?> type) {
        return type.getPackage() == null || type.getPackage().getName().startsWith("java");
    }

    /**
     * Get parameter types list from a class loader.
     */
    private static Class<?>[] wrapTypesForClassLoader(ClassLoader cl, Class<?>[] types)
            throws ClassNotFoundException {
        List<Class<?>> res = new ArrayList<Class<?>>();
        if (types != null) {
            for (Class<?> type : types) {
                Class<?> clazz = classLoaderType(cl, type);
                res.add(clazz);
            }
        }
        return res.toArray(new Class<?>[res.size()]);
    }

    /**
     * Wrap args for a class loader (if necessary)
     *
     * @param hostCL  specifies a host class loader (from which argument is accessible)
     * @param method  specifies a method that should be used for wrapping
     * @param guestCL specifies a guest class loader (which is supposed to be the actual consumer of the arguments)
     */
    private static <T> Object[] wrapArgsForClassLoader(ClassLoader hostCL, ClassLoader guestCL, Method method, Object[] args)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Object> wrappedArgs = new ArrayList<Object>();
        if (args != null) {
            if (method.getParameterTypes().length != args.length) {
                throw new RuntimeException("Cannot wrap arguments: method " + method.getName() + " is expecting " +
                        method.getParameterTypes().length + " args, but only " + args.length + " provided!");
            }
            for (int i = 0; i < method.getParameterTypes().length; ++i) {
                wrappedArgs.add(wrapObjectForClassLoader(hostCL, guestCL, args[i], method.getParameterTypes()[i]));
            }
        }
        return wrappedArgs.toArray(new Object[wrappedArgs.size()]);
    }

    /**
     * Wrap object for a class loader (if necessary)
     *
     * @param hostCL       specifies a host class loader (from which argument is accessible)
     * @param guestCL      specifies a guest class loader (which is supposed to be the actual consumer of the arguments)
     * @param originalType type that must be used for wrapping
     */
    private static Object wrapObjectForClassLoader(ClassLoader hostCL, ClassLoader guestCL, Object arg, Class originalType)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (arg == null) {
            return null;
        }
        if (arg instanceof Collection) {
            Collection newList = instantiateCollection((Class<? extends Collection>) arg.getClass());
            for (Object item : (Collection) arg) {
                newList.add(wrapObjectForClassLoader(hostCL, guestCL, item, originalType));
            }
            return newList;
        } else if (arg instanceof Enum) {
            return EnumUtil.fromString((Class<Enum>) guestCL.loadClass(arg.getClass().getName()), ((Enum) arg).name());
        } else if (!isBasicJavaType(arg.getClass())) {
            if (originalType != null) {
                return classLoaderProxy(hostCL, arg, originalType);
            }
            // if this is a not a basic type or a type from java base package, we must create proxy for it (if it
            // implements exactly one interface)
            return classLoaderProxy(hostCL, arg, guestCL.loadClass(getClassSingleInterface(arg.getClass()).getName()));
        } else if (arg instanceof Class) {
            // this is a class, we must load it from guest CL
            return guestCL.loadClass(((Class) arg).getName());
        } else {
            // this is probably a primitive type or basic class, we can pass it through as it is
            return (arg);
        }
    }


    /**
     * Returns single interface for a class.
     * Throws an exception if a class has more than 1 or has no interfaces
     */
    private static <T> Class<?> getClassSingleInterface(Class<T> clazz) {
        List<Class<?>> interfaces = getNonBasicJavaInterfaces(clazz);
        if (interfaces.size() != 1) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " must implement exactly one non-basic java interface!");
        }
        return interfaces.get(0);
    }

    /**
     * Returns non-basic java interfaces of a class
     */
    private static List<Class<?>> getNonBasicJavaInterfaces(Class<?> clazz) {
        List<Class<?>> result = new ArrayList<Class<?>>();
        for (Object iface : getAllInterfaces(clazz)) {
            if (!isBasicJavaType((Class<?>) iface)) {
                result.add((Class<?>) iface);
            }
        }
        return result;
    }

    /**
     * Returns the list of classes for the arguments from the different classloader
     */
    public static Class<?>[] getArgTypes(Object[] arguments, ClassLoader classLoader) throws ClassNotFoundException {
        List<Class<?>> types = new ArrayList<Class<?>>();
        for (Object arg : arguments) {
            types.add(classLoader.loadClass(arg.getClass().getName()));
        }
        return types.toArray(new Class<?>[types.size()]);
    }

    /**
     * Invokes any object method (even if it's private)
     */
    public static <T> Object invokeAnyMethod(Class<?> clazz, T instance, String method, Class<?>[] argTypes,
                                             Object... arguments) throws
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Class<?>> types = new ArrayList<Class<?>>();
        if (argTypes == null) {
            for (Object arg : arguments) {
                types.add(arg.getClass());
            }
            argTypes = types.toArray(new Class<?>[types.size()]);
        }
        Method m = clazz.getDeclaredMethod(method, argTypes);
        m.setAccessible(true);
        return m.invoke(instance, arguments);
    }

    /**
     * Invokes any object method (even if it's private)
     */
    public static <T> Object invokeAnyMethod(T instance, String method, Class<?>[] argTypes, Object... arguments) throws
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return invokeAnyMethod(instance.getClass(), instance, method, argTypes, arguments);
    }

    /**
     * Set private field
     */
    public static <T> void setPrivateField(T instance, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = (instance instanceof Class)
                ? ((Class) instance).getDeclaredField(name)
                : instance.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(instance, value);
    }

    /**
     * Invokes any object method (even if it's private)
     */
    public static <T> Object invokeAnyMethod(T instance, String method, Object... args) throws InvocationTargetException,
            NoSuchMethodException, IllegalAccessException {
        return invokeAnyMethod(instance, method, null, args);
    }

}
