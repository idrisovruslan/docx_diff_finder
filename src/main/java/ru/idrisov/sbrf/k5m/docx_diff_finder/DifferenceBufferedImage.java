package ru.idrisov.sbrf.k5m.docx_diff_finder;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.image.BufferedImage;

@Getter
@AllArgsConstructor
public class DifferenceBufferedImage {
    private BufferedImage bufferedImage;
    private int differencePixels;
}
