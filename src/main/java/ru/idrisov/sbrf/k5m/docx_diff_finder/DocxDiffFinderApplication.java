package ru.idrisov.sbrf.k5m.docx_diff_finder;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
        File theFile = new File("src/main/resources/src1.docx");
        System.out.println(theFile.getAbsolutePath());

        File img1 = convertToPngFromDocx("src/main/resources/src1.docx", "target/img1.png");
        File img2 = convertToPngFromDocx("src/main/resources/src2.docx", "target/img2.png");

//        writeImage(getDifferenceImage(
//                ImageIO.read(new File("target/img1.png")),
//                ImageIO.read(new File("target/img2.png")))
//        );
    }

    private File convertToPngFromDocx(String inPath, String outPath) throws IOException, Docx4JException {
        File theFile = new File(inPath);
        File outfile=new File(outPath);
        WordprocessingMLPackage wordMLPckg = Docx4J.load(theFile);
        OutputStream os = new FileOutputStream(outfile);
        FOSettings settings = Docx4J.createFOSettings();
        settings.setWmlPackage(wordMLPckg);
        settings.setApacheFopMime("images/png");
        Docx4J.toFO(settings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
        os.close();

        return outfile;
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
