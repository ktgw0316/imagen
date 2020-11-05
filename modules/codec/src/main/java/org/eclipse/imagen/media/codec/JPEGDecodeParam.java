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

package org.eclipse.imagen.media.codec;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGQTable;
import java.io.IOException;

/**
 * An instance of <code>ImageDecodeParam</code> for decoding images in
 * the JPEG format.
 *
 * <p> This class allows for the specification of whether to decode the
 * JPEG data into an image having a <code>SampleModel</code> which is a
 * <code>ComponentSampleModel</code> or a subclass thereof.  By default
 * data are decoded into an image having a <code>ComponentSampleModel</code>.
 *
 * <p><b> This class is not a committed part of the JAI API.  It may
 * be removed or changed in future releases of JAI.</b>
 */
public class JPEGDecodeParam implements ImageDecodeParam {

    /**
     * Flag indicating whether to decode the data into an image with
     * a <code>ComponentSampleModel</code>.
     */
    private boolean decodeToCSM = true;
    
    /**
     * Constructs a <code>JPEGDecodeParam</code> object with default
     * parameter values.
     */
    public JPEGDecodeParam() {
    }

    /**
     * Sets the data formatting flag to the value of the supplied parameter.
     * If <code>true</code> the data will be decoded into an image which has
     * a <code>SampleModel</code> which is a <code>ComponentSampleModel</code>.
     * The default setting of this flag is <code>true</code>.  If the flag is
     * set to <code>false</code> then memory may be saved during decoding but
     * the resulting image is not in that case guaranteed to have a
     * <code>ComponentSampleModel</code>.
     *
     * @param decodeToCSM <code>true</code> if a
     * <code>ComponentSampleModel</code> layout is preferred for the
     * decoded image.
     */
    public void setDecodeToCSM(boolean decodeToCSM) {
        this.decodeToCSM = decodeToCSM;
    }

    /**
     * Returns the value of the <code>ComponentSampleModel</code> flag
     * which is by default <code>true</code>.
     */
    public boolean getDecodeToCSM() {
        return decodeToCSM;
    }

    private IIOMetadataNode tree;
    private IIOMetadataNode componentSpec;
    private IIOMetadataNode dqt;

    public void setReader(ImageReader reader) throws IOException {
        IIOMetadata metadata = reader.getImageMetadata(0);
        tree = (IIOMetadataNode) metadata.getAsTree("javax_imageio_jpeg_image_1.0");
        componentSpec = (IIOMetadataNode) tree.getElementsByTagName("componentSpec");
        dqt = (IIOMetadataNode) tree.getElementsByTagName("dqt");
    }

    public int getHorizontalSubsampling() {
        return Integer.parseInt(componentSpec.getAttribute("HsamplingFactor"));
    }

    public int getVerticalSubsampling() {
        return Integer.parseInt(componentSpec.getAttribute("VsamplingFactor"));
    }

    public JPEGQTable getQTable(int i) {
        IIOMetadataNode dqti = (IIOMetadataNode) dqt.item(i);
        IIOMetadataNode dqtable = (IIOMetadataNode) dqti.getElementsByTagName("dqtable");
        return (JPEGQTable) dqtable.getUserObject();
    }

    public int getQTableComponentMapping(int i) {
        // TODO
        return Integer.parseInt(componentSpec.getAttribute("QtableSelector"));
    }

    public boolean isTableInfoValid() {
        // TODO
        return true;
    }

    public boolean isImageInfoValid() {
        // TODO
        return true;
    }

    public int getRestartInterval() {
        IIOMetadataNode dri = (IIOMetadataNode) tree.getElementsByTagName("dri").item(0);
        return Integer.parseInt(dri.getAttribute("interval"));
    }

    public boolean hasJFIFHeader() {
        return tree.getElementsByTagName("app0JFIF").item(0) != null;
    }
}
