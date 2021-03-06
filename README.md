# PDF IMAGE SUPPORT FOR APACHE FOP

**Table of Contents**

- [What's in here?](#whats-in-here)
- [Legal Information](#legal-information)
- [Where to get help?](#where-to-get-help)
- [How do I enable PDF image support in FOP?](#how-do-i-enable-pdf-image-support-in-fop)
- [Notes on PDF image support for output formats other than PDF](#notes-on-pdf-image-support-for-output-formats-other-than-pdf)
- [Known Issues](#known-issues)
- [Contributors](#contributors)
- [RELEASE NOTES](#release-notes)

## What's in here?

This package contains classes which add support using PDF images in
`fo:external-graphic` elements when you generate PDF files. This means
you can write something like:

```xml
  <fo:external-graphic src="my-doc.pdf"/>
```

Implementation notes:

- This package uses the [Apache PDFBox][] PDF library for parsing the
  PDF files as FOP's PDF library is a write-only library.

- PDF image support is done differently for PDF output than for the
  other output formats. For PDF output, the plug-in tries to transfer
  the various PDF objects 1:1 to the target PDF. For the other output
  formats, [Apache PDFBox][] is used to render the PDF to a Graphics2D
  object (Java2D). PDFBox still has some problems with rendering
  certain PDFs, so if you run into a problem with that, don't come to
  me. Rather, consider helping out in the PDFBox project to improve
  the code. This plug-in only plays the adapter between FOP and
  PDFBox. It is not responsible for correct rendering of the PDF.

- Individual pages inside a multi-page PDF can be accessed by using a URI
  fragment in the following form: <uri>#page=<nr>
  Example: http://localhost/mydoc.pdf#page=7
  Note: this only works on URIs, not plain file names in which case you'll
  get an error. Plain file names are illegal in XSL-FO anyway, strictly
  speaking.

- The PDF file must not be encrypted. Or else, you'll have to install an
  `OnLoadInterceptor`. Example:

  ```java
  Interceptors.getInstance().setOnLoad(new Decrypter());

  [..]

  private static class Decrypter implements OnLoadInterceptor {

      public PDDocument intercept(PDDocument doc, URI uri) throws IOException {
          if (doc.isEncrypted()) {
              DecryptionMaterial dec = new StandardDecryptionMaterial("password");
              try {
                  doc.openProtection(dec);
                  System.out.println("Decrypting: " + uri);
              } catch (Exception e) {
                  throw new RuntimeException("Error decrypting PDF", e);
              }
          }
          return doc;
      }

  }
  ```

  For more information on how to set decryption material, please see the PDFBox
  documentation.

- If you enable PDF/A or PDF/X functionality, the resulting PDF may not be
  a compliant file since the code isn't sophisticated enough to ensure that
  the rules of PDF/A and PDF/X are respected when the PDF file is embedded.
  So if you enable PDF/A and/or PDF/X, you should refrain from using this
  package.

- If the PDF you want to embed has annotations, be prepared that they may
  not be transferred correctly.

- Document structure (Tagged PDF/Accessibility) will not be preserved!

- Apache FOP currently generates PDF 1.4. If you include a PDF with a
  higher PDF version, the results may be unpredictable.

- This package does not work with FOP 0.93, 0.94 or any earlier version.
  You will need a later release or the code from FOP Trunk.

- Some PDF may not be processed correctly with FOP 0.95 due to bugs in
  its PDF library. You may need to upgrade Apache FOP in this case.

- Using PDF files is NOT supported inside SVG, only in XSL-FO.

## Legal Information

This package is published under the Apache License version 2.0. For the license
text, please see the following files:

- LICENSE
- NOTICE

## Where to get help?

See: http://xmlgraphics.apache.org/fop/gethelp.html

## How do I enable PDF image support in FOP?

Just add the `fop-pdf-images` as a dependency to your pom.xml.

You can then use URIs referring to PDF files inside `fo:external-graphic` and
`fox:external-document` elements.

## Notes on PDF image support for output formats other than PDF

Please note that this plug-in was written mostly for PDF production.
For other FOP output formats, the PDF is converted to a vector or
bitmap image using [Apache PDFBox][] which is not yet a full-fledged
PDF viewer. There may be limitations concerning the quality. If you
run into a problem displaying PDF for any output format other than
PDF, you will need to ask the PDFBox community for help. Or you can
help the Apache PDFBox project to improve their PDF interpreter.

[Apache PDFBox]: http://pdfbox.apache.org/

## Known Issues

- If Acrobat fails to open the generated PDF, it maybe be due to a bug in
  FOP that was fixed in revision 833375.

- When importing multiple pages from the same PDF, the same PDF objects from
  the original may be imported anew for each page. There is currently no
  easy way to avoid this.

## Contributors

- Jeremias Märki (original author)
- Krister Wicksell
- Vincent Hennebert

Versions prior to 2.1 (i.e. prior to the move to the Apache XML Graphics
project) can be downloaded from here:
http://www.jeremias-maerki.ch/development/fop/index.html


## RELEASE NOTES

### Version 2.1 (TBD)

Changes:

- Bugfix for handling of strings (the check method for US-ASCII had a bug)

### Version 2.0 (2010-10-28)

Changes:

- Moved from PDFBox (org.pdfbox) to Apache PDFBox (org.apache.pdfbox). Note
  that Apache PDFBox is currently in incubation at the ASF. More info at:
  http://incubator.apache.org/pdfbox/
- Added support for "null" objects in PDF.
- Added support for FOP's new intermediate format (and the new output
  implementations).
- Fixed transfer of untouched streams.
- Added an ImageConverter that extends Apache XML Graphics Commons' image
  loader framework with a converter from PDFBox to Java2D. This enables PDF
  display for all of FOP's output formats. See the restrictions mentioned above.
- Fix re-encoding problem for binary strings (for example lookup tables for
  /Indexed color spaces).
- Taken advantage of https://issues.apache.org/jira/browse/PDFBOX-507 to
  avoid warning messages in COSDocument's finalizer method.
- Fixed NullPointerException if a page has no content stream (empty page).
- Added limited support for AcroForms (PDF forms).
- Using CropBox instead of MediaBox, if available, to define the portion of the
  PDF to display.
- Added support for the /Rotation entry in the Page dictionary.
- Handled the case where the Resources dict of a page is missing and therefore
  inherited.
- Fixed a problem with Crop/MediaBox dictionaries not starting at coordinate 0,0.
- Added on-load hook to decrypt a PDF, for example.
- Added support for merging AcroForms (Thanks to Vincent Hennebert)
- Updated Apache PDFBox to version 0.8.0 to 1.3.1. The plug-in now requires at
  least J2SE 1.5!


### Version 1.3 (2008-11-28)

Changes:

- Fixed a NullPointerException when the MediaBox is inherited.
- An invalid page number is now properly handled.

### Version 1.2 (2008-04-04)

Changes:

- Fixed handling of rotated pages (Thanks to Krister Wicksell)
- Fixed "PDFObject already has an object number" error occurring with
  certain PDFs.

### Version 1.1a (2008-01-22)

Changes:

- Disabled the in-memory caching of PDF files due to an intermittent bug
  that appeared due to reuse of a PDDocument. This needs to be fixed in
  PDFBox.
- Change in PDFBox: Don't warn about that COSDocument isn't closed.
  Instead, the object is closed automatically when it's collected.

### Version 1.1 (2008-01-14)

Changes:

- Adjustments to changes in FOP Trunk after the introduction of a new image
  loading package (in Apache XML Graphics Commons).
- Support for addressing every single page in a PDF, not just the first.

Note: This version will only work with FOP Trunk (revision 611768 or later) or
with FOP 0.95 when it is released.

### Version 1.0 (2007-08-06)

This is the initial release. Feedback is welcome as this is very new code.

Note: Version 1.0 contains a modified version of PDFBox! I'll send a patch
with the change to the PDFBox project so hopefully the next version of PDFBox
will not need such a modification.

Note: Version 1.0 contains a development version of Apache FOP from the
following branch:
https://svn.apache.org/repos/asf/xmlgraphics/fop/branches/Temp_PDF_in_PDF/
As soon as the code is stabilized this will be merged into the Trunk.

