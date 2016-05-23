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

package org.apache.fop.render.pdf;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.io.IOUtils;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.type1.Type1Font;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.image.loader.util.SoftMapCache;
import org.apache.xmlgraphics.java2d.GeneralGraphics2DImagePainter;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.pdf.PDFAnnotList;
import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFGState;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.render.pdf.pdfbox.FOPPDFMultiByteFont;
import org.apache.fop.render.pdf.pdfbox.FOPPDFSingleByteFont;
import org.apache.fop.render.pdf.pdfbox.ImageConverterPDF2G2D;
import org.apache.fop.render.pdf.pdfbox.ImagePDF;
import org.apache.fop.render.pdf.pdfbox.PDFBoxAdapter;
import org.apache.fop.render.pdf.pdfbox.PDFBoxImageHandler;
import org.apache.fop.render.pdf.pdfbox.PSPDFGraphics2D;
import org.apache.fop.render.ps.PSDocumentHandler;
import org.apache.fop.render.ps.PSImageFormResource;
import org.apache.fop.render.ps.PSRenderingUtil;

import junit.framework.Assert;

public class PDFBoxAdapterTestCase {
    private Rectangle2D r = new Rectangle2D.Double();
    private static final String CFF1 = "/2fonts.pdf";
    private static final String CFF2 = "/2fonts2.pdf";
    private static final String CFF3 = "/simpleh.pdf";
    private static final String TTCID1 = "/ttcid1.pdf";
    private static final String TTCID2 = "/ttcid2.pdf";
    private static final String TTSubset1 = "/ttsubset.pdf";
    private static final String TTSubset2 = "/ttsubset2.pdf";
    private static final String TTSubset3 = "/ttsubset3.pdf";
    private static final String TTSubset5 = "/ttsubset5.pdf";
    private static final String CFFCID1 = "/cffcid1.pdf";
    private static final String CFFCID2 = "/cffcid2.pdf";
    private static final String Type1Subset1 = "/t1subset.pdf";
    private static final String Type1Subset2 = "/t1subset2.pdf";
    private static final String Type1Subset3 = "/t1subset3.pdf";
    private static final String Type1Subset4 = "/t1subset4.pdf";
    protected static final String ROTATE = "/rotate.pdf";
    private static final String SHADING = "/shading.pdf";
    private static final String LINK = "/link.pdf";
    private static final String IMAGE = "/image.pdf";
    private static final String HELLOTagged = "/taggedWorld.pdf";
    private static final String XFORM = "/xform.pdf";
    private static final String LOOP = "/loop.pdf";

    private PDFBoxAdapter getPDFBoxAdapter() {
        PDFDocument doc = new PDFDocument("");
        PDFPage pdfpage = new PDFPage(new PDFResources(doc), 0, r, r, r, r);
        doc.setMergeFontsEnabled(true);
        pdfpage.setDocument(doc);
        pdfpage.setObjectNumber(1);
        return new PDFBoxAdapter(pdfpage, new HashMap(), new HashMap<Integer, PDFArray>());
    }

