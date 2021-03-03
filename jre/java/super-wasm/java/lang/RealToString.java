/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package java.lang;

final class RealToString {

  private static final int FLOAT_MANTISSA_BITS = 23;
  private static final int FLOAT_MANTISSA_MASK = 0x007fffff;
  private static final int FLOAT_EXPONENT_BIAS = 127;
  private static final int FLOAT_EXPONENT_MASK = 0x7f800000;
  private static final int FLOAT_SIGN_MASK = 0x80000000;

  private static final int DOUBLE_MANTISSA_BITS = 52;
  private static final long DOUBLE_MANTISSA_MASK = 0x000fffffffffffffL;
  private static final int DOUBLE_EXPONENT_BIAS = 1023;
  private static final long DOUBLE_EXPONENT_MASK = 0x7ff0000000000000L;
  private static final long DOUBLE_SIGN_MASK = 0x8000000000000000L;

  private static final long[] LONG_POWERS_OF_TEN =
      new long[] {
        1L,
        10L,
        100L,
        1000L,
        10000L,
        100000L,
        1000000L,
        10000000L,
        100000000L,
        1000000000L,
        10000000000L,
        100000000000L,
        1000000000000L,
        10000000000000L,
        100000000000000L,
        1000000000000000L,
        10000000000000000L,
        100000000000000000L,
        1000000000000000000L,
      };

  private static final double invLogOfTenBaseTwo = Math.log(2.0) / Math.log(10.0);

  private static int firstK;
  /** An array of decimal digits, filled by longDigitGenerator or bigIntDigitGenerator. */
  private static final int[] digits = new int[64];
  /** Number of valid entries in 'digits'. */
  private static int digitCount;

  private RealToString() {}

  private static String resultOrSideEffect(AbstractStringBuilder sb, String s) {
    if (sb != null) {
      sb.append0(s);
      return null;
    }
    return s;
  }

  public static String doubleToString(double d) {
    return convertDouble(null, d);
  }

  public static void appendDouble(AbstractStringBuilder sb, double d) {
    convertDouble(sb, d);
  }

  private static String convertDouble(AbstractStringBuilder sb, double inputNumber) {
    long inputNumberBits = Double.doubleToLongBits(inputNumber);
    boolean positive = (inputNumberBits & DOUBLE_SIGN_MASK) == 0;
    int e = (int) ((inputNumberBits & DOUBLE_EXPONENT_MASK) >> DOUBLE_MANTISSA_BITS);
    long f = inputNumberBits & DOUBLE_MANTISSA_MASK;
    boolean mantissaIsZero = f == 0;
    String quickResult = null;
    if (e == 2047) {
      if (mantissaIsZero) {
        quickResult = positive ? "Infinity" : "-Infinity";
      } else {
        quickResult = "NaN";
      }
    } else if (e == 0) {
      if (mantissaIsZero) {
        quickResult = positive ? "0.0" : "-0.0";
      } else if (f == 1) {
        // special case to increase precision even though 2 * Double.MIN_VALUE is 1.0e-323
        quickResult = positive ? "4.9E-324" : "-4.9E-324";
      }
    }
    if (quickResult != null) {
      return resultOrSideEffect(sb, quickResult);
    }
    int p = DOUBLE_EXPONENT_BIAS + DOUBLE_MANTISSA_BITS; // the power offset (precision)
    int pow;
    int numBits = DOUBLE_MANTISSA_BITS;
    if (e == 0) {
      pow = 1 - p; // a denormalized number
      long ff = f;
      while ((ff & 0x0010000000000000L) == 0) {
        ff = ff << 1;
        numBits--;
      }
    } else {
      // 0 < e < 2047
      // a "normalized" number
      f = f | 0x0010000000000000L;
      pow = e - p;
    }
    firstK = digitCount = 0;
    if (-59 < pow && pow < 6 || (pow == -59 && !mantissaIsZero)) {
      longDigitGenerator(f, pow, e == 0, mantissaIsZero, numBits);
    } else {
      bigIntDigitGenerator(f, pow, e == 0, numBits);
    }
    AbstractStringBuilder dst = (sb != null) ? sb : new StringBuilder(26);
    if (inputNumber >= 1e7D
        || inputNumber <= -1e7D
        || (inputNumber > -1e-3D && inputNumber < 1e-3D)) {
      freeFormatExponential(dst, positive);
    } else {
      freeFormat(dst, positive);
    }
    return (sb != null) ? null : dst.toString();
  }

  public static String floatToString(float f) {
    return convertFloat(null, f);
  }

  public static void appendFloat(AbstractStringBuilder sb, float f) {
    convertFloat(sb, f);
  }

