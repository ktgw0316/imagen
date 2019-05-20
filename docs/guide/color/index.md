---
layout: default
title: Color Space
parent: Programming Guide
nav_order: 6
---

# Color Space                                                           

This chapter describes the JAI color space, transparency, and the
color conversion operators. JAI follows the Java AWT color model.


5.1 Introduction
-------------------------------------

Digital images, specifically digital color images, come in several
different forms. The form is often dictated by the means by which the
image was acquired or by the image\'s intended use.

One of the more basic types of color image is RGB, for the three
primary colors (red, green, and blue). RGB images are sometimes
acquired by a color scanner or video camera. These devices incorporate
three sensors that are spectrally sensitive to light in the red,
green, and blue portions of the spectrum. The three separate red,
green, and blue values can be made to directly drive red, green, and
blue light guns in a CRT. This type of color system is called an
*additive* linear RGB color system, as the sum of the three full color
values produces white.

Printed color images are based on a *subtractive* color process in
which cyan, magenta, and yellow (CMY) dyes are deposited onto paper.
The amount of dye deposited is subtractively proportional to the
amount of each red, blue, and green color value. The sum of the three
CMY color values produce black.

The black produced by a CMY color system often falls short of being a
true black. To produce a more accurate black in printed images, black
is often added as a fourth color component. This is known as the CMYK
color system and is commonly used in the printing industry.

The amount of light generated by the red, blue, and green phosphors of
a CRT is not linear. To achieve good display quality, the red, blue,
and green values must be adjusted - a process known as *gamma
correction*. In computer systems, gamma correction often takes place
in the frame buffer, where the RGB values are passed through lookup
tables that are set with the necessary compensation values.

In television transmission systems, the red, blue, and green
gamma-corrected color video signals are not transmitted directly.
Instead, a linear transformation between the RGB components is
performed to produce a *luminance* signal and a pair of *chrominance*
signals. The luminance signal conveys color brightness levels. The two
chrominance signals convey the color hue and saturation. This color
system is called YCC (or, more specifically, YC~b~C~r~).

Another significant color space standard for JAI is CIEXYZ. This is a
widely-used, device-independent color standard developed by the
Commission Internationale de l\'Éclairage (CIE). The CIEXYZ standard
is based on color-matching experiments on human observers.


5.2 Color Management
-----------------------------------------

JAI uses three primary classes for the management of color:

-   `ColorModel` - describes a particular way that pixel values are
    mapped to colors. A `ColorModel` is typically associated with an
    `Image` or `BufferedImage` and provides the information necessary
    to correctly interpret pixel values. `ColorModel` is defined in
    the `java.awt.image` package.


-   `ColorSpace` - represents a system for measuring colors, typically
    using three separate values or components. The `ColorSpace` class
    contains methods for converting between the original color space
    and one of two standard color spaces, CIEXYZ and RGB. `ColorSpace`
    is defined in the `java.awt.color` package.


-   `Color` - a fixed color, defined in terms of its components in a
    particular `ColorSpace`. `Color` is defined in the `java.awt`
    package.


### 5.2.1 Color Models

A `ColorModel` is used to interpret pixel data in an image. This
includes:

-   Mapping components in the bands of an image to components of a
    particular color space


-   Extracting pixel components from packed pixel data


-   Retrieving multiple components from a single band using masks


-   Converting pixel data through a lookup table

To determine the color value of a particular pixel in an image, you
need to know how the color information is encoded in each pixel. The
`ColorModel` associated with an image encapsulates the data and
methods necessary for translating a pixel value to and from its
constituent color components.

JAI supports five color models:

-   `DirectColorModel` - works with pixel values that represent RGB
    color and alpha information as separate samples and that pack all
    samples for a single pixel into a single int, short, or byte
    quantity. This class can be used only with ColorSpaces of type
    `ColorSpace.TYPE_RGB`.


-   `IndexColorModel` - works with pixel values consisting of a single
    sample that is an index into a fixed colormap in the default sRGB
    ColorSpace. The colormap specifies red, green, blue, and optional
    alpha components corresponding to each index.


-   `ComponentColorModel` - can handle an arbitrary `ColorSpace` and
    an array of color components to match the `ColorSpace`. This model
    can be used to represent most color models on most types of
    `GraphicsDevices`.


-   `PackedColorModel` - a base class for models that represent pixel
    values in which the color components are embedded directly in the
    bits of an integer pixel. A `PackedColorModel` stores the packing
    information that describes how color and alpha components are
    extracted from the channel. The `DirectColorModel` is a
    `PackedColorModel`.


