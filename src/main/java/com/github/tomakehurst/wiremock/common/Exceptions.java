package com.github.tomakehurst.wiremock.common;

public class Exceptions {

    /**
     * Because this method throws an unchecked exception, when it is called in a method with a return type the compiler
     * does not know the method is exiting, requiring a further line to return null or throw an unchecked exception
     * directly. This generified method allows this to be avoided by tricking the compiler by adding a return statement
     * as so:
     * <pre>
     *     String someMethod() {
     *         try {
     *             somethingThatThrowsException();
     *         } catch (Exception e) {
     *             return throwUnchecked(e, String.class); // does not actually return, throws the exception
     *         }
     *     }
     * </pre>
     * @param ex The exception that will be thrown, unwrapped and unchecked
     * @param returnType trick to persuade the compiler that a method returns appropriately
     * @return Never returns, always throws the passed in exception
     */
    public static <T> T throwUnchecked(final Throwable ex, final Class<T> returnType) {
        Exceptions.<RuntimeException>throwsUnchecked(ex);
        throw new AssertionError("This code should be unreachable. Something went terribly wrong here!");
    }

    /**
     * @param ex The exception that will be thrown, unwrapped and unchecked
     */
    public static void throwUnchecked(final Throwable ex) {
        throwUnchecked(ex, null);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwsUnchecked(Throwable toThrow) throws T {
        throw (T) toThrow;
    }
}
