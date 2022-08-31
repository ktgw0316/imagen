/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

package org.eclipse.imagen.media.codecimpl;

import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.imagen.media.codec.SeekableStream;
import org.eclipse.imagen.media.codec.TIFFDecodeParam;
import org.eclipse.imagen.media.codec.TIFFDirectory;
import org.eclipse.imagen.media.codec.TIFFField;
import org.eclipse.imagen.media.codecimpl.util.MinMaxSingleBandColorModel;
import org.eclipse.imagen.media.codecimpl.util.PseudoColorSpace;

public class TIFFImage extends TIFFImageOrginal {

  public static class ObjectPair<F, S> implements Serializable {

    private static final long serialVersionUID = 1L;
    private final S secondObject;
    private final F firstObject;

    public ObjectPair(final F firstObject, final S secondObject) {
      this.firstObject = firstObject;
      this.secondObject = secondObject;
    }

    public F getFirstObject() {
      return this.firstObject;
    }

    public S getSecondObject() {
      return this.secondObject;
    }

    public static <F, S> ObjectPair<F, S> of(final F firstObject, final S secondObject) {
      return new ObjectPair<>(firstObject, secondObject);
    }
  }

  public TIFFImage(final SeekableStream stream, final TIFFDecodeParam param, final int directory)
      throws IOException {
    super(stream, param, directory);

    if (this.sampleModel.getNumBands() != 1) {
      return;
    }

    if (this.colorModel == null) {
      final int dataType = this.sampleModel.getDataType();
      if (dataType != DataBuffer.TYPE_FLOAT
          && dataType != DataBuffer.TYPE_DOUBLE
          && dataType != DataBuffer.TYPE_USHORT
          && dataType != DataBuffer.TYPE_SHORT
          && dataType != DataBuffer.TYPE_INT) {
        return;
      }
      Double noDataValue = getNoDataValue(dataType);
      final ObjectPair<Double, Double> minMaxValues = calculateMinMaxValues(
          dataType,
          noDataValue == null ? Double.valueOf(Double.NaN) : noDataValue);
      if (minMaxValues == null
          || Objects.equals(minMaxValues.getFirstObject(), minMaxValues.getSecondObject())) {
        return;
      }

      this.colorModel = new MinMaxSingleBandColorModel(
          minMaxValues.getFirstObject().doubleValue(),
          minMaxValues.getSecondObject().doubleValue(),
          noDataValue != null ? noDataValue.doubleValue() : Double.POSITIVE_INFINITY,
          noDataValue != null,
          new PseudoColorSpace(1),
          dataType);
      return;
    }
    if (!(("org.eclipse.imagen.FloatDoubleColorModel".equals(this.colorModel.getClass().getName())
        || this.colorModel instanceof org.eclipse.imagen.media.codecimpl.util.FloatDoubleColorModel)
        && Boolean.getBoolean("org.eclipse.imagen.media.codec.tiff.colormodel.overwrite"))) {
      return;
    }
    final int transferType = this.colorModel.getTransferType();
    if (transferType != DataBuffer.TYPE_FLOAT
        && transferType != DataBuffer.TYPE_DOUBLE
        && transferType != DataBuffer.TYPE_USHORT
        && transferType != DataBuffer.TYPE_SHORT
        && transferType != DataBuffer.TYPE_INT) {
      return;
    }

    final Double noDataValue = getNoDataValue(transferType);
    if (noDataValue == null) {
      return;
    }

    final ObjectPair<Double, Double> minMaxValues = calculateMinMaxValues(
        transferType,
        noDataValue);
    if (minMaxValues == null
        || Objects.equals(minMaxValues.getFirstObject(), minMaxValues.getSecondObject())) {
      return;
    }
    // LOGGER.log(ILevel.DEBUG,
    // "Identified GeoTIFF: min " //$NON-NLS-1$
    // + minMaxValues.getFirstObject()
    // + ", max " //$NON-NLS-1$
    // + minMaxValues.getSecondObject()
    // + ", nodata " //$NON-NLS-1$
    // + noDataValue);

    this.colorModel = new MinMaxSingleBandColorModel(
        minMaxValues.getFirstObject().doubleValue(),
        minMaxValues.getSecondObject().doubleValue(),
        noDataValue.doubleValue(),
        true,
        new PseudoColorSpace(1),
        transferType);
  }

