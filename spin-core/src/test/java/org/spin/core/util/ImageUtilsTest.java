package org.spin.core.util;

import org.junit.jupiter.api.Test;
import org.spin.core.util.file.FileType;
import org.spin.core.util.qrcode.QrCodeUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>Created by xuweinan on 2017/10/10.</p>
 *
 * @author xuweinan
 */
public class ImageUtilsTest {

//        @Test
    public void test() {
        try (InputStream is = new FileInputStream(new File("D:\\cat.jpg"))) {
            BufferedImage bufferedImage = ImageIO.read(is);
            BufferedImage radius = ImageUtils.radius(bufferedImage, 40, 5, Color.MAGENTA, 20);
            ImageUtils.writeImage(radius, FileType.Image.PNG, new File("D:\\d.png"));
        } catch (Exception e) {
        }
    }

//            @Test
    void testQr() throws IOException {
        String content = "aaaa";
        BufferedImage logo = QrCodeUtils.optimizeLogo(ImageIO.read(new File("D:\\cat.jpg")), 430, Color.WHITE);
        BufferedImage encode = QrCodeUtils.encode(content, 430, logo);
        ImageUtils.writeImage(encode, FileType.Image.PNG, new File("D:\\qr.png"));
    }

    //        @Test
    void testScale() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\d.png"));
        image = ImageUtils.scale(image, 70, 70, ImageUtils.ScaleMode.TILE, null, Transparency.TRANSLUCENT);
        ImageUtils.writeImage(image, FileType.Image.PNG, new File("D:\\e.png"));
    }

    //    @Test
    public void testCut() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\cat.jpg"));
        BufferedImage cut = ImageUtils.cut(image, 20, 20, 100, 100);
        ImageUtils.writeImage(cut, FileType.Image.PNG, new File("D:\\cut.png"));
    }
}
