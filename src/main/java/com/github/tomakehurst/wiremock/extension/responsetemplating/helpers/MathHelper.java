/*
MIT License

Copyright (c) 2015 Matthew Hanlon

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Handlebars Math Helper
 *
 * <p>
 * Perform math operations in handlebars. Inspired by: http://jsfiddle.net/mpetrovich/wMmHS/
 * Operands are treated as java.math.BigDecimal and operations are performed with the
 * java.math.MathContext.DECIMAL64 MathContext, yielding results with 16 bits of precision
 * and rounding to the nearest EVEN digit, according to IEEE 754R. You can force rounding
 * decimal results using the extra parameter <code>scale</code>, which corresponds to calling
 * <code>BigDecimal.setScale(int scale, RoundingMode.HALF_UP)</code>.
 * </p>
 *
 * <p>addition</p>
 *
 * <pre>{{math arg0 "+" arg1}} // arg0 + arg1</pre>
 *
 * <p>subtraction</p>
 *
 * <pre>{{math arg0 "-" arg1}} // arg0 - arg1</pre>
 *
 * <p>multiplication</p>
 *
 * <pre>{{math arg0 "*" arg1}} // arg0 * arg1</pre>
 *
 * <p>division</p>
 *
 * <pre>{{math arg0 "/" arg1}} // arg0 / arg1</pre>
 *
 * <p>modulus</p>
 *
 * <pre>{{math arg0 "%" arg1}} // arg0 % arg1</pre>
 *
 * @author mrhanlon
 * @see java.math.BigDecimal
 * @see java.math.MathContext
 */
public class MathHelper implements Helper<Object> {

    public enum Operator {
        add("+"),
        subtract("-"),
        multiply("*"),
        divide("/"),
        mod("%");

        private Operator(String symbol) {
            this.symbol = symbol;
        }

        private String symbol;

        public static Operator fromSymbol(String symbol) {
            Operator op = null;
            if (symbol.equals("+")) {
                op = add;
            } else if (symbol.equals("-")) {
                op = subtract;
            } else if (symbol.equals("*")) {
                op = multiply;
            } else if (symbol.equals("/")) {
                op = divide;
            } else if (symbol.equals("%")) {
                op = mod;
            }
            return op;
        }
    }

    @Override
    public CharSequence apply(final Object value, Options options) throws IOException, IllegalArgumentException {
        if (options.params.length >= 2) {
            Operator op = Operator.fromSymbol(options.param(0).toString());
            if (op == null) {
                throw new IllegalArgumentException("Unknown operation '" + options.param(0) + "'");
            }

            Integer scale = options.hash("scale");

            MathContext mc = new MathContext(16, RoundingMode.HALF_UP);

            if (value == null || options.param(1) == null) {
                throw new IllegalArgumentException("Cannot perform operations on null values");
            }

            BigDecimal t0 = new BigDecimal(value.toString());
            BigDecimal t1 = new BigDecimal(options.param(1).toString());
            BigDecimal result;
            switch (op) {
                case add:
                    result = t0.add(t1, mc);
                    break;
                case subtract:
                    result = t0.subtract(t1, mc);
                    break;
                case multiply:
                    result = t0.multiply(t1, mc);
                    break;
                case divide:
                    result = t0.divide(t1, mc);
                    break;
                case mod:
                    result = t0.remainder(t1, mc);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown operation '" + options.param(0) + "'");
            }
            if (scale != null) {
                result = result.setScale(scale, BigDecimal.ROUND_HALF_UP);
            }
            return result.toString();
        } else {
            throw new IOException("MathHelper requires three parameters");
        }
    }
}