  private ObjectPair<Double, Double> calculateMinMaxValues(
      final int transferType,
      final Double noDataValue) {
    final Double min =
        Optional.ofNullable(getDoubleData(DataBuffer.TYPE_SHORT, TIFFImageDecoder.TIFFTAG_MINSAMPLEVALUE))
            .orElse(getDoubleData(DataBuffer.TYPE_SHORT, TIFFImageDecoder.TIFF_S_MIN_SAMPLE_VALUE));
    final Double max =
        Optional.ofNullable(getDoubleData(DataBuffer.TYPE_SHORT, TIFFImageDecoder.TIFFTAG_MAXSAMPLEVALUE))
            .orElse(getDoubleData(DataBuffer.TYPE_SHORT, TIFFImageDecoder.TIFF_S_MAX_SAMPLE_VALUE));

    if (min != null && max != null) {
      return ObjectPair.of(min, max);
    }
    if (Boolean.getBoolean("org.eclipse.imagen.media.codec.tiff.bruteforce.minmax")) {
      final ObjectPair<Double, Double> interval = calculateMinMaxValuesBruteForce(
          transferType,
          noDataValue.doubleValue());
      if (interval != null) {
        return interval;
      }
    }

    return getMinMaxValueByDataType(transferType);
  }

  private ObjectPair<Double, Double> getMinMaxValueByDataType(final int transferType) {
    if (transferType == DataBuffer.TYPE_INT) {
      int minValue = Integer.MIN_VALUE;
      int maxValue = Integer.MAX_VALUE;
      return ObjectPair.of(Double.valueOf(minValue), Double.valueOf(maxValue));
    }
    if (transferType == DataBuffer.TYPE_BYTE) {
      int minValue = 0;
      int maxValue = Byte.MAX_VALUE;
      return ObjectPair.of(Double.valueOf(minValue), Double.valueOf(maxValue));
    }
    if (transferType == DataBuffer.TYPE_USHORT) {
      int minValue = 0;
      int maxValue = Short.MAX_VALUE;
      return ObjectPair.of(Double.valueOf(minValue), Double.valueOf(maxValue));
    }
    if (transferType == DataBuffer.TYPE_SHORT) {
      int minValue = Short.MIN_VALUE;
      int maxValue = Short.MAX_VALUE;
      return ObjectPair.of(Double.valueOf(minValue), Double.valueOf(maxValue));
    }
    if (transferType == DataBuffer.TYPE_FLOAT) {
      float minValue = -Float.MAX_VALUE;
      float maxValue = Float.MAX_VALUE;
      return ObjectPair.of(Double.valueOf(minValue), Double.valueOf(maxValue));
    } else if (transferType == DataBuffer.TYPE_DOUBLE) {
      double minValue = -Double.MAX_VALUE;
      double maxValue = Double.MAX_VALUE;
      return ObjectPair.of(Double.valueOf(minValue), Double.valueOf(maxValue));
    }
    return null;
  }