    @Ignore("Seems to fail for some reason")
    @Test
    public void testPDFWriter() throws Exception {
        FontInfo fi = new FontInfo();
        String msg = writeText(fi, CFF3);
        Assert.assertTrue(msg, msg.contains("/Myriad_Pro"));
        Assert.assertEquals(fi.getUsedFonts().size(), 2);
        msg = writeText(fi, TTSubset1);
        Assert.assertTrue(msg, msg.contains("<74>-0.168 <65>-0.1523 <73>0.1528 <74>277.832"));
        msg = writeText(fi, TTSubset2);
        Assert.assertTrue(msg, msg.contains("(t)-0.168 (e)-0.1523 (s)0.1528 (t)"));
        msg = writeText(fi, TTSubset3);
        Assert.assertTrue(msg, msg.contains("[<0001>3 <0002>-7 <0003>] TJ"));
        msg = writeText(fi, TTSubset5);
        Assert.assertTrue(msg, msg.contains("[<0003>2 <0004>-7 <0007>] TJ"));
        msg = writeText(fi, TTCID1);
        Assert.assertTrue(msg, msg.contains("<0028003B0034003000420034>"));
        msg = writeText(fi, TTCID2);
        Assert.assertTrue(msg, msg.contains("<000F00100001002A0034003F00430034003C00310034004100010010000E000F0011>"));
        msg = writeText(fi, CFFCID1);
        Assert.assertTrue(msg, msg.contains("/Fm01700251251 Do"));
        msg = writeText(fi, CFFCID2);
        Assert.assertTrue(msg, msg.contains("/Fm01701174772 Do"));
        msg = writeText(fi, Type1Subset1);
        Assert.assertTrue(msg, msg.contains("/Verdana_Type1"));
        msg = writeText(fi, Type1Subset2);
        Assert.assertTrue(msg, msg.contains("[(2nd example)] TJ"));
        msg = writeText(fi, Type1Subset3);
        Assert.assertTrue(msg, msg.contains("/URWChanceryL-MediItal_Type1 20 Tf"));
        msg = writeText(fi, Type1Subset4);
        Assert.assertTrue(msg, msg.contains("/F15-1521012718 40 Tf"));

        for (Typeface font : fi.getUsedFonts().values()) {
            InputStream is = ((CustomFont) font).getInputStream();
            if (font.getFontType() == FontType.TYPE1C || font.getFontType() == FontType.CIDTYPE0) {
                byte[] data = IOUtils.toByteArray(is);
                CFFParser p = new CFFParser();
                p.parse(data).get(0);
            } else if (font.getFontType() == FontType.TRUETYPE) {
                TTFParser parser = new TTFParser();
                parser.parse(is);
            } else if (font.getFontType() == FontType.TYPE0) {
                TTFParser parser = new TTFParser(true);
                parser.parse(is);
            } else if (font.getFontType() == FontType.TYPE1) {
                Type1Font.createWithPFB(is);
            }
            Assert.assertTrue(((CustomFont) font).isEmbeddable());
            if (font instanceof MultiByteFont) {
                Assert.assertTrue(((MultiByteFont) font).getWidthsMap() != null);
            } else {
                Assert.assertFalse(((CustomFont)font).isSymbolicFont());
            }
        }
    }

    private String writeText(FontInfo fi, String pdf) throws IOException {
        PDDocument doc = getResource(pdf);
        PDPage page = (PDPage) doc.getDocumentCatalog().getPages().get(0);
        AffineTransform at = new AffineTransform();
        String c = getPDFBoxAdapter().createStreamFromPDFBoxPage(doc, page, pdf, at, fi, new Rectangle());
//        PDResources sourcePageResources = page.findResources();
//        COSDictionary fonts = (COSDictionary)sourcePageResources.getCOSDictionary().getDictionaryObject(COSName.FONT);
//        PDFBoxAdapter.PDFWriter w = adapter. new MergeFontsPDFWriter(fonts, fi, "", new ArrayList<COSName>());
//        String c = w.writeText(page.getContents());
        doc.close();
        return c;
    }

    private COSDictionary getFont(PDDocument doc, String internalname) throws IOException {
        PDPage page = (PDPage) doc.getDocumentCatalog().getPages().get(0);
        PDResources sourcePageResources = page.getResources();
        COSDictionary fonts = (COSDictionary)sourcePageResources.getCOSObject().getDictionaryObject(COSName.FONT);
        return (COSDictionary) fonts.getDictionaryObject(internalname);
    }

    private PDDocument getResource(String pdf) throws IOException {
        return PDDocument.load(new File(getClass().getResource(pdf).getFile()));
    }
    
