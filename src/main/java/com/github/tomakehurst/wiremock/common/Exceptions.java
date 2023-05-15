/*
 * Copyright (C) 2013-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;

public class Exceptions {

  /**
   * Because this method throws an unchecked exception, when it is called in a method with a return
   * type the compiler does not know the method is exiting, requiring a further line to return null
   * or throw an unchecked exception directly. This generified method allows this to be avoided by
   * tricking the compiler by adding a return statement as so:
   *
   * <pre>
   *     String someMethod() {
   *         try {
   *             somethingThatThrowsException();
   *         } catch (Exception e) {
   *             return throwUnchecked(e, String.class); // does not actually return, throws the exception
   *         }
   *     }
   * </pre>
   *
   * @param ex The exception that will be thrown, unwrapped and unchecked
   * @param returnType trick to persuade the compiler that a method returns appropriately
   * @return Never returns, always throws the passed in exception
   */
  public static <T> T throwUnchecked(final Throwable ex, final Class<T> returnType) {
    Exceptions.<RuntimeException>throwsUnchecked(ex);
    throw new AssertionError(
        "This code should be unreachable. Something went terribly wrong here!");
  }

  /** @param ex The exception that will be thrown, unwrapped and unchecked */
  public static void throwUnchecked(final Throwable ex) {
    throwUnchecked(ex, null);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> void throwsUnchecked(Throwable toThrow) throws T {
    throw (T) toThrow;
  }

  public static <T> T uncheck(Callable<T> work, Class<T> returnType) {
    try {
      return work.call();
    } catch (Exception e) {
      return throwUnchecked(e, returnType);
    }
  }

  public static void uncheck(RunnableWithException work) {
    try {
      work.run();
    } catch (Exception e) {
      throwUnchecked(e);
    }
  }

  public static String renderStackTrace(Throwable t) {
    StringWriter sw = new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }

  public interface RunnableWithException {
    void run() throws Exception;
  }
}
