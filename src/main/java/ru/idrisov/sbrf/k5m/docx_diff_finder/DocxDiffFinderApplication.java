package ru.idrisov.sbrf.k5m.docx_diff_finder;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

@SpringBootApplication
public class DocxDiffFinderApplication implements CommandLineRunner {

    public Integer counter = 0;

    public static void main(String[] args) {
        SpringApplication.run(DocxDiffFinderApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        File pdf1 = convertDocxToPdf("src/main/resources/src1.docx", "target/pdf1.pdf");
        File pdf2 = convertDocxToPdf("src/main/resources/src2.docx", "target/pdf2.pdf");

        File img1 = convertPdfToPng(pdf1, "target/firstFile");
        File img2 = convertPdfToPng(pdf2, "target/secondFile");

        getDifferenceImages(img1, img2);

        System.out.println(counter);
    }

    private File convertPdfToPng(File docPath, String pngPath) throws IOException {
        PDDocument pd = PDDocument.load(docPath);
        PDFRenderer pr = new PDFRenderer(pd);

        if (!(new File(pngPath).exists())) {
            new File(pngPath).mkdir();
        }

        for (int page = 0; page < pd.getNumberOfPages(); ++page) {
            BufferedImage bi = pr.renderImageWithDPI(page, 300);
            ImageIO.write (bi, "JPEG", new File(pngPath + "/" + page + ".png"));
        }

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

    public void writeImage(BufferedImage image, String name) throws IOException {
        ImageIO.write(
                image,
                "png",
                new File("target/" + name + ".png"));
    }

    public void getDifferenceImages(File firstFolderWithImages, File secondFolderWithImages) throws IOException {

        if (firstFolderWithImages.listFiles().length != secondFolderWithImages.listFiles().length) {
            throw new RuntimeException();
        }

        for (int i = 0; i < firstFolderWithImages.listFiles().length; i++) {
            writeImage(getDifferenceImage(ImageIO.read(firstFolderWithImages.listFiles()[i]),
                    ImageIO.read(secondFolderWithImages.listFiles()[i])),
                    "diff" + i);
        }
    }

    public BufferedImage getDifferenceImage(BufferedImage img1, BufferedImage img2) {
        final int w = img1.getWidth(),
                h = img1.getHeight(),
                highlight = Color.MAGENTA.getRGB();
        final int[] p1 = img1.getRGB(0, 0, w, h, null, 0, w);
        final int[] p2 = img2.getRGB(0, 0, w, h, null, 0, w);

        for (int i = 0; i < p1.length; i++) {
            if (p1[i] != p2[i]) {
                counter++;
                p1[i] = highlight;
            }
        }

        final BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        out.setRGB(0, 0, w, h, p1, 0, w);
        return out;
    }
}
