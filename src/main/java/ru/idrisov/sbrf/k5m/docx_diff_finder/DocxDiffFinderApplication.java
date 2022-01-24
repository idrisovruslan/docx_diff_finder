package ru.idrisov.sbrf.k5m.docx_diff_finder;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.xmlgraphics.image.loader.impl.imageio.ImageIOUtil;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

@SpringBootApplication
public class DocxDiffFinderApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DocxDiffFinderApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        File pdf1 = convertDocxToPdf("src/main/resources/src1.docx", "target/pdf1.pdf");
        File pdf2 = convertDocxToPdf("src/main/resources/src2.docx", "target/pdf2.pdf");

        File img1 = convertPdfToPng("target/pdf1.pdf", "target/img1.png");
        File img2 = convertPdfToPng("target/pdf2.pdf", "target/img2.png");

        writeImage(getDifferenceImage(
                ImageIO.read(new File("target/img1.png")),
                ImageIO.read(new File("target/img2.png")))
        );
    }

    private File convertPdfToPng(String docPath, String pngPath) throws IOException {
        PDDocument pd = PDDocument.load (new File (docPath));
        PDFRenderer pr = new PDFRenderer (pd);
        BufferedImage bi = pr.renderImageWithDPI (0, 300);
        ImageIO.write (bi, "JPEG", new File (pngPath));

        return new File(pngPath);
    }



    public File convertDocxToPdf(String docPath, String pdfPath) throws IOException {
        InputStream doc = new FileInputStream(docPath);
        XWPFDocument document = new XWPFDocument(doc);
        PdfOptions options = PdfOptions.create();
        OutputStream out = new FileOutputStream(pdfPath);
        PdfConverter.getInstance().convert(document, out, options);

        return new File(pdfPath);
    }

    public void writeImage(BufferedImage image) throws IOException {
        ImageIO.write(
                image,
                "png",
                new File("target/diff.png"));
    }

    public BufferedImage getDifferenceImage(BufferedImage img1, BufferedImage img2) {
        final int w = img1.getWidth(),
                h = img1.getHeight(),
                highlight = Color.MAGENTA.getRGB();
        final int[] p1 = img1.getRGB(0, 0, w, h, null, 0, w);
        final int[] p2 = img2.getRGB(0, 0, w, h, null, 0, w);

        for (int i = 0; i < p1.length; i++) {
            if (p1[i] != p2[i]) {
                p1[i] = highlight;
            }
        }

        final BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        out.setRGB(0, 0, w, h, p1, 0, w);
        return out;
    }
}