  private ObjectPair<Double, Double> calculateMinMaxValuesBruteForce(
      final int transferType,
      final double noDataValue) {
    if (transferType == DataBuffer.TYPE_INT) {
      final Rectangle bounds = this.getBounds();
      final int[] buffer = new int[bounds.width * bounds.height];

      this.getData().getSamples(0, 0, bounds.width, bounds.height, 0, buffer);

      int minValue = Integer.MAX_VALUE;
      int maxValue = Integer.MIN_VALUE;

      boolean found = false;
      for (final int value : buffer) {
        if (value != (int) noDataValue) {
          minValue = Math.min(minValue, value);
          maxValue = Math.max(maxValue, value);
          found = true;
        }
      }
      if (!found) {
        return null;
      }
      return ObjectPair.of(Double.valueOf(minValue), Double.valueOf(maxValue));
    }
    if (transferType == DataBuffer.TYPE_BYTE) {
      final Rectangle bounds = this.getBounds();
      final int[] buffer = new int[bounds.width * bounds.height];

      this.getData().getSamples(0, 0, bounds.width, bounds.height, 0, buffer);

      int minValue = Byte.MAX_VALUE;
      int maxValue = 0;

      boolean found = false;
      for (final int aBuffer : buffer) {
        final int value = aBuffer;
        if (value != (int) noDataValue) {
          minValue = Math.min(minValue, value);
          maxValue = Math.max(maxValue, value);
          found = true;
        }
      }
      if (!found) {
        return null;
      }
      return ObjectPair.of(Double.valueOf(minValue), Double.valueOf(maxValue));
    }
    if (transferType == DataBuffer.TYPE_USHORT) {
      final Rectangle bounds = this.getBounds();
      final int[] buffer = new int[bounds.width * bounds.height];

      this.getData().getSamples(0, 0, bounds.width, bounds.height, 0, buffer);

      int minValue = Short.MAX_VALUE;
      int maxValue = 0;

      boolean found = false;
      for (final int aBuffer : buffer) {
        final int value = (short) aBuffer;
        if (value != (int) noDataValue) {
          minValue = Math.min(minValue, value);
          maxValue = Math.max(maxValue, value);
          found = true;
        }
      }
      if (!found) {
        return null;
      }
      return ObjectPair.of(Double.valueOf(minValue), Double.valueOf(maxValue));
    }
    if (transferType == DataBuffer.TYPE_SHORT) {
      final Rectangle bounds = this.getBounds();
      final int[] buffer = new int[bounds.width * bounds.height];

      this.getData().getSamples(0, 0, bounds.width, bounds.height, 0, buffer);

      int minValue = Short.MAX_VALUE;
      int maxValue = Short.MIN_VALUE;

      boolean found = false;
      for (final int aBuffer : buffer) {
        final int value = (short) aBuffer;
        if (value != (int) noDataValue) {
          minValue = Math.min(minValue, value);
          maxValue = Math.max(maxValue, value);
          found = true;
        }
      }
      if (!found) {
        return null;
      }
      return ObjectPair.of(Double.valueOf(minValue), Double.valueOf(maxValue));
    }
    if (transferType == DataBuffer.TYPE_FLOAT) {
      final Rectangle bounds = this.getBounds();
      final float[] buffer = new float[bounds.width * bounds.height];

      this.getData().getSamples(0, 0, bounds.width, bounds.height, 0, buffer);

      float minValue = Float.MAX_VALUE;
      float maxValue = -Float.MAX_VALUE;

      boolean found = false;
      for (final float value : buffer) {
        if (value != noDataValue) {
          minValue = Math.min(minValue, value);
          maxValue = Math.max(maxValue, value);
          found = true;
        }
      }
      if (!found) {
        return null;
      }
      return ObjectPair.of(Double.valueOf(minValue), Double.valueOf(maxValue));
    } else if (transferType == DataBuffer.TYPE_DOUBLE) {
      final Rectangle bounds = this.getBounds();
      final double[] buffer = new double[bounds.width * bounds.height];

      this.getData().getSamples(0, 0, bounds.width, bounds.height, 0, buffer);

      double minValue = Double.MAX_VALUE;
      double maxValue = -Double.MAX_VALUE;

      boolean found = false;
      for (final double value : buffer) {
        if (value != noDataValue) {
          minValue = Math.min(minValue, value);
          maxValue = Math.max(maxValue, value);
          found = true;
        }
      }
      if (!found) {
        return null;
      }
      return ObjectPair.of(Double.valueOf(minValue), Double.valueOf(maxValue));
    }
    return null;
  }

  private Double getNoDataValue(final int transferType) {
    final int fiffFieldId = TIFFImageDecoder.NODATA_TIFFFIELD;
    return getDoubleData(transferType, fiffFieldId);
  }

  private Double getDoubleData(final int transferType, final int tiffFieldCode) {
    final TIFFDirectory dir = (TIFFDirectory) this.properties.get("tiff_directory"); //$NON-NLS-1$
    if (!dir.isTagPresent(tiffFieldCode)) {
      return null;
    }
    try {
      final TIFFField field = dir.getField(tiffFieldCode);
      switch (field.getType()) {
        case TIFFField.TIFF_ASCII: {
          switch (transferType) {
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT: {
              return Double
                  .valueOf(Double.valueOf(Double.parseDouble(field.getAsString(0))).shortValue());
            }
            case DataBuffer.TYPE_INT: {
              return Double
                  .valueOf(Double.valueOf(Double.parseDouble(field.getAsString(0))).intValue());
            }
            default: {
              String string = field.getAsString(0);
              // Double.parseDouble or Double.valueOf are case sensitive
              if (Objects.equals(string.toLowerCase(), "nan")) {
                return Double.NaN;
              }
              if (Objects.equals(string.toLowerCase(), "infinity")) {
                return Double.POSITIVE_INFINITY;
              }
              if (Objects.equals(string.toLowerCase(), "-infinity")) {
                return Double.NEGATIVE_INFINITY;
              }
              return Double.parseDouble(string);
            }
          }
        }
        case TIFFField.TIFF_SSHORT:
        case TIFFField.TIFF_SHORT: {
          final char[] c = field.getAsChars();
          final int i = c[0];
          return Double.valueOf((short) i);
        }
        default: {
          return null;
        }
      }
    } catch (final Exception exception) {
      return null;
    }
  }
}