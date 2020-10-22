/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.eclipse.imagen.media.tilecodec ;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.imagen.ParameterListDescriptor;
import org.eclipse.imagen.tilecodec.TileCodecDescriptor ;
import org.eclipse.imagen.tilecodec.TileCodecParameterList ;
import org.eclipse.imagen.tilecodec.TileEncoderImpl ;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.plugins.jpeg.JPEGQTable;
import javax.imageio.stream.ImageOutputStream;

/**
 * A concrete implementation of the <code>TileEncoderImpl</code> class
 * for the jpeg tile codec.
 */
public class JPEGTileEncoder extends TileEncoderImpl {
    /* The associated TileCodecDescriptor */
    private TileCodecDescriptor tcd = null ;

    /**
     * Constructs an <code>JPEGTileEncoder</code>. Concrete implementations
     * of <code>TileEncoder</code> may throw an
     * <code>IllegalArgumentException</code> if the
     * <code>param</code>'s <code>getParameterListDescriptor()</code> method
     * does not return the same descriptor as that from the associated
     * <code>TileCodecDescriptor</code>'s 
     * <code>getParameterListDescriptor</code> method for the "tileEncoder" 
     * registry mode. 
     *
     * <p> If param is null, then the default parameter list for encoding
     * as defined by the associated <code>TileCodecDescriptor</code>'s 
     * <code>getDefaultParameters()</code> method will be used for encoding.
     *
     * @param output The <code>OutputStream</code> to write encoded data to.
     * @param param  The object containing the tile encoding parameters.
     * @throws IllegalArgumentException if param is not the appropriate 
     * Class type.
     * @throws IllegalArgumentException is output is null.
     */
    public JPEGTileEncoder(OutputStream output, TileCodecParameterList param) {
        super("jpeg", output, param) ;
        tcd = TileCodecUtils.getTileCodecDescriptor("tileEncoder", "jpeg");
    }

    /**
     * Encodes a <code>Raster</code> and writes the output
     * to the <code>OutputStream</code> associated with this 
     * <code>TileEncoder</code>.
     *
     * @param ras the <code>Raster</code> to encode.
     * @throws IOException if an I/O error occurs while writing to the 
     * OutputStream.
     * @throws IllegalArgumentException if ras is null.
     */
    public void encode(Raster ras) throws IOException {
	if(ras == null)
	    throw new IllegalArgumentException(
		JaiI18N.getString("TileEncoder1")) ;

	ImageWriter writer = null;
	Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
	while (iter.hasNext()) {
	    writer = iter.next();
	}
	assert writer != null;
	ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
	writer.setOutput(ios);

	SampleModel sm = ras.getSampleModel();
	IIOMetadata iooMetaData = convertToIIOMetadata(writer, sm);
	IIOImage iioImage = new IIOImage(ras, null, null);

	JPEGImageWriteParam param = (JPEGImageWriteParam) writer.getDefaultWriteParam();

	if(paramList.getBooleanParameter("qualitySet")) {
	    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	    float quality = paramList.getFloatParameter("quality") ;
	    param.setCompressionQuality(quality);
	}

	writer.write(iooMetaData, iioImage, param);
	ios.close();
	writer.dispose();
    }

    private IIOMetadata convertToIIOMetadata(ImageWriter writer, SampleModel sm) {
	if(sm == null)
	    return null;

	int nbands = sm.getNumBands();

	ColorModel cm = createColorModel(nbands);

	int[] hSubSamp
		= (int[]) paramList.getObjectParameter("horizontalSubsampling");
	int[] vSubSamp
		= (int[]) paramList.getObjectParameter("verticalSubsampling");
	int[] qTabSlot
		= (int[]) paramList.getObjectParameter("quantizationTableMapping");

	IIOMetadata iooMetaData = writer.getDefaultImageMetadata(
		new ImageTypeSpecifier(cm, sm), null);
	IIOMetadataNode tree = (IIOMetadataNode) iooMetaData.getAsTree(
		"javax_imageio_jpeg_image_1.0");

	IIOMetadataNode componentSpec =
		(IIOMetadataNode) tree.getElementsByTagName("componentSpec");
	IIOMetadataNode dqt = (IIOMetadataNode) tree.getElementsByTagName("dqt");

	for (int i = 0; i < nbands; i++) {
	    int[] qTab
		    = (int[]) paramList.getObjectParameter("quantizationTable" + i);
	    if(qTab != null &&
		    qTab.equals(ParameterListDescriptor.NO_PARAMETER_DEFAULT)) {
		componentSpec.setAttribute("componentId", Integer.toString(i));
		componentSpec.setAttribute("HsamplingFactor", Integer.toString(hSubSamp[i]));
		componentSpec.setAttribute("VsamplingFactor", Integer.toString(vSubSamp[i]));
		componentSpec.setAttribute("QtableSelector", Integer.toString(qTabSlot[i]));

		IIOMetadataNode dqti = (IIOMetadataNode) dqt.item(i);
		IIOMetadataNode dqtable  = (IIOMetadataNode) dqti.getElementsByTagName("dqtable");

		// dqtable.setAttribute("elementPrecision", "0"); // 8-bit
		dqtable.setAttribute("qtableId", Integer.toString(qTabSlot[i]));
		dqtable.setUserObject(new JPEGQTable(qTab));
	    }
	}

	IIOMetadataNode dri = (IIOMetadataNode) tree.getElementsByTagName("dri").item(0);
	int rInt = paramList.getIntParameter("restartInterval");
	dri.setAttribute("interval", Integer.toString(rInt));

//	paramList.getBooleanParameter("writeImageInfo");
//	paramList.getBooleanParameter("writeTableInfo");

	if(!paramList.getBooleanParameter("writeJFIFHeader")) {
	    if (tree.hasAttribute("app0JFIF")) {
		tree.removeAttribute("app0JFIF");
	    }
	}
	return iooMetaData;
    }

    private ColorModel createColorModel(int nbands) {
        ColorSpace cs = null;
        int[] bits = null;
	if (nbands == 1) {
	    cs = ColorSpace.getInstance(ColorSpace.TYPE_GRAY);
	    bits = new int[] { 8 };
	}
	if (nbands == 3) {
	    cs = ColorSpace.getInstance(ColorSpace.TYPE_YCbCr);
	    bits = new int[] { 8, 8, 8 };
	}
	if (nbands == 4) {
	    cs = ColorSpace.getInstance(ColorSpace.TYPE_CMYK);
	    bits = new int[] { 8, 8, 8, 8 };
	}
	assert cs != null;
	return new ComponentColorModel(cs, bits, false, false,
		Transparency.OPAQUE, DataBuffer.TYPE_BYTE)
    }
}