    @Test
    public void testCFF() throws Exception {
        PDDocument doc = getResource(CFF1);
        FOPPDFSingleByteFont sbfont = new FOPPDFSingleByteFont(getFont(doc, "R11"),
                "MyriadPro-Regular_Type1f0encstdcs");

        Assert.assertTrue(Arrays.asList(sbfont.getEncoding().getCharNameMap()).contains("bracketright"));
        Assert.assertTrue(!Arrays.asList(sbfont.getEncoding().getCharNameMap()).contains("A"));
        Assert.assertTrue(!Arrays.toString(sbfont.getEncoding().getUnicodeCharMap()).contains("A"));
        Assert.assertEquals(sbfont.mapChar('A'), 0);
        Assert.assertEquals(sbfont.getWidths().length, 28);
        Assert.assertEquals(sbfont.getFirstChar(), 87);
        Assert.assertEquals(sbfont.getLastChar(), 114);

        PDDocument doc2 = getResource(CFF2);
        String name = sbfont.addFont(getFont(doc2, "R11"));
        Assert.assertTrue(name.contains("MyriadPro"));

        Assert.assertEquals(sbfont.getFontName(), "MyriadPro-Regular_Type1f0encstdcs");
        Assert.assertEquals(sbfont.getEncodingName(), "WinAnsiEncoding");
        Assert.assertEquals(sbfont.mapChar('W'), 'W');
        String x = IOUtils.toString(sbfont.getInputStream());
        Assert.assertTrue(x, x.contains("Adobe Systems"));
        Assert.assertEquals(sbfont.getEncoding().getName(), "FOPPDFEncoding");
        Assert.assertTrue(Arrays.asList(sbfont.getEncoding().getCharNameMap()).contains("A"));
        Assert.assertEquals(sbfont.getWidths().length, 65);
        Assert.assertEquals(sbfont.getFirstChar(), 50);
        Assert.assertEquals(sbfont.getLastChar(), 114);

        Assert.assertEquals(sbfont.addFont(getFont(doc2, "R13")), null);

        doc.close();
        doc2.close();
    }

    @Test
    public void testCFF2() throws Exception {
        PDDocument doc = getResource(CFF3);
        FOPPDFSingleByteFont sbfont = new FOPPDFSingleByteFont(getFont(doc, "T1_0"),
                "Myriad_Pro_Type1f0encf1cs");
        Assert.assertTrue(Arrays.asList(sbfont.getEncoding().getCharNameMap()).contains("uni004E"));
        Assert.assertEquals(sbfont.getFontName(), "Myriad_Pro_Type1f0encf1cs");
        Assert.assertEquals(sbfont.getEncodingName(), null);
        byte[] is = IOUtils.toByteArray(sbfont.getInputStream());

        CFFParser p = new CFFParser();
        CFFFont ff = p.parse(is).get(0);
        Assert.assertEquals(ff.getName(), "MNEACN+Myriad_Pro");
//        Assert.assertEquals(ff.getCharset().getEntries().get(0).getSID(), 391);

        doc.close();
    }

    @Test
    public void testTTCID() throws Exception {
        PDDocument doc = getResource(TTCID1);
        FOPPDFMultiByteFont mbfont = new FOPPDFMultiByteFont(getFont(doc, "C2_0"),
                "ArialMT_Type0");
        mbfont.addFont(getFont(doc, "C2_0"));
        Assert.assertEquals(mbfont.mapChar('t'), 67);

        PDDocument doc2 = getResource(TTCID2);
        String name = mbfont.addFont(getFont(doc2, "C2_0"));
        Assert.assertEquals(name, "ArialMT_Type0");
        Assert.assertEquals(mbfont.getFontName(), "ArialMT_Type0");
        byte[] is = IOUtils.toByteArray(mbfont.getInputStream());
        Assert.assertEquals(is.length, 38640);
        doc.close();
        doc2.close();
    }

