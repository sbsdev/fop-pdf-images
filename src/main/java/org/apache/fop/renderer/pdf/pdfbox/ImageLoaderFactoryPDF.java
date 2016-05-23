/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: ImageLoaderFactoryPDF.java 1620810 2014-08-27 08:32:15Z ssteiner $ */

package org.apache.fop.render.pdf.pdfbox;

import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageLoaderFactory;
import org.apache.xmlgraphics.image.loader.spi.ImageLoader;

/**
 * Factory class for the ImageLoader for PDF.
 */
public class ImageLoaderFactoryPDF extends AbstractImageLoaderFactory {

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
        ImagePDF.PDFBOX_IMAGE};

    private static final String[] MIMES = new String[] {
        ImagePDF.MIME_PDF};

    /** {@inheritDoc} */
    public String[] getSupportedMIMETypes() {
        return MIMES.clone();
    }

    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedFlavors(String mime) {
        return FLAVORS.clone();
    }

    /** {@inheritDoc} */
    public ImageLoader newImageLoader(ImageFlavor targetFlavor) {
        return new ImageLoaderPDF(targetFlavor);
    }

    /** {@inheritDoc} */
    @Override
    public int getUsagePenalty(String mime, ImageFlavor flavor) {
        return 10;
    }

    /** {@inheritDoc} */
    public boolean isAvailable() {
        try {
            Class.forName("org.apache.pdfbox.pdmodel.PDDocument");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

}
