package org.wiremock.url;

/**
 * Many elements of URI References have a normal form - e.g. the schemes {@code HTTP} and
 * {@code HtTp} both normalise to {@code http}.
 * <p>
 * {@code Type.normalise().equals(Type.normalise().normalise()} should always be true.
 *
 * @param <SELF> the self type
 */
public interface Normalisable<SELF extends Normalisable<SELF>> {

  /**
   * Returns a normalised form of this value.
   *
   * @return a normalised value
   */
  SELF normalise();

  boolean isNormalForm();
}