    @Test
    public void testTTSubset() throws Exception {
        PDDocument doc = getResource(TTSubset1);
        FOPPDFSingleByteFont mbfont = new FOPPDFSingleByteFont(getFont(doc, "R9"),
                "TimesNewRomanPSMT_TrueType");
        mbfont.addFont(getFont(doc, "R9"));
//        Assert.assertEquals(mbfont.mapChar('t'), 116);

        PDDocument doc2 = getResource(TTSubset2);
        String name = mbfont.addFont(getFont(doc2, "R9"));
        Assert.assertEquals(name, "TimesNewRomanPSMT_TrueType");
        Assert.assertEquals(mbfont.getFontName(), "TimesNewRomanPSMT_TrueType");
        byte[] is = IOUtils.toByteArray(mbfont.getInputStream());
        Assert.assertEquals(is.length, 41112);
        doc.close();
        doc2.close();
    }

    @Test
    public void testType1Subset() throws Exception {
        PDDocument doc = getResource(Type1Subset1);
        FOPPDFSingleByteFont mbfont = new FOPPDFSingleByteFont(getFont(doc, "F15"), "");
        mbfont.addFont(getFont(doc, "F15"));
        PDDocument doc2 = getResource(Type1Subset2);
        mbfont.addFont(getFont(doc2, "F15"));
        Type1Font f = Type1Font.createWithPFB(mbfont.getInputStream());
        Set<String> csDict = new TreeSet<String>(f.getCharStringsDict().keySet());
        Assert.assertEquals(csDict.toString(), "[.notdef, a, d, e, hyphen, l, m, n, p, s, space, t, two, x]");
        Assert.assertEquals(f.getSubrsArray().size(), 518);
        Assert.assertEquals(f.getFamilyName(), "Verdana");
        doc.close();
        doc2.close();
    }

    @Test
    public void testStream() throws Exception {
        PDFDocument pdfdoc = new PDFDocument("");
        PDFPage pdfpage = new PDFPage(new PDFResources(pdfdoc), 0, r, r, r, r);
        pdfpage.setDocument(pdfdoc);
        PDFBoxAdapter adapter = new PDFBoxAdapter(pdfpage, new HashMap(), new HashMap<Integer, PDFArray>());
        PDDocument doc = getResource(ROTATE);
        PDPage page = (PDPage) doc.getDocumentCatalog().getPages().get(0);
        AffineTransform at = new AffineTransform();
        Rectangle r = new Rectangle(0, 1650, 842000, 595000);
        String stream = adapter.createStreamFromPDFBoxPage(doc, page, "key", at, null, r);
        Assert.assertEquals(at, new AffineTransform(-0.0, 1.0000000554888686, 1.0000000554888686, 0.0, 0.0,
                -2.0742416381835938E-5));
        Assert.assertTrue(stream.contains("/GS0106079 gs"));
        Assert.assertTrue(stream.contains("/TT0106079 1 Tf"));
        doc.close();
    }

    @Test
    public void testTaggedPDFWriter() throws IOException {
        PDFDocument pdfdoc = new PDFDocument("");
        PDFPage pdfpage = new PDFPage(new PDFResources(pdfdoc), 0, r, r, r, r);
        pdfpage.setDocument(pdfdoc);
        PDFBoxAdapter adapter = new PDFBoxAdapter(pdfpage, new HashMap(), new HashMap<Integer, PDFArray>());
        adapter.setCurrentMCID(5);
        PDDocument doc = getResource(HELLOTagged);
        PDPage page = (PDPage) doc.getDocumentCatalog().getPages().get(0);
        AffineTransform at = new AffineTransform();
        Rectangle r = new Rectangle(0, 1650, 842000, 595000);
        String stream = adapter.createStreamFromPDFBoxPage(doc, page, "key", at, null, r);
        Assert.assertTrue(stream, stream.contains("/P <</MCID 5 >>BDC"));
        doc.close();
    }

