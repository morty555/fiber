package com.example.srp.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TiffConverterUtil {
    public static File convertTiffToPng(File tiffFile) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(tiffFile);
        File pngFile = null;
        try {
            pngFile = File.createTempFile("converted_", ".png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ImageIO.write(bufferedImage, "png", pngFile);
        return pngFile;
    }
}