-   `FloatDoubleColorModel` - works with pixel values that represent
    color and alpha information as separate samples, using float or
    double elements.

The following sample code shows the construction of a
`ComponentColorModel` for an RGB color model.

------------------------------------------------------------------------

         // Create an RGB color model
         int[] bits = { 8, 8, 8 };
         ColorModel colorModel = new
          ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                              bits, false, false,
                              Transparency.OPAQUE,
                              DataBuffer.TYPE_BYTE);

------------------------------------------------------------------------

The following sample code shows the construction of a
`ComponentColorModel` for a grayscale color model.

------------------------------------------------------------------------

         // Create a grayscale color model.
         ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
         int bits[] = new int[] {8};
         ColorModel cm = new ComponentColorModel(cs, bits, false, false,
                                                 Transparency.OPAQUE,
                                                 DataBuffer.TYPE_BYTE);

------------------------------------------------------------------------

The following sample code shows the construction of a
`FloatDoubleColorModel` for a linear RGB color model.

------------------------------------------------------------------------

         ColorSpace colorSpace =
             ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
         int[] bits = new int[3];
         bits[0] = bits[1] = bits[2] = 32;
         ColorModel cm = new FloatDoubleColorModel(colorSpace,
                                                   false,
                                                   false,
                                                   Transparency.OPAQUE,
                                                   DataBuffer.TYPE_FLOAT);

------------------------------------------------------------------------

**API:** 
|                                   | `java.awt.image.ComponentColorMod |
|                                   | el`                               |

    ComponentColorModel(ColorSpace colorSpace, int[] bits, 
           boolean  hasAlpha, boolean isAlphaPremultiplied, 
           int  transparency, int transferType)

