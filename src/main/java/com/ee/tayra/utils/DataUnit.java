/*******************************************************************************
 * Copyright (c) 2013, Equal Experts Ltd
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation
 * are those of the authors and should not be interpreted as representing
 * official policies, either expressed or implied, of the Tayra Project.
 ******************************************************************************/
package com.ee.tayra.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DataUnit {
  private static final String regex = "^([0-9]+)(.+)$";
  private static final Pattern numberPattern = Pattern.compile(regex);
  public static final int PRIME = 31;
  private static final Map<String, DataUnit> cache
            = new HashMap<String, DataUnit>();

  private final ByteUnit byteUnit;
  private final int value;

  private DataUnit(final int value, final ByteUnit byteUnit) {
    this.value = value;
    this.byteUnit = byteUnit;
  }

  public int value() {
    return value;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }

    if (getClass() != other.getClass()) {
      return false;
    }
    DataUnit that = (DataUnit) other;

    return value == that.value && byteUnit == that.byteUnit;
  }

  @Override
  public int hashCode() {
    int result = byteUnit.hashCode();
    result = PRIME * result + value;
    return result;
  }

  public int toBytes() {
    return value() * byteUnit.toInt();
  }

  public static DataUnit from(final String valueWithUnit) {
    if (valueWithUnit == null || valueWithUnit.isEmpty()) {
      throw new IllegalArgumentException("Valid values are B, KB, MB, GB");
    }
    if (cache.containsKey(valueWithUnit)) {
      return cache.get(valueWithUnit);
    }
    Matcher matcher = numberPattern.matcher(valueWithUnit);
    if (matcher.matches()) {
      String group = matcher.group(1);
      int value = Integer.parseInt(group);
      String unit = matcher.group(2);
      final DataUnit dunit = new DataUnit(value, ByteUnit.from(unit));
      cache.put(valueWithUnit, dunit);
      return dunit;
    }
    throw new IllegalArgumentException("Don't know how to represent "
            + valueWithUnit);
  }
}
