/*
 * Copyright (C) 2026 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wiremock.url;

import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

class IsolatedClassLoader implements Closeable {

  private static final URL[] urls = getClasspathUrls();
  private final URLClassLoader isolated =
      new URLClassLoader(
          "isolated-" + new Random().nextInt(), urls, ClassLoader.getPlatformClassLoader());

  ReflectiveInstance load(String className) throws ClassNotFoundException {
    return new ReflectiveInstance(isolated.loadClass(className), isolated);
  }

  private static URL[] getClasspathUrls() {
    return Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
        .map(
            path -> {
              try {
                return new File(path).toURI().toURL();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .toArray(URL[]::new);
  }

  @Override
  public void close() throws IOException {
    isolated.close();
  }
}

@SuppressWarnings("unused")
final class ReflectiveInstance {

  private final Object instance;
  private final Class<?> theClass;
  private final ClassLoader classLoader;

  ReflectiveInstance(Object instance, ClassLoader classLoader) {
    this.instance = requireNonNull(instance);
    theClass = instance instanceof Class<?> aClass ? aClass : instance.getClass();
    this.classLoader = classLoader;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ReflectiveInstance that)) return false;
    return instance.equals(that.instance);
  }

  @Override
  public int hashCode() {
    return instance.hashCode();
  }

  @Override
  public String toString() {
    return instance.toString();
  }

  public ReflectiveInstance getReflectiveClass() {
    return new ReflectiveInstance(instance.getClass(), classLoader);
  }

  @Nullable ReflectiveInstance invoke(String methodName, Object... args)
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    return invoke(methodName, Arrays.stream(args).map(this::transform).toList());
  }

  private Pair<Class<?>, Object> transform(Object arg) {
    if (arg instanceof ReflectiveInstance ref) {
      return Pair.of((Class<?>) getReflectiveClass().instance, ref.instance);
    } else {
      return Pair.of(arg.getClass(), arg);
    }
  }

  @SafeVarargs
  @Nullable
  final ReflectiveInstance invoke(String methodName, Pair<String, @Nullable Object>... args)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    return invoke(methodName, Arrays.stream(args).map(this::transform).toList());
  }

  private Pair<Class<?>, @Nullable Object> transform(Pair<String, @Nullable Object> arg) {
    try {
      Object param = arg.getRight();
      Object unwrapped = param instanceof ReflectiveInstance ref ? ref.instance : param;

      return Pair.of(classLoader.loadClass(arg.getLeft()), unwrapped);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private @Nullable ReflectiveInstance invoke(
      String methodName, List<Pair<Class<?>, @Nullable Object>> args)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Class<?>[] parameterTypes = args.stream().map(Pair::getLeft).toArray(Class<?>[]::new);

    Object[] parameters = args.stream().map(Pair::getRight).toArray();
    Method theMethod = theClass.getMethod(methodName, parameterTypes);
    theMethod.setAccessible(true);
    //    System.out.println(describe(theMethod));
    //    for (Object parameter : parameters) {
    //      System.out.println(describe(parameter));
    //    }
    Object result = theMethod.invoke(instance, parameters);
    return asInstance(result);
  }

  private String describe(Method theMethod) {
    StringBuilder base = new StringBuilder(theMethod.toString()).append('\n');
    for (Class<?> parameterType : theMethod.getParameterTypes()) {
      ClassLoader parameterTypeClassLoader = parameterType.getClassLoader();
      base.append(parameterType.getName())
          .append(" ")
          .append(
              parameterTypeClassLoader != null ? parameterTypeClassLoader.getName() : "bootstrap")
          .append('\n');
    }
    return base.toString();
  }

  private String describe(@Nullable Object o) {
    StringBuilder base = new StringBuilder();
    if (o != null) {
      base.append(o)
          .append(" ")
          .append(o.getClass().getName())
          .append(" ")
          .append(o.getClass().getClassLoader().getName());
    } else {
      base.append("<null>");
    }
    return base.toString();
  }

  public @Nullable ReflectiveInstance field(String name)
      throws NoSuchFieldException, IllegalAccessException {
    Object result = theClass.getField(name).get(instance);
    return asInstance(result);
  }

  private @Nullable ReflectiveInstance asInstance(@Nullable Object result) {
    return result != null ? new ReflectiveInstance(result, classLoader) : null;
  }
}
