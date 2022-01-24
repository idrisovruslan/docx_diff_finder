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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;

@SpringBootApplication
public class DocxDiffFinderApplication implements CommandLineRunner {

    private final String tempFolderString = "target/temp" + Calendar.getInstance().getTimeInMillis() + "/";
    private final String srcFolderString = "src/main/resources/";

    public static void main(String[] args) {
        SpringApplication.run(DocxDiffFinderApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        createFolders();

        File firstFolderWithImages = convertDocxToImages("src1.docx", "pdf1.pdf", "firstFile");
        File secondFolderWithImages = convertDocxToImages("src2.docx", "pdf2.pdf", "secondFile");

        getDifferenceImages(firstFolderWithImages, secondFolderWithImages);
    }

    private void createFolders() {
        createFolder(tempFolderString);
        createFolder(tempFolderString + "result");
    }

    private void createFolder(String tempFolderString) {
        File tempFolder = new File(tempFolderString);
        if (!tempFolder.exists()) {
            tempFolder.mkdir();
        }
    }

    private File convertDocxToImages(String docxFileName, String pdfFileName, String imagesFolderName) throws IOException {
        File srcFile = new File(srcFolderString + docxFileName);
        File pdfFile = new File(tempFolderString + pdfFileName);
        convertDocxToPdf(srcFile, pdfFile);

        File folderWithImages = new File(tempFolderString + imagesFolderName);
        fillFolderWithImagesFromPdf(pdfFile, folderWithImages);
        return folderWithImages;
    }

    public void convertDocxToPdf(File docPath, File pdfPath) throws IOException {
        InputStream doc = new FileInputStream(docPath);
        XWPFDocument document = new XWPFDocument(doc);
        PdfOptions options = PdfOptions.create();
        OutputStream out = new FileOutputStream(pdfPath);
        PdfConverter.getInstance().convert(document, out, options);
    }

    private void fillFolderWithImagesFromPdf(File docPath, File pngPath) throws IOException {
        PDDocument document = PDDocument.load(docPath);
        PDFRenderer renderer = new PDFRenderer(document);

        if (!pngPath.exists()) {
            pngPath.mkdir();
        }

        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage bi = renderer.renderImageWithDPI(page, 300);
            ImageIO.write(bi, "JPEG", new File(pngPath + "/" + page + ".png"));
        }
    }

    public void getDifferenceImages(File firstFolderWithImages, File secondFolderWithImages) throws IOException {
        File[] firstFolderImages = firstFolderWithImages.listFiles();
        File[] secondFolderImages = secondFolderWithImages.listFiles();

        if (firstFolderImages.length != secondFolderImages.length) {
            throw new RuntimeException();
        }

        Comparator<File> comparator = Comparator.comparingInt(o -> Integer.parseInt(o.getName().split("\\.png")[0]));

        Arrays.sort(firstFolderImages, comparator);
        Arrays.sort(secondFolderImages, comparator);

        for (int i = 0; i < firstFolderImages.length; i++) {
            DifferenceBufferedImage differenceImage = getDifferenceImage(
                    ImageIO.read(firstFolderImages[i]),
                    ImageIO.read(secondFolderImages[i])
            );

            writeImage(differenceImage.getBufferedImage(), i + "_diff_" + differenceImage.getDifferencePixels());
        }
    }

    public void writeImage(BufferedImage image, String name) throws IOException {
        ImageIO.write(
                image,
                "png",
                new File(tempFolderString + "result/" + name + ".png"));
    }

    public DifferenceBufferedImage getDifferenceImage(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        int highlight = Color.MAGENTA.getRGB();
        int[] p1 = img1.getRGB(0, 0, width, height, null, 0, width);
        int[] p2 = img2.getRGB(0, 0, width, height, null, 0, width);

        int counter = 0;
        for (int i = 0; i < p1.length; i++) {
            if (p1[i] != p2[i]) {
                counter++;
                p1[i] = highlight;
            }
        }

        final BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        out.setRGB(0, 0, width, height, p1, 0, width);
        return new DifferenceBufferedImage(out, counter);
    }
}