    @Test
    public void testLink() throws Exception {
        PDFDocument pdfdoc = new PDFDocument("");
        PDFPage pdfpage = new PDFPage(new PDFResources(pdfdoc), 0, r, r, r, r);
        pdfpage.setDocument(pdfdoc);
        pdfpage.setObjectNumber(1);
        Map<Integer, PDFArray> pageNumbers = new HashMap<Integer, PDFArray>();
        PDFBoxAdapter adapter = new PDFBoxAdapter(pdfpage, new HashMap(), pageNumbers);
        PDDocument doc = getResource(LINK);
        PDPage page = (PDPage) doc.getDocumentCatalog().getPages().get(0);
        AffineTransform at = new AffineTransform();
        Rectangle r = new Rectangle(0, 1650, 842000, 595000);
        String stream = adapter.createStreamFromPDFBoxPage(doc, page, "key", at, null, r);
        Assert.assertTrue(stream.contains("/Link <</MCID 5 >>BDC"));
        Assert.assertEquals(pageNumbers.size(), 4);
        PDFAnnotList annots = (PDFAnnotList) pdfpage.get("Annots");
        Assert.assertEquals(annots.toPDFString(), "[\n1 0 R\n2 0 R\n]");
        doc.close();
    }

    @Test
    public void testXform() throws Exception {
        PDFDocument pdfdoc = new PDFDocument("");
        pdfdoc.getFilterMap().put(PDFFilterList.DEFAULT_FILTER, Arrays.asList("null"));
        pdfdoc.setMergeFontsEnabled(true);
        PDFPage pdfpage = new PDFPage(new PDFResources(pdfdoc), 0, r, r, r, r);
        pdfpage.setDocument(pdfdoc);
        pdfpage.setObjectNumber(1);
        Map<Integer, PDFArray> pageNumbers = new HashMap<Integer, PDFArray>();
        PDFBoxAdapter adapter = new PDFBoxAdapter(pdfpage, new HashMap(), pageNumbers);
        PDDocument doc = getResource(XFORM);
        PDPage page = (PDPage) doc.getDocumentCatalog().getPages().get(0);
        AffineTransform at = new AffineTransform();
        Rectangle r = new Rectangle(0, 1650, 842000, 595000);
        adapter.createStreamFromPDFBoxPage(doc, page, "key", at, new FontInfo(), r);
        doc.close();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        pdfdoc.output(bos);
        Assert.assertFalse(bos.toString("UTF-8").contains("/W 5 /H 5 /BPC 8 /CS /RGB ID ÿÿÿ"));
    }

    @Test
    public void testPSPDFGraphics2D() throws Exception {
        ByteArrayOutputStream stream = pdfToPS(IMAGE);
        Assert.assertTrue(stream.toString("UTF-8"),
                stream.toString("UTF-8").contains("%%IncludeResource: form FOPForm:0\nFOPForm:0 execform"));

        pdfToPS(CFF1);
        pdfToPS(CFF2);
        pdfToPS(CFF3);
        pdfToPS(TTCID1);
        pdfToPS(TTCID2);
        pdfToPS(TTSubset1);
        pdfToPS(TTSubset2);
        pdfToPS(TTSubset3);
        pdfToPS(TTSubset5);
        pdfToPS(CFFCID1);
        pdfToPS(CFFCID2);
        pdfToPS(Type1Subset1);
        pdfToPS(Type1Subset2);
        pdfToPS(Type1Subset3);
        pdfToPS(Type1Subset4);
        pdfToPS(ROTATE);
        pdfToPS(LINK);
        pdfToPS(LOOP);
    }

    @Test
    public void testPDFToPDF() throws IOException {
        FontInfo fi = new FontInfo();
        writeText(fi, CFF1);
        writeText(fi, CFF2);
        writeText(fi, CFF3);
        writeText(fi, CFFCID1);
        writeText(fi, CFFCID2);
        writeText(fi, IMAGE);
        writeText(fi, LINK);
        writeText(fi, ROTATE);
        writeText(fi, SHADING);
        writeText(fi, TTCID1);
        writeText(fi, TTCID2);
        writeText(fi, TTSubset1);
        writeText(fi, TTSubset2);
        writeText(fi, TTSubset3);
        writeText(fi, TTSubset5);
        writeText(fi, Type1Subset1);
        writeText(fi, Type1Subset2);
        writeText(fi, Type1Subset3);
        writeText(fi, Type1Subset4);
        writeText(fi, LOOP);
    }

