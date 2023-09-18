/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.reflect.ClassPath;

import java.io.*;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

@SuppressWarnings("rawtypes")
class StringValuePatternTest {

  @Test
  void allSubclassesHaveWorkingToString() throws Exception {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    Set<ClassPath.ClassInfo> allClasses = ClassPath.from(classLoader).getAllClasses();

    Set<Class> newAllClasses =
            getClassOfPackage("com.github.tomakehurst.wiremock.matching");

    var newAllClassesAfterFilter01 =
        newAllClasses.stream()
            .filter(
                classInfo ->
                    classInfo
                        .getPackageName()
                        .startsWith("com.github.tomakehurst.wiremock.matching"))
                .sorted(Comparator.comparing(Class::getName))
            .collect(Collectors.toCollection(LinkedHashSet::new));

    var allClassesAfterFilter01 =
        allClasses.stream()
            .filter(
                classInfo ->
                    classInfo
                        .getPackageName()
                        .startsWith("com.github.tomakehurst.wiremock.matching"))
                .sorted(Comparator.comparing(ClassPath.ClassInfo::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new));

    var allClassesAfterMap =
        allClassesAfterFilter01.stream()
            .map(
                input -> {
                  try {
                    return input.load();
                  } catch (Throwable e) {
                    return Object.class;
                  }
                })
            .collect(Collectors.toSet());

    var allClassesAfterFilter02 =
        allClassesAfterMap.stream()
            .filter(clazz -> clazz.isAssignableFrom(StringValuePattern.class))
            .collect(Collectors.toSet());

    var allClassesAfterFilter03 =
        allClassesAfterFilter02.stream()
            .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
            .collect(Collectors.toSet());

    assertDoesNotThrow(
        () -> allClassesAfterFilter03.forEach(this::findConstructorWithStringParamInFirstPosition));
  }

    public Set<Class> getClassOfPackage(String packageName)
            throws ClassNotFoundException, IOException {

        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();

        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        Set<Class> classes = new HashSet<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    private Set<Class> findClasses(File directory, String packageName)
            throws ClassNotFoundException {
        Set<Class> classes = new HashSet<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : Objects.requireNonNull(files)) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file,
                        packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName
                        + '.'
                        + file.getName().substring(0,
                        file.getName().length() - 6)));
            }
        }
        return classes;
    }

//  public Set<Class> findAllClassesUsingClassLoader(String packageName) {
//    BufferedReader reader;
//    try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(packageName.replaceAll("[.]", "/"))) {
//      reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(stream)));
//    } catch (IOException ignored) {
//      return Collections.emptySet();
//    }
//    return reader
//        .lines()
//        .filter(line -> line.endsWith(".class"))
//        .map(line -> getClass(line, packageName))
//        .collect(Collectors.toSet());
//  }

//  private Class getClass(String className, String packageName) {
//    try {
//      return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
//    } catch (ClassNotFoundException ignored) {
//    }
//    return null;
//  }

  private void findConstructorWithStringParamInFirstPosition(Class<?> clazz) {
    Arrays.stream(clazz.getConstructors())
        .filter(
            constructor ->
                constructor.getParameterTypes().length > 0
                    && constructor.getParameterTypes()[0].equals(String.class)
                    && constructor.getParameterAnnotations().length > 0
                    && constructor.getParameterAnnotations()[0].length > 0
                    && constructor
                        .getParameterAnnotations()[0][0]
                        .annotationType()
                        .equals(JsonProperty.class))
        .findFirst()
        .orElseThrow(
            () ->
                new AssertionError(
                    "No constructor found with @JsonProperty annotated name parameter"));
  }
}
