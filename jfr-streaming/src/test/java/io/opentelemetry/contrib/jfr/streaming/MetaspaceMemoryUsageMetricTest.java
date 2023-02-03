/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.jfr.streaming;

import static io.opentelemetry.contrib.jfr.streaming.internal.Constants.ATTR_COMPRESSED_CLASS_SPACE;
import static io.opentelemetry.contrib.jfr.streaming.internal.Constants.ATTR_METASPACE;
import static io.opentelemetry.contrib.jfr.streaming.internal.Constants.BYTES;
import static io.opentelemetry.contrib.jfr.streaming.internal.Constants.METRIC_DESCRIPTION_MEMORY;
import static io.opentelemetry.contrib.jfr.streaming.internal.Constants.METRIC_NAME_MEMORY;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.SumData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MetaspaceMemoryUsageMetricTest {

  @RegisterExtension
  JfrExtension jfrExtension =
      new JfrExtension(
          builder -> builder.disableAllFeatures().enableFeature(JfrFeature.MEMORY_POOL_METRICS));

  /**
   * This is a basic test for process.runtime.jvm.memory.usage and
   * process.runtime.jvm.memory.usage_after_last_gc metrics.
   */
  @Test
  void shouldHaveMemoryUsageMetrics() {
    System.gc();

    // Memory spaces in metaspace usage test
    jfrExtension.waitAndAssertMetrics(
        metric ->
            metric
                .hasName(METRIC_NAME_MEMORY)
                .hasUnit(BYTES)
                .hasDescription(METRIC_DESCRIPTION_MEMORY)
                .satisfies(
                    metricData -> {
                      SumData<?> sumData = metricData.getLongSumData();
                      assertThat(sumData.getPoints())
                          .anyMatch(p -> p.getAttributes().equals(ATTR_METASPACE));
                    }),
        metric ->
            metric
                .hasName(METRIC_NAME_MEMORY)
                .hasUnit(BYTES)
                .hasDescription(METRIC_DESCRIPTION_MEMORY)
                .satisfies(
                    metricData -> {
                      SumData<?> sumData = metricData.getLongSumData();
                      assertThat(sumData.getPoints())
                          .anyMatch(p -> p.getAttributes().equals(ATTR_COMPRESSED_CLASS_SPACE));
                    }));
  }
}