    private ByteArrayOutputStream pdfToPS(String pdf) throws IOException, ImageException {
        ImageConverterPDF2G2D i = new ImageConverterPDF2G2D();
        ImageInfo imgi = new ImageInfo("a", "b");
        PDDocument doc = getResource(pdf);
        org.apache.xmlgraphics.image.loader.Image img = new ImagePDF(imgi, doc);
        ImageGraphics2D ig = (ImageGraphics2D)i.convert(img, null);
        GeneralGraphics2DImagePainter g = (GeneralGraphics2DImagePainter) ig.getGraphics2DImagePainter();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PSPDFGraphics2D g2d = (PSPDFGraphics2D) g.getGraphics(true, new FOPPSGeneratorImpl(stream));
        Rectangle2D rect = new Rectangle2D.Float(0, 0, 100, 100);
        GraphicContext gc = new GraphicContext();
        g2d.setGraphicContext(gc);
        ig.getGraphics2DImagePainter().paint(g2d, rect);
        doc.close();
        return stream;
    }

    static class FOPPSGeneratorImpl extends PSGenerator implements PSDocumentHandler.FOPPSGenerator {
        public FOPPSGeneratorImpl(OutputStream out) {
            super(out);
        }

        public PSDocumentHandler getHandler() {
            PSDocumentHandler handler = mock(PSDocumentHandler.class);
            PSRenderingUtil util = mock(PSRenderingUtil.class);
            when(util.isOptimizeResources()).thenReturn(true);
            when(handler.getPSUtil()).thenReturn(util);
            FOUserAgent mockedAgent = mock(FOUserAgent.class);
            when(handler.getUserAgent()).thenReturn(mockedAgent);
            when(mockedAgent.getTargetResolution()).thenReturn(72f);
            when(handler.getFormForImage(any(String.class))).thenReturn(new PSImageFormResource(0, ""));
            return handler;
        }

        public BufferedOutputStream getTempStream(URI uri) throws IOException {
            return new BufferedOutputStream(new ByteArrayOutputStream());
        }

        public Map<Integer, URI> getImages() {
            return new HashMap<Integer, URI>();
        }
    }

    @Test
    public void testPDFBoxImageHandler() throws Exception {
        ImageInfo imgi = new ImageInfo("a", "b");
        PDDocument doc = getResource(SHADING);
        ImagePDF img = new ImagePDF(imgi, doc);
        PDFDocument pdfdoc = new PDFDocument("");
        PDFPage pdfpage = new PDFPage(new PDFResources(pdfdoc), 0, r, r, r, r);
        pdfpage.setDocument(pdfdoc);
        PDFGState g = new PDFGState();
        pdfdoc.assignObjectNumber(g);
        pdfpage.addGState(g);
        PDFContentGenerator con = new PDFContentGenerator(pdfdoc, null, null);
        FOUserAgent mockedAgent = mock(FOUserAgent.class);
        when(mockedAgent.isAccessibilityEnabled()).thenReturn(false);
        when(mockedAgent.getPDFObjectCache()).thenReturn(new SoftMapCache(true));
        PDFRenderingContext c = new PDFRenderingContext(mockedAgent, con, pdfpage, null);
        c.setPageNumbers(new HashMap<Integer, PDFArray>());
        new PDFBoxImageHandler().handleImage(c, img, new Rectangle());
        PDFResources res = c.getPage().getPDFResources();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        res.output(bos);
        Assert.assertTrue(bos.toString("UTF-8").contains("/ExtGState << /GS1"));
    }
}
