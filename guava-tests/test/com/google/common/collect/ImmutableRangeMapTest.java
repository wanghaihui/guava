/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.common.collect;

import static com.google.common.collect.BoundType.OPEN;

import com.google.common.annotations.GwtIncompatible;

import junit.framework.TestCase;

/**
 * Tests for {@code ImmutableRangeMap}.
 *
 * @author Louis Wasserman
 */
@GwtIncompatible("NavigableMap")
public class ImmutableRangeMapTest extends TestCase {
  private static final ImmutableList<Range<Integer>> RANGES;
  private static final int MIN_BOUND = 0;
  private static final int MAX_BOUND = 10;
  static {
    ImmutableList.Builder<Range<Integer>> builder = ImmutableList.builder();

    builder.add(Range.<Integer>all());

    // Add one-ended ranges
    for (int i = MIN_BOUND; i <= MAX_BOUND; i++) {
      for (BoundType type : BoundType.values()) {
        builder.add(Range.upTo(i, type));
        builder.add(Range.downTo(i, type));
      }
    }

    // Add two-ended ranges
    for (int i = MIN_BOUND; i <= MAX_BOUND; i++) {
      for (int j = i; j <= MAX_BOUND; j++) {
        for (BoundType lowerType : BoundType.values()) {
          for (BoundType upperType : BoundType.values()) {
            if (i == j & lowerType == OPEN & upperType == OPEN) {
              continue;
            }
            builder.add(Range.range(i, lowerType, j, upperType));
          }
        }
      }
    }
    RANGES = builder.build();
  }

  public void testOverlapRejection() {
    for (Range<Integer> range1 : RANGES) {
      for (Range<Integer> range2 : RANGES) {
        boolean expectRejection =
            range1.isConnected(range2) && !range1.intersection(range2).isEmpty();
        ImmutableRangeMap.Builder<Integer, Integer> builder = ImmutableRangeMap.builder();
        builder.put(range1, 1);
        try {
          builder.put(range2, 2);
          assertFalse(expectRejection);
        } catch (IllegalArgumentException e) {
          assertTrue(expectRejection);
        }
      }
    }
  }

  public void testGet() {
    for (Range<Integer> range1 : RANGES) {
      for (Range<Integer> range2 : RANGES) {
        if (!range1.isEmpty() && !range2.isEmpty()
            && (!range1.isConnected(range2) || range1.intersection(range2).isEmpty())) {
          ImmutableRangeMap<Integer, Integer> rangeMap =
              ImmutableRangeMap.<Integer, Integer>builder().put(range1, 1).put(range2, 2).build();

          for (int i = MIN_BOUND; i <= MAX_BOUND; i++) {
            Integer expectedValue = null;
            if (range1.contains(i)) {
              expectedValue = 1;
            } else if (range2.contains(i)) {
              expectedValue = 2;
            }

            assertEquals(expectedValue, rangeMap.get(i));
          }
        }
      }
    }
  }

  public void testGetLargeRangeMap() {
    ImmutableRangeMap.Builder<Integer, Integer> builder = ImmutableRangeMap.builder();
    for (int i = 0; i < 100; i++) {
      builder.put(Range.closedOpen(i, i + 1), i);
    }
    ImmutableRangeMap<Integer, Integer> map = builder.build();
    for (int i = 0; i < 100; i++) {
      assertEquals(Integer.valueOf(i), map.get(i));
    }
  }

  public void testAsMapOfRanges() {
    for (Range<Integer> range1 : RANGES) {
      for (Range<Integer> range2 : RANGES) {
        if (!range1.isEmpty() && !range2.isEmpty()
            && (!range1.isConnected(range2) || range1.intersection(range2).isEmpty())) {
          ImmutableRangeMap<Integer, Integer> rangeMap =
              ImmutableRangeMap.<Integer, Integer>builder().put(range1, 1).put(range2, 2).build();

          ImmutableMap<Range<Integer>, Integer> expectedAsMap =
              ImmutableMap.of(range1, 1, range2, 2);
          ImmutableMap<Range<Integer>, Integer> asMap = rangeMap.asMapOfRanges();
          assertEquals(expectedAsMap, asMap);

          for (Range<Integer> query : RANGES) {
            assertEquals(expectedAsMap.get(query), asMap.get(query));
          }
        }
      }
    }
  }
}