  public static String convertFloat(AbstractStringBuilder sb, float inputNumber) {
    int inputNumberBits = Float.floatToIntBits(inputNumber);
    boolean positive = (inputNumberBits & FLOAT_SIGN_MASK) == 0;
    int e = (inputNumberBits & FLOAT_EXPONENT_MASK) >> FLOAT_MANTISSA_BITS;
    int f = inputNumberBits & FLOAT_MANTISSA_MASK;
    boolean mantissaIsZero = f == 0;
    String quickResult = null;
    if (e == 255) {
      if (mantissaIsZero) {
        quickResult = positive ? "Infinity" : "-Infinity";
      } else {
        quickResult = "NaN";
      }
    } else if (e == 0 && mantissaIsZero) {
      quickResult = positive ? "0.0" : "-0.0";
    }
    if (quickResult != null) {
      return resultOrSideEffect(sb, quickResult);
    }
    int p = FLOAT_EXPONENT_BIAS + FLOAT_MANTISSA_BITS; // the power offset (precision)
    int pow;
    int numBits = FLOAT_MANTISSA_BITS;
    if (e == 0) {
      pow = 1 - p; // a denormalized number
      if (f < 8) { // want more precision with smallest values
        f = f << 2;
        pow -= 2;
      }
      int ff = f;
      while ((ff & 0x00800000) == 0) {
        ff = ff << 1;
        numBits--;
      }
    } else {
      // 0 < e < 255
      // a "normalized" number
      f = f | 0x00800000;
      pow = e - p;
    }
    firstK = digitCount = 0;
    if (-59 < pow && pow < 35 || (pow == -59 && !mantissaIsZero)) {
      longDigitGenerator(f, pow, e == 0, mantissaIsZero, numBits);
    } else {
      bigIntDigitGenerator(f, pow, e == 0, numBits);
    }
    AbstractStringBuilder dst = (sb != null) ? sb : new StringBuilder(26);
    if (inputNumber >= 1e7f
        || inputNumber <= -1e7f
        || (inputNumber > -1e-3f && inputNumber < 1e-3f)) {
      freeFormatExponential(dst, positive);
    } else {
      freeFormat(dst, positive);
    }
    return (sb != null) ? null : dst.toString();
  }

  private static void freeFormatExponential(AbstractStringBuilder sb, boolean positive) {
    int digitIndex = 0;
    if (!positive) {
      sb.append0('-');
    }
    sb.append0((char) ('0' + digits[digitIndex++]));
    sb.append0('.');
    int k = firstK;
    int exponent = k;
    while (true) {
      k--;
      if (digitIndex >= digitCount) {
        break;
      }
      sb.append0((char) ('0' + digits[digitIndex++]));
    }
    if (k == exponent - 1) {
      sb.append0('0');
    }
    sb.append0('E');
    IntegralToString.appendInt(sb, exponent);
  }

  private static void freeFormat(AbstractStringBuilder sb, boolean positive) {
    int digitIndex = 0;
    if (!positive) {
      sb.append0('-');
    }
    int k = firstK;
    if (k < 0) {
      sb.append0('0');
      sb.append0('.');
      for (int i = k + 1; i < 0; ++i) {
        sb.append0('0');
      }
    }
    int U = digits[digitIndex++];
    do {
      if (U != -1) {
        sb.append0((char) ('0' + U));
      } else if (k >= -1) {
        sb.append0('0');
      }
      if (k == 0) {
        sb.append0('.');
      }
      k--;
      U = digitIndex < digitCount ? digits[digitIndex++] : -1;
    } while (U != -1 || k >= -1);
  }

  private static void bigIntDigitGenerator(long f, int e, boolean isDenormalized, int p) {
    throw new AssertionError("Not implemented yet");
  }

  private static void longDigitGenerator(
      long f, int e, boolean isDenormalized, boolean mantissaIsZero, int p) {
    long R, S, M;
    if (e >= 0) {
      M = 1l << e;
      if (!mantissaIsZero) {
        R = f << (e + 1);
        S = 2;
      } else {
        R = f << (e + 2);
        S = 4;
      }
    } else {
      M = 1;
      if (isDenormalized || !mantissaIsZero) {
        R = f << 1;
        S = 1l << (1 - e);
      } else {
        R = f << 2;
        S = 1l << (2 - e);
      }
    }
    int k = (int) Math.ceil((e + p - 1) * invLogOfTenBaseTwo - 1e-10);
    if (k > 0) {
      S = S * LONG_POWERS_OF_TEN[k];
    } else if (k < 0) {
      long scale = LONG_POWERS_OF_TEN[-k];
      R = R * scale;
      M = M == 1 ? scale : M * scale;
    }
    if (R + M > S) { // was M_plus
      firstK = k;
    } else {
      firstK = k - 1;
      R = R * 10;
      M = M * 10;
    }
    boolean low, high;
    int U;
    while (true) {
      // Set U to floor(R/S) and R to the remainder, using *unsigned* 64-bit division
      U = 0;
      for (int i = 3; i >= 0; i--) {
        long remainder = R - (S << i);
        if (remainder >= 0) {
          R = remainder;
          U += 1 << i;
        }
      }
      low = R < M; // was M_minus
      high = R + M > S; // was M_plus
      if (low || high) {
        break;
      }
      R = R * 10;
      M = M * 10;
      digits[digitCount++] = U;
    }
    if (low && !high) {
      digits[digitCount++] = U;
    } else if (high && !low) {
      digits[digitCount++] = U + 1;
    } else if ((R << 1) < S) {
      digits[digitCount++] = U;
    } else {
      digits[digitCount++] = U + 1;
    }
  }
}