:   constructs a `ComponentColorModel` from the specified parameters.
      --------------- ------------------------ -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
      *Parameters*:   `colorSpace`             The `ColorSpace` associated with this color model. See [Section 5.2.2, \"Color Space](../color).\"
                      `bits`                   The number of significant bits per component.
                      `hasAlpha`               If true, this color model supports alpha.
                      `isAlphaPremultiplied`   If true, alpha is premultiplied.
                      `transparency`           Specifies what alpha values can be represented by this color model. See [Section 5.3, \"Transparency](../color).\"
                      `transferType`           Specifies the type of primitive array used to represent pixel values. One of `DataBuffer.TYPE_BYTE`, `DataBuffer.TYPE_INT`, `DataBuffer.TYPE_SHORT`, `DataBuffer.TYPE_USHORT`, `DataBuffer.TYPE_DOUBLE`, or `DataBuffer.TYPE_FLOAT`
      --------------- ------------------------ -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

      : 

**API:** `org.eclipse.imagen.FloatDoubleColor |
|                                   | Model`

    FloatDoubleColorModel(ColorSpace colorSpace, boolean hasAlpha, 
           boolean isAlphaPremultiplied, int transparency, 
           int  transferType)

:   constructs a `FloatDoubleColorModel` from the specified
    parameters.
    *Parameters*:
    `colorSpace`
    The `ColorSpace` associated with this color model. See [Section
    5.2.2, \"Color Space](../color).\"
    `hasAlpha`
    If true, this color model supports alpha.
    `isAlphaPremultiplied`
    If true, alpha is premultiplied.
    `transparency`
    Specifies what alpha values can be represented by this color
    model. See [Section 5.3, \"Transparency](../color).\"
    `transferType`
    Specifies the type of primitive array used to represent pixel
    values. One of `DataBuffer.TYPE_FLOAT` or
    `DataBuffer.TYPE_DOUBLE`.


### 5.2.2 Color Space

The `ColorSpace` class represents a system for measuring colors,
typically using three or more separate numeric values. For example,
RGB and CMYK are color spaces. A `ColorSpace` object serves as a color
space tag that identifies the specific color space of a `Color` object
or, through a `ColorModel` object, of an `Image`, `BufferedImage`, or
`GraphicsConfiguration`.

`ColorSpace` provides methods that transform `Colors` in a specific
color space to and from `sRGB` and to and from a well-defined `CIEXYZ`
color space. All `ColorSpace` objects must be able to map a color from
the represented color space into `sRGB` and transform an `sRGB` color
into the represented color space.

[Table 5-1](../color) lists the variables used to refer to
color spaces (such as `CS_sRGB` and `CS_CIEXYZ`) and to color space
types (such as `TYPE_RGB` and `TYPE_CMYK`).

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  [Name]{#51323}               [Description]{#51325}
  ---------------------------- ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  CS\_CIEXYZ        A widely-used, device-independent color standard developed by the Commission Internationale de Eclairage (CIE), based on color-matching experiments on human observers.

  CS\_GRAY          Grayscale color space.

  CS\_LINEAR\_RGB   Linear RGB. Images that have not been previously color-corrected.

  CS\_PYCC          PhotoCD YCC conversion color space. A luminance/chrominance standard for Kodak PhotoCD images.

  CS\_sRGB          A proposed default \"standard RGB\" color standard for use over the Internet.

  TYPE\_2CLR        A generic two-component color space.

  TYPE\_3CLR        A generic three-component color space.

  TYPE\_4CLR        A generic four-component color space.

  TYPE\_5CLR        A generic five-component color space.

  TYPE\_6CLR        A generic six-component color space.

  TYPE\_7CLR        A generic seven-component color space.

  TYPE\_8CLR        A generic eight-component color space.

  TYPE\_9CLR        A generic nine-component color space.

  TYPE\_ACLR        A generic 10-component color space.

  TYPE\_BCLR        A generic 11-component color space.

  TYPE\_CCLR        A generic 12-component color space.

  TYPE\_CMY         Any of the family of CMY color spaces.

  TYPE\_CMYK        Any of the family of CMYK color spaces.

  TYPE\_DCLR        Generic 13-component color spaces.

  TYPE\_ECLR        Generic 14-component color spaces.

  TYPE\_FCLR        Generic 15-component color spaces.

  TYPE\_GRAY        Any of the family of GRAY color spaces.

  TYPE\_HLS         Any of the family of HLS color spaces.

  TYPE\_HSV         Any of the family of HSV color spaces.

  TYPE\_Lab         Any of the family of Lab color spaces.

  TYPE\_Luv         Any of the family of Luv color spaces.

  TYPE\_RGB         Any of the family of RGB color spaces.

  TYPE\_XYZ         Any of the family of XYZ color spaces.

  TYPE\_YCbCr       Any of the family of YCbCr color spaces.

  TYPE\_Yxy         Any of the family of Yxy color spaces.
  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  :  **[*Table 5-1*  Color Space
  Types]{#51319}**

Conversion between Java color spaces is simplified by a set of methods
that map a color from a represented color space to either sRGB or
CIEXYZ and transform a sRGB or CIEXYZ color space to the represented
color space. There are four methods:

-   The `toRGB` method transforms a `Color` in the represented color
    space to a `Color` in sRGB.


-   The `toCIEXYZ` method transforms a `Color` in the represented
    color space to a `Color` in CIEXYZ.


-   The `fromRGB` method takes a `Color` in sRGB and transforms into
    the represented color space.


-   The `fromCIEXYZ` method takes a `Color` in CIEXYZ and transforms
    into the represented color space.

The sRGB (which stands for \"standard\" RGB) color space is provided
as a convenience to programmers, since many applications are primarily
concerned with RGB images. Defining a standard RGB color space makes
writing such applications easier. The `toRGB` and `fromRGB` methods
are provided so that developers can easily retrieve colors in this
standard space. However, the sRGB color space is not intended for use
with highly accurate color correction or conversions.

The sRGB color space is somewhat limited in that it cannot represent
every color in the full gamut (spectrum of representable colors) of
CIEXYZ color. If a color is specified in some space that has a
different gammut than sRGB, using sRGB as an intermediate color space
results in a loss of information. The CIEXYZ color space is used as an
intermediate color space to avoid any loss of color quality. The
CIEXYZ color space is known as the *conversion space* for this reason.
The `toCIEXYZ` and `fromCIEXYZ` methods support conversions between
any two color spaces at a reasonably high degree of accuracy, one
color at a time.

**API:** 
|                                   | `java.awt.color.ColorSpace`       |

    abstract float[] toRGB(float[] colorvalue)

:   transforms a color value assumed to be in this `ColorSpace` into a
    value in the default `CS_sRGB` color space.
      -------------- -------------- ----------------------------------------------------------------------------------------
      *Parameter*:   `colorvalue`   A float array with length of at least the number of components in this `ColorSpace`.``
      -------------- -------------- ----------------------------------------------------------------------------------------

      : 


    abstract float[] fromRGB(float[] rgbvalue)

:   transforms a color value assumed to be in the default `CS_sRGB`
    color space into this `ColorSpace`.
      -------------- ------------ ------------------------------------------
      *Parameter*:   `rgbvalue`   A float array with length of at least 3.
      -------------- ------------ ------------------------------------------

      : 


    abstract float[] toCIEXYZ(float[] colorvalue)

:   transforms a color value assumed to be in this `ColorSpace` into
    the `CS_CIEXYZ` conversion color space.


    abstract float[] fromCIEXYZ(float[] colorvalue)

:   transforms a color value assumed to be in the `CS_CIEXYZ`
    conversion color space into this `ColorSpace`.


    static ColorSpace getInstance(int colorspace)

:   returns a ColorSpace representing one of the specific predefined
    color spaces.
      -------------- -------------- ------------------------------------------------------------------------------------------------------------------------------------------------------
      *Parameter*:   `colorSpace`   A specific color space identified by one of the predefined class constants (e.g., `CS_sRGB`, `CS_LINEAR_RGB`, `CS_CIEXYZ`, `CS_GRAY`, or `CS_PYCC`).
      -------------- -------------- ------------------------------------------------------------------------------------------------------------------------------------------------------

      : 


    int getType()

:   returns the color space type of this `ColorSpace` (for example
    `TYPE_RGB`, `TYPE_XYZ`, etc.).


### 5.2.3 ICC Profile and ICC Color Space

The `ColorSpace` class is an abstract class. It is expected that
particular implementations of subclasses of `ColorSpace` will support
high performance conversion based on underlying platform color
management systems. The `ICC_ColorSpace` class is one such
implementation provided in the base AWT. Developers can define their
own subclasses to represent arbitrary color spaces, as long as the
appropriate \"to\" and \"from\" conversion methods are implemented.
However, most developers can simply use the default `sRGB` color space
or color spaces that are represented by commonly-available ICC
profiles, such as profiles for monitors and printers or profiles
embedded in image data.``

The `ICC_ColorSpace` class is based on ICC profile data as represented
by the `ICC_Profile` class. The `ICC_Profile` class is a
representation of color profile data for device-independent and
device-dependent color spaces based on the *ICC Profile Format
Specification*, Version 3.4, August 15, 1997, from the International
Color Consortium. ICC profiles describe an *input space* and a
*connection space*, and define how to map between them.

The `ICC_Profile` class has two subclasses that correspond to the
specific color types:

-   `ICC_ProfileRGB`, which represents `TYPE_RGB` color spaces


-   `ICC_ProfileGray`, which represents `TYPE_GRAY` color spaces


5.3 Transparency
-------------------------------------

Just as images can have color, they can also have transparency.
Transparency defines the specular transmission of light through
transparent materials, such as glass, or the lack of transparency for
completely opaque objects. The amount of transparency is specified by
an alpha (![](shared/chars/alpha.gif)) value. An alpha value of 0.0
specifies complete translucency; an alpha value of 1.0 specifies
complete opacity.

Images can carry transparency information, known as the alpha channel,
for each pixel in the image. The alpha value is particularly important
when colors overlap. The alpha value specifies how much of the
previously-rendered color should show through.

The Java `Transparency` interface defines the common transparency
modes for implementing classes. [Table 5-2](../color)
lists the variables used to specify transparency.

  -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  [Name]{#51532}           [Description]{#51534}
  ------------------------ ------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  BITMASK       Represents image data that is guaranteed to be either completely opaque, with an alpha value of 1.0, or completely transparent, with an alpha value of 0.0.

  OPAQUE        Represents image data that is guaranteed to be completely opaque, meaning that all pixels have an alpha value of 1.0.

  TRANSLUCENT   Represents image data that contains or might contain arbitrary alpha values between and including 0.0 and 1.0.
  -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  :  **[*Table 5-2*  Transparency]{#51528}**

Transparency is specified as part of the color model (see [Section
5.2.1, \"Color Models](../color)\").


5.4 Color Conversion
-----------------------------------------

The `ColorConvert` operation performs a pixel-by-pixel color
conversion of the data in a rendered or renderable source image. The
data are treated as having no alpha channel, i.e., all bands are color
bands. The color space of the source image is specified by the
`ColorSpace` object of the source image `ColorModel` which must not be
null.

JAI does not attempt to verify that the `ColorModel` of the
destination image is consistent with the `ColorSpace` parameter. To
ensure that this is the case, a compatible `ColorModel` must be
provided via an `ImageLayout` in the `RenderingHints` (see [Section
3.7.3, \"Rendering Hints](../programming-environ)\").

Integral data are assumed to occupy the full range of the respective
data type; floating point data are assumed to be normalized to the
range \[0.0,1.0\]. By default, the destination image bounds, data
type, and number of bands are the same as those of the source image.

The `ColorConvert` operation takes one parameter:

  -----------------------------------------------------------------------------------------
  [Parameters]{#51557}    [Type]{#51559}          [Description]{#51561}
  ----------------------- ----------------------- -----------------------------------------
  colorSpace   ColorSpace   The destination color space.

  -----------------------------------------------------------------------------------------

  : 

For information on color space, see [Section 5.2.2, \"Color
Space](../color).\"

[Listing 5-1](../color) shows a code sample for a
`ColorConvert` operation.

**[]{#51792}**

***Listing 5-1*  Example ColorConvert
Operation**

------------------------------------------------------------------------

         // Read the image from the specified file name.
         RenderedOp src = JAI.create("fileload", fileName);

         // Create the ParameterBlock.
         ParameterBlock pb = new ParameterBlock();
         pb.addSource(src).add(colorSpace);

         // Perform the color conversion.
         RenderedOp dst = JAI.create("ColorConvert", pb);

------------------------------------------------------------------------


5.5 Non-standard Linear Color Conversion (BandCombine)
---------------------------------------------------------------------------

In JAI, the `BandCombine` operation performs a linear color conversion
between color spaces other than those listed in [Table
5-1](../color). The `BandCombine` operation computes a set
of arbitrary linear combinations of the bands of a rendered or
renderable source image, using a specified matrix. The matrix must
have dimension (\# of source bands plus one) by (\# of desired
destination bands).

The `BandCombine` operation takes one parameter:

  --------------------------------------------------------------------------------------------------
  [Parameter]{#51599}   [Type]{#51601}      [Description]{#51603}
  --------------------- ------------------- --------------------------------------------------------
  matrix     double   The matrix specifying the band combination.

  --------------------------------------------------------------------------------------------------

  : 

As an example, assume the three-band source image and the matrix shown
in [Figure 5-1](../color). The equation to calculate the
value of the destination pixel in this example would be:

:   *dst* = (255 \* 0.25) + (157 \* 0.5) + (28 \* 0.75)

    
    ------------------------------------------------------------------------

    ![](Color.doc.anc.gif)

    ------------------------------------------------------------------------


***Figure 5-1*  Band Combine Example**

In this example, the number of columns in the matrix is equal to the
number of bands in the source image. The number of rows in the matrix
must equal the number of bands in the destination image. For a
destination image with three bands, the values in the second row of
the matrix would be used to calculate the values in the second band of
the destination image and the values in the third row would be used to
calculate the values in the third band.

If the result of the computation underflows or overflows the minimum
or maximum value supported by the destination image, it will be
clamped to the minimum or maximum value, respectively.

[Listing 5-2](../color) shows a code sample for a
`BandCombine` operation.

**[]{#51816}**

***Listing 5-2*  Example BandCombine
Operation**

------------------------------------------------------------------------

         // Create the matrix.
         // Invert center band.
              double[][] matrix = {
                         { 1.0D,  0.0D, 0.0D,   0.0D },
                         { 0.0D, -1.0D, 0.0D, 255.0D },
                         { 0.0D,  0.0D, 1.0D,   0.0D },
                      };

         // Identity.
              double[][] matrix = {
                         { 1.0D, 0.0D, 0.0D, 0.0D },
                         { 0.0D, 1.0D, 0.0D, 0.0D },
                         { 0.0D, 0.0D, 1.0D, 0.0D },
                      };

         // Luminance stored into red band (3 band).
               double[][] matrix = {
                          { .114D, 0.587D, 0.299D, 0.0D },
                          { .000D, 0.000D, 0.000D, 0.0D },
                          { .000D, 0.000D, 0.000D, 0.0D }
                       };

         // Luminance (single band output).
               double[][] matrix = {
                          { .114D, 0.587D, 0.299D, 0.0D }
                       };

         // Create the ParameterBlock.
         ParameterBlock pb = new ParameterBlock();
         pb.addSource(src_image);
         pb.add(matrix);

         // Perform the band combine operation.
         dst = (PlanarImage)JAI.create("bandcombine", pb, null);

------------------------------------------------------------------------

------------------------------------------------------------------------

\




\

