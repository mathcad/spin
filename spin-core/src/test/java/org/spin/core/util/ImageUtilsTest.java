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

//    @Test
    public void test() {
        try (InputStream is = new FileInputStream(new File("D:\\cat.jpg"))) {
            BufferedImage bufferedImage = ImageIO.read(is);
            BufferedImage radius = ImageUtils.radius(bufferedImage, 10, 5, Color.MAGENTA, 20);
            ImageUtils.writeImage(radius, FileType.Image.PNG, new File("D:\\d.png"));
        } catch (Exception e) {
        }
    }

//        @Test
    void testQr() throws IOException {
        String content = "https://lanhuapp.com/web/#/item/project/product?pid=688abfb3-a19d-4f5c-9303-a869ab161f00&docId=77c0ff0f-af9d-4b93-a448-0c7a78557e11&docType=axure&pageId=079d66aaae8149ca9070923ef60d43b4&image_id=77c0ff0f-af9d-4b93-a448-0c7a78557e11&type=share_mark&tab=product&teamId=8686155b-cd59-45ec-aa35-3bbc6cb14f35&param=228528db-dcc4-4bd6-a9f9-61efaae33ce3&parentId=83aa7328-a3f0-4ea1-94a6-ae99eab3d067";
        BufferedImage logo = QrCodeUtils.optimizeLogo(ImageIO.read(new File("D:\\cat.jpg")), 1000, Color.WHITE);
        BufferedImage encode = QrCodeUtils.encode(content, 1000, logo);
        ImageUtils.writeImage(encode, FileType.Image.PNG, new File("D:\\qr.png"));
    }

    //    @Test
    void testScale() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\cat.jpg"));
        image = ImageUtils.scale(image, 50, 2000, ImageUtils.ScaleMode.FILL, null, Transparency.TRANSLUCENT);
        ImageUtils.writeImage(image, FileType.Image.PNG, new File("D:\\d.png"));
    }
}
