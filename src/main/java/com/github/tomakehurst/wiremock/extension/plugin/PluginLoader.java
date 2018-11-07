package com.github.tomakehurst.wiremock.extension.plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.jknack.handlebars.Helper;
import com.github.tomakehurst.wiremock.extension.Extension;

public class PluginLoader {

	public static final String HELPER_REF = "@helpers";
	
	private PluginLoader() {
		
	}

	public static List<Extension> initExtensionsInstances(ExtensionFile extensionFile,
			List<URLClassLoader> jarClassLoaders) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		// Load all helpers in new object HashMap<String, Helper>
		Map<String, Helper<?>> helpersInstances = initHelpers(extensionFile, jarClassLoaders);
		// Load all extensions and instantiate them, modify param if value = @helpers
		return initExtensions(helpersInstances, extensionFile, jarClassLoaders);
	}

	private static List<Extension> initExtensions(Map<String, Helper<?>> helpersInstances, ExtensionFile extensionFile,
			List<URLClassLoader> jarClassLoaders) throws ClassNotFoundException, NoSuchMethodException,
			InstantiationException, IllegalAccessException, InvocationTargetException {
		List<Extension> extensions = new ArrayList<>();
		for (ExtensionDefinition extensionDefinition : extensionFile.getExtensionList()) {
			extensions.add(initExtension(extensionDefinition, helpersInstances, jarClassLoaders));
		}
		return extensions;
	}

	private static Extension initExtension(ExtensionDefinition extensionDefinition,
			Map<String, Helper<?>> helpersInstances, List<URLClassLoader> jarClassLoaders)
			throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
			InvocationTargetException {
		// Search constructor by params
		Class<Extension> extensionClass = (Class<Extension>) getClassFromAnyLoader(jarClassLoaders,
				extensionDefinition.getExtensionClassname());

		// Init params
		List<Object> initargs = new ArrayList<>();
		for (ArgumentDefinition argumentDefinition : extensionDefinition.getArguments()) {
			if (StringUtils.equalsIgnoreCase(HELPER_REF, argumentDefinition.getValue())) {
				initargs.add(helpersInstances);
			} else {
				initargs.add(instantiateParam(argumentDefinition, jarClassLoaders));
			}
		}

		// Instantiate extension with constructor and params
		if (initargs.isEmpty()) {
			return extensionClass.newInstance();
		} else {
			Constructor<Extension> extensionConstructor = extractConstructor(extensionClass,
					extensionDefinition.getArguments(), jarClassLoaders);
			Object[] argArray = new Object[initargs.size()];
			return extensionConstructor.newInstance(initargs.toArray(argArray));
		}

	}

	private static Constructor<Extension> extractConstructor(Class<Extension> extensionClass,
			List<ArgumentDefinition> arguments, List<URLClassLoader> jarClassLoaders)
			throws ClassNotFoundException, NoSuchMethodException {

		Class<?>[] parameterTypes = new Class[arguments.size()];
		for (int i = 0; i < arguments.size(); i++) {
			parameterTypes[i] = getClassFromAnyLoader(jarClassLoaders, arguments.get(i).getType());
		}
		return extensionClass.getConstructor(parameterTypes);
	}

	private static Object instantiateParam(ArgumentDefinition definition, List<URLClassLoader> jarClassLoaders)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException {
		Class paramClass = getClassFromAnyLoader(jarClassLoaders, definition.getType());
		if (paramClass.isPrimitive()) {
			paramClass = ClassUtils.primitiveToWrapper(paramClass);
		}
		Constructor<Object> constructor = paramClass.getConstructor(String.class);
		return constructor.newInstance(definition.getValue());

	}

	private static Map<String, Helper<?>> initHelpers(ExtensionFile extensionFile, List<URLClassLoader> jarClassLoaders)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Map<String, Helper<?>> instantiatedHelpers = new HashMap<>();
		for (HelperDefinition helperDefinition : extensionFile.getHelpers()) {
			Class<Helper<?>> helperClass = (Class<Helper<?>>) getClassFromAnyLoader(jarClassLoaders,
					helperDefinition.getHelperClass());
			instantiatedHelpers.put(helperDefinition.getHelperName(), helperClass.newInstance());
		}
		return instantiatedHelpers;
	}

	private static Class<?> getClassFromAnyLoader(List<URLClassLoader> jarClassLoaders, String className)
			throws ClassNotFoundException {
		try {
			return ClassUtils.getClass(className);
		} catch (ClassNotFoundException e) {
			for (URLClassLoader urlClassLoader : jarClassLoaders) {
				try {
					return ClassUtils.getClass(urlClassLoader, className, true);
				} catch (ClassNotFoundException e1) {
					// Nothing to do, just iterate over next classLoader
				}
			}
		}
		throw new ClassNotFoundException("Plugin loader can't found class " + className);
	}
}
