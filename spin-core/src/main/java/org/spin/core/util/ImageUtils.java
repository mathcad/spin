package org.spin.core.util;

import org.spin.core.ErrorCode;
import org.spin.core.security.Base64;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.file.FileType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 图像工具
 * <p>Created by xuweinan on 2017/10/10.</p>
 *
 * @author xuweinan
 */
public abstract class ImageUtils {
    private ImageUtils() {
    }

    public enum ScaleMode {
        /**
         * 拉伸
         */
        STRENTCH,

        /**
         * 填充
         */
        FILL,

        /**
         * 平铺
         */
        TILE
    }

    public static BufferedImage scale(Image image, int width, int height, ScaleMode mode, Color fillColor, Integer transparency) {
        int originWidth = image.getWidth(null);
        int originHeight = image.getHeight(null);

        // 尺寸不变直接返回
        if (originWidth == width && originHeight == height) {
            return toBufferedImage(image, false, null);
        }

        switch (mode) {
            case STRENTCH:
                Image image_scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return toBufferedImage(image_scaled, false, null);
                break;
            case FILL:
                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                g.setColor(fillColor);
                g.fillRect(0, 0, width, height);

                int targetWidth;
                int targetHeight;
                if (width < originWidth || height < originHeight) {
                    // 缩小
                    if (width < originWidth) {
                        targetWidth = width;
                        targetHeight = originHeight * width / originWidth;
                    } else {
                        targetWidth = originWidth * height / originHeight;
                        targetHeight = height;
                    }
                } else {
                    // 放大
                    if (width > originWidth) {
                        targetWidth = width;
                        targetHeight = originHeight * width / originWidth;
                    } else {
                        targetWidth = originWidth * height / originHeight;
                        targetHeight = height;
                    }
                }
                Image scaledInstance = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                if (width == targetWidth) {
                    g.drawImage(scaledInstance, 0, (height - targetHeight) / 2, targetWidth, targetHeight, fillColor, null);
                } else {
                    g.drawImage(scaledInstance, (width - targetWidth) / 2, 0, targetWidth, targetHeight, fillColor, null);
                }
                g.dispose();
                return img;
            break;
            case TILE:
                break;
        }

        double ratioX; // 缩放比例
        double ratioY; // 缩放比例
        BufferedImage bi = toBufferedImage(image, false, transparency);
        Image image_scaled = bi.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        // 计算比例
        if ((bi.getHeight() > height) || (bi.getWidth() > width)) {
            if (bi.getHeight() > bi.getWidth()) {
                ratioY = (Integer.valueOf(height)).doubleValue() / bi.getHeight();
            } else {
                ratioX = (Integer.valueOf(width)).doubleValue() / bi.getWidth();
            }
            AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(ratioX, ratioY), null);
            image_scaled = op.filter(bi, null);
        }
        if (bb) {//补白
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, width, height);
            if (width == image_scaled.getWidth(null))
                g.drawImage(image_scaled, 0, (height - image_scaled.getHeight(null)) / 2,
                    image_scaled.getWidth(null), image_scaled.getHeight(null),
                    Color.white, null);
            else
                g.drawImage(image_scaled, (width - image_scaled.getWidth(null)) / 2, 0,
                    image_scaled.getWidth(null), image_scaled.getHeight(null),
                    Color.white, null);
            g.dispose();
            image_scaled = img;
        }
        return ImageUtils.toBufferedImage(image_scaled, false, transparency);
    }


    public static BufferedImage scale(Image srcImg, float scale, Integer transparency) {
        int width = Math.round(srcImg.getWidth(null) * scale);
        int height = Math.round(srcImg.getHeight(null) * scale);
        return scale(srcImg, width, height, false, transparency);
    }

    public static void cut(String srcImageFile, String result, int x, int y, int width, int height) {
        try {
            // 读取源图像
            BufferedImage bi = ImageIO.read(new File(srcImageFile));
            int srcWidth = bi.getHeight(); // 源图宽度
            int srcHeight = bi.getWidth(); // 源图高度
            if (srcWidth > 0 && srcHeight > 0) {
                Image image = bi.getScaledInstance(srcWidth, srcHeight,
                    Image.SCALE_DEFAULT);
                // 四个参数分别为图像起点坐标和宽高
                // 即: CropImageFilter(int x,int y,int width,int height)
                ImageFilter cropFilter = new CropImageFilter(x, y, width, height);
                Image img = Toolkit.getDefaultToolkit().createImage(
                    new FilteredImageSource(image.getSource(),
                        cropFilter));
                BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics g = tag.getGraphics();
                g.drawImage(img, 0, 0, width, height, null); // 绘制切割后的图
                g.dispose();
                // 输出为文件
                ImageIO.write(tag, "JPEG", new File(result));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void cut2(String srcImageFile, String descDir, int rows, int cols) {
        try {
            if (rows <= 0 || rows > 20) rows = 2; // 切片行数
            if (cols <= 0 || cols > 20) cols = 2; // 切片列数
            // 读取源图像
            BufferedImage bi = ImageIO.read(new File(srcImageFile));
            int srcWidth = bi.getHeight(); // 源图宽度
            int srcHeight = bi.getWidth(); // 源图高度
            if (srcWidth > 0 && srcHeight > 0) {
                Image img;
                ImageFilter cropFilter;
                Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
                int destWidth; // 每张切片的宽度
                int destHeight; // 每张切片的高度
                // 计算切片的宽度和高度
                if (srcWidth % cols == 0) {
                    destWidth = srcWidth / cols;
                } else {
                    destWidth = srcWidth / cols + 1;
                }
                if (srcHeight % rows == 0) {
                    destHeight = srcHeight / rows;
                } else {
                    destHeight = srcWidth / rows + 1;
                }
                // 循环建立切片
                // 改进的想法:是否可用多线程加快切割速度
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        // 四个参数分别为图像起点坐标和宽高
                        // 即: CropImageFilter(int x,int y,int width,int height)
                        cropFilter = new CropImageFilter(j * destWidth, i * destHeight,
                            destWidth, destHeight);
                        img = Toolkit.getDefaultToolkit().createImage(
                            new FilteredImageSource(image.getSource(),
                                cropFilter));
                        BufferedImage tag = new BufferedImage(destWidth,
                            destHeight, BufferedImage.TYPE_INT_RGB);
                        Graphics g = tag.getGraphics();
                        g.drawImage(img, 0, 0, null); // 绘制缩小后的图
                        g.dispose();
                        // 输出为文件
                        ImageIO.write(tag, "JPEG", new File(descDir
                            + "_r" + i + "_c" + j + ".jpg"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void cut3(String srcImageFile, String descDir, int destWidth, int destHeight) {
        try {
            if (destWidth <= 0) destWidth = 200; // 切片宽度
            if (destHeight <= 0) destHeight = 150; // 切片高度
            // 读取源图像
            BufferedImage bi = ImageIO.read(new File(srcImageFile));
            int srcWidth = bi.getHeight(); // 源图宽度
            int srcHeight = bi.getWidth(); // 源图高度
            if (srcWidth > destWidth && srcHeight > destHeight) {
                Image img;
                ImageFilter cropFilter;
                Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
                int cols = 0; // 切片横向数量
                int rows = 0; // 切片纵向数量
                // 计算切片的横向和纵向数量
                if (srcWidth % destWidth == 0) {
                    cols = srcWidth / destWidth;
                } else {
                    cols = srcWidth / destWidth + 1;
                }
                if (srcHeight % destHeight == 0) {
                    rows = srcHeight / destHeight;
                } else {
                    rows = srcHeight / destHeight + 1;
                }
                // 循环建立切片
                // 改进的想法:是否可用多线程加快切割速度
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        // 四个参数分别为图像起点坐标和宽高
                        // 即: CropImageFilter(int x,int y,int width,int height)
                        cropFilter = new CropImageFilter(j * destWidth, i * destHeight,
                            destWidth, destHeight);
                        img = Toolkit.getDefaultToolkit().createImage(
                            new FilteredImageSource(image.getSource(),
                                cropFilter));
                        BufferedImage tag = new BufferedImage(destWidth,
                            destHeight, BufferedImage.TYPE_INT_RGB);
                        Graphics g = tag.getGraphics();
                        g.drawImage(img, 0, 0, null); // 绘制缩小后的图
                        g.dispose();
                        // 输出为文件
                        ImageIO.write(tag, "JPEG", new File(descDir
                            + "_r" + i + "_c" + j + ".jpg"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage gray(Image image) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);
        return op.filter(toBufferedImage(image, true, null), null);
    }

    public static BufferedImage pressText(Image image, String pressText, Font font, int fontStyle, Color color, float fontSize, int x, int y, float alpha) {
        BufferedImage bufferedImage = toBufferedImage(image, true, null);
        pressText(bufferedImage, pressText, font, fontStyle, color, fontSize, x, y, alpha);
        return bufferedImage;
    }

    public static void pressText(BufferedImage bufferedImage, String pressText, Font font, int fontStyle, Color color, float fontSize, int x, int y, float alpha) {
        try {
            Font dynamicFontPt = font.deriveFont(fontStyle, fontSize);
            Graphics2D g = bufferedImage.createGraphics();
            g.setColor(color);
            g.setFont(dynamicFontPt);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            // 在指定坐标绘制水印文字
            g.drawString(pressText, x, y);
            g.dispose();
        } catch (Exception e) {
            throw new SimplifiedException(e);
        }
    }

    public static BufferedImage overlay(Image srcImage, Image overlay, int x, int y, float alpha) {
        BufferedImage image = toBufferedImage(srcImage, true, null);
        overlay(image, overlay, x, y, alpha);
        return image;
    }

    public static void overlay(BufferedImage image, Image overlay, int x, int y, float alpha) {
        Graphics2D g = image.createGraphics();
        int wideth = overlay.getWidth(null);
        int height = overlay.getHeight(null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        g.drawImage(overlay, x, y, wideth, height, null);
        g.dispose();
    }

    /**
     * 图片设置圆角
     *
     * @param srcImage    源图像
     * @param radius
     * @param border
     * @param borderColor
     * @param padding
     * @return
     */
    public static BufferedImage radius(Image srcImage, int radius, int border, Color borderColor, int padding) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        int trans = Transparency.TRANSLUCENT;
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gs.getDefaultConfiguration();
        BufferedImage bimage = gc.createCompatibleImage(srcImage.getWidth(null) + padding * 2, srcImage.getHeight(null) + padding * 2, trans);

//        BufferedImage image = toBufferedImage(srcImage, false, Transparency.TRANSLUCENT);

        Graphics2D g2 = bimage.createGraphics();
//        g2.setComposite(AlphaComposite.Clear);
//        g2.fill(new Rectangle(image.getWidth(), image.getHeight()));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));

        int width = srcImage.getWidth(null);
        int height = srcImage.getHeight(null);

//        Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, image.getWidth(), image.getHeight());
//
//        g2.setClip(shape);
//        // 使用 setRenderingHint 设置抗锯齿
//        g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fillRoundRect(padding, padding, width, height, radius, radius);
        g2.setComposite(AlphaComposite.SrcIn);
        g2.drawImage(srcImage, padding, padding, width, height, null);

        if (border > 0) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(border));
            g2.drawRoundRect(padding, padding, width - border / 2, height - border / 2, radius, radius);
        }
        g2.dispose();

        return bimage;
    }

    public static int getLength(String text) {
        int length = 0;
        for (int i = 0; i < text.length(); i++) {
            if ((text.charAt(i) + "").getBytes().length > 1) {
                length += 2;
            } else {
                length += 1;
            }
        }
        return length / 2;
    }

    public static void writeImage(Image image, FileType.Image fileType, OutputStream outputStream) {
        BufferedImage img = toBufferedImage(image, false, null);
        try {
            ImageIO.write(img, fileType.getFormat(), outputStream);
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "图片写出失败", e);
        }
    }

    public static void writeImage(Image image, FileType.Image fileType, File outputFile) {
        BufferedImage img = toBufferedImage(image, false, null);
        try {
            ImageIO.write(img, fileType.getFormat(), outputFile);
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "图片写出失败", e);
        }
    }

    /**
     * 将图片进行Base64编码
     *
     * @param image    图片内容
     * @param fileType 转换目标格式
     * @return 图片的Base64编码
     */
    public String encodeWithBase64(Image image, FileType.Image fileType) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            writeImage(image, fileType, os);
            return "data:image/" + fileType.getFormat() + ";base64," + Base64.encode(os.toByteArray());
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "生成图片base64编码失败", e);
        }
    }

    /**
     * 将Image对象转换为BufferedImage
     *
     * @param image        源图像
     * @param copy         是否复制(如果源图像就是BufferedImage, 当copy为false时会直接返回源图像;如果copy为true, 不论源图像是何对象, 都将为其生成新的副本)
     * @param transparency 透明模式
     * @return BufferedImage
     */
    public static BufferedImage toBufferedImage(Image image, boolean copy, Integer transparency) {
        if (image instanceof BufferedImage) {
            if ((null == transparency || transparency == ((BufferedImage) image).getTransparency()) && !copy) {
                return (BufferedImage) image;
            }

            if (null == transparency) {
                transparency = ((BufferedImage) image).getTransparency();
            }
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // Determine if the image has transparent pixels; for this method's
        // implementation, see e661 Determining If an Image Has Transparent Pixels
        //boolean hasAlpha = hasAlpha(image);

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int trans = null == transparency ? Transparency.OPAQUE : transparency;
	       /* if (hasAlpha) {
	         transparency = Transparency.BITMASK;
	         }*/

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                image.getWidth(null), image.getHeight(null), trans);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            //int type = BufferedImage.TYPE_3BYTE_BGR;//by wang
	        /*if (hasAlpha) {
	         type = BufferedImage.TYPE_INT_ARGB;
	         }*/
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }
}
