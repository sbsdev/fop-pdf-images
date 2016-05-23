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

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;

import org.apache.fop.render.pdf.pdfbox.PageParentTreeFinder;

import junit.framework.Assert;

public class PageParentTreeFinderTestCase {
    private static final String LINK = "/linkTagged.pdf";

    @Test
    public void testGetPageParentTreeArray() throws IOException {
	File resource = new File(getClass().getResource(LINK).getFile());
        PDDocument doc = PDDocument.load(resource);
        PDPage srcPage = doc.getPage(0);
        PageParentTreeFinder finder = new PageParentTreeFinder(srcPage);
        COSArray markedContentParents = finder.getPageParentTreeArray(doc);
        Assert.assertEquals(markedContentParents.size(), 3);
        COSObject firstObj = (COSObject)markedContentParents.get(0);
        COSObject secObj = (COSObject)markedContentParents.get(1);
        COSArray firstKids = (COSArray)firstObj.getDictionaryObject(COSName.K);
        COSDictionary firstKid = (COSDictionary) firstKids.get(0);
        int test = firstKid.getInt("MCID");
        int expected = 0;
        Assert.assertEquals(test, expected);
        COSDictionary firstKidBrother = (COSDictionary)firstKids.get(2);
        test = firstKidBrother.getInt("MCID");
        expected = 2;
        Assert.assertEquals(test, expected);
        COSArray secKidsArray = (COSArray)secObj.getDictionaryObject(COSName.K);
        COSDictionary secondKid = (COSDictionary)secKidsArray.get(0);
        test = secondKid.getInt("MCID");
        expected = 1;
        Assert.assertEquals(test, expected);
    }

    @Test
    public void testNoparentTreePresent() throws IOException {
        PDPage srcPage = new PDPage();
        srcPage.getCOSObject().setItem(COSName.STRUCT_PARENTS, COSInteger.get(-1));
        PDResources res = new PDResources();
        srcPage.setResources(res);
        PageParentTreeFinder finder = new PageParentTreeFinder(srcPage);
        COSArray parentTree = finder.getPageParentTreeArray(null);
        int test = parentTree.size();
        Assert.assertEquals(test, 0);
    }
}
