package org.spin.core.util;

import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.security.Base64;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.file.FileType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.ByteArrayInputStream;
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

    /**
     * 缩放模式
     */
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

    /**
     * 缩放图像
     *
     * @param image        原始图像
     * @param width        目标宽度
     * @param height       目标高度
     * @param mode         缩放模式
     * @param fillColor    填充颜色(仅填充模式时有效)
     * @param transparency 透明模式
     * @return 缩放后的图像
     */
    public static BufferedImage scale(Image image, int width, int height, ScaleMode mode, Color fillColor, Integer transparency) {
        int originWidth = image.getWidth(null);
        int originHeight = image.getHeight(null);

        // 尺寸不变直接返回
        if (originWidth == width && originHeight == height) {
            return toBufferedImage(image, false, transparency);
        }

        int trans = null == transparency ? Transparency.OPAQUE : transparency;
        BufferedImage result;
        Graphics2D g;
        switch (mode) {
            case STRENTCH:
                Image image_scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return toBufferedImage(image_scaled, false, transparency);
            case FILL:
                result = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                    .getDefaultConfiguration().createCompatibleImage(width, height, trans);

                Assert.notTrue((fillColor == null || fillColor.getAlpha() != 255) && (trans == Transparency.OPAQUE), "不透明模式时，无法实现透明的背景填充");
                g = result.createGraphics();
                if (null != fillColor) {
                    g.setColor(fillColor);
                    g.fillRect(0, 0, width, height);
                }

                int targetWidth;
                int targetHeight;
                if (width < originWidth || height < originHeight) {
                    // 缩小
                    if (height >= originHeight) {
                        targetWidth = width;
                        targetHeight = originHeight * width / originWidth;
                    } else if (width >= originWidth) {
                        targetWidth = originWidth * height / originHeight;
                        targetHeight = height;
                    } else {
                        float incW = (originWidth - ((float) width)) / originWidth;
                        float incH = (originHeight - ((float) height)) / originHeight;

                        if (incW > incH) {
                            targetWidth = width;
                            targetHeight = originHeight * width / originWidth;
                        } else {
                            targetWidth = originWidth * height / originHeight;
                            targetHeight = height;
                        }
                    }
                } else if (width != originWidth && height != originHeight) {
                    // 放大
                    float incW = (width - (float) originWidth) / originWidth;
                    float incH = (height - (float) originHeight) / originHeight;
                    if (incW < incH) {
                        targetWidth = width;
                        targetHeight = originHeight * width / originWidth;
                    } else {
                        targetWidth = originWidth * height / originHeight;
                        targetHeight = height;
                    }
                } else {
                    // 尺寸不变，只做填充
                    targetWidth = originWidth;
                    targetHeight = originHeight;
                }
                Image scaledInstance;
                if (width != originWidth) {
                    scaledInstance = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                } else {
                    scaledInstance = image;
                }
                if (width == targetWidth) {
                    g.drawImage(scaledInstance, 0, (height - targetHeight) / 2, targetWidth, targetHeight, null);
                } else {
                    g.drawImage(scaledInstance, (width - targetWidth) / 2, 0, targetWidth, targetHeight, null);
                }
                g.dispose();
                return result;
            case TILE:
                result = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                    .getDefaultConfiguration().createCompatibleImage(width, height, trans);
                g = result.createGraphics();
                int xTimes = (int) Math.ceil(((double) width) / originWidth);
                int yTimes = (int) Math.ceil(((double) height) / originHeight);
                for (int y = 0; y < yTimes; ++y) {
                    for (int x = 0; x < xTimes; ++x) {
                        g.drawImage(image, x * originWidth, y * originHeight, originWidth, originHeight, null);
                    }
                }
                g.dispose();
                return result;
            default:
                throw new SpinException("Unsupported MODE");
        }
    }

    /**
     * 图像裁剪
     *
     * @param image  原图像
     * @param x      起始x
     * @param y      起始y
     * @param width  裁剪宽度
     * @param height 裁剪高度
     * @return 裁剪的结果
     */
    public static BufferedImage cut(Image image, int x, int y, int width, int height) {
        int originWidth = image.getWidth(null);
        int originHeight = image.getHeight(null);

        BufferedImage result = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
            .getDefaultConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);

        // 如果起始坐标超出源图像范围，直接返回空白图像
        if (x >= originWidth || y >= originHeight) {
            return result;
        }

        int targetWidth = Math.min((originWidth - x), width);
        int targetHeight = Math.min((originHeight - y), height);

        ImageFilter cropFilter = new CropImageFilter(x, y, targetWidth, targetHeight);
        Image img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), cropFilter));
        Graphics g = result.getGraphics();
        g.drawImage(img, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return result;
    }


    /**
     * 图像分割
     * <p>将图像分割成rows * cols个部分，如果不能整除，最后一行与最后一列将会使用指定颜色的像素补齐</p>
     *
     * @param image           源图像
     * @param rows            行数
     * @param cols            列数
     * @param completionColor 像素不足时的填充颜色(默认使用透明像素)
     * @return 分割后的图像(二维数组)
     */
    public static BufferedImage[][] split(Image image, int rows, int cols, Color completionColor) {
        BufferedImage[][] res = new BufferedImage[rows][cols];
        int originWidth = image.getWidth(null);
        int originHeight = image.getHeight(null);

        int ppr = (int) Math.ceil(((double) originWidth) / rows);
        int ppc = (int) Math.ceil(((double) originHeight) / cols);

        for (int y = 0; y < cols; ++y) {
            for (int x = 0; x < rows; x++) {
                res[x][y] = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                    .getDefaultConfiguration().createCompatibleImage(ppr, ppc, Transparency.TRANSLUCENT);

                int startX = x * ppr;
                int startY = y * ppc;

                int targetWidth = Math.min((originWidth - startX), ppr);
                int targetHeight = Math.min((originHeight - startY), ppc);

                Image img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(),
                    new CropImageFilter(startX, startY, targetWidth, targetHeight)));
                Graphics g = res[x][y].getGraphics();
                g.drawImage(img, 0, 0, targetWidth, targetHeight, null);
                if (null != completionColor) {
                    int shortageX = ppr - targetWidth;
                    int shortageY = ppc - targetHeight;
                    if (shortageX > 0 || shortageY > 0) {
                        if (shortageY == 0) {
                            targetHeight = 0;
                        }

                        if (shortageX == 0) {
                            targetWidth = 0;
                        }
                        g.setColor(completionColor);
                        g.fillRect(targetWidth, targetHeight, ppr - targetWidth, ppr - targetHeight);
                    }
                }
                g.dispose();
            }
        }
        return res;
    }

    /**
     * 将图像转换为灰度图
     *
     * @param image 原图像
     * @return 灰度图
     */
    public static BufferedImage gray(Image image) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);
        return op.filter(toBufferedImage(image, true, null), null);
    }

    /**
     * 绘制文字, 并返回新的图像
     *
     * @param image     源图像
     * @param pressText 文字内容
     * @param font      字体
     * @param fontStyle 字体样式
     * @param color     绘制颜色
     * @param fontSize  字体大小
     * @param x         起始x
     * @param y         起始y
     * @param alpha     透明度
     * @return 绘制后的图像
     */
    public static BufferedImage pressText(Image image, String pressText, Font font, int fontStyle, Color color, float fontSize, int x, int y, float alpha) {
        BufferedImage bufferedImage = toBufferedImage(image, true, null);
        pressText(bufferedImage, pressText, font, fontStyle, color, fontSize, x, y, alpha);
        return bufferedImage;
    }

    /**
     * 在原图像上绘制文字
     *
     * @param bufferedImage 图像
     * @param pressText     文字内容
     * @param font          字体
     * @param fontStyle     字体样式
     * @param color         绘制颜色
     * @param fontSize      字体大小
     * @param x             起始x
     * @param y             起始y
     * @param alpha         透明度
     */
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

    /**
     * 图像叠加
     *
     * @param srcImage 原图像
     * @param overlay  叠加层
     * @param x        叠加层起点x
     * @param y        叠加层起点y
     * @param alpha    叠加层透明度
     * @return 处理后的图像
     */
    public static BufferedImage overlay(Image srcImage, Image overlay, int x, int y, float alpha) {
        BufferedImage image = toBufferedImage(srcImage, true, null);
        overlay(image, overlay, x, y, alpha);
        return image;
    }

    /**
     * 图像叠加(在原图像上直接叠加)
     *
     * @param image   图像
     * @param overlay 叠加层
     * @param x       叠加层起点x
     * @param y       叠加层起点y
     * @param alpha   叠加层透明度
     */
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
     * <p>处理后的图像实际宽/高为原始图像宽/高 + 边框宽度 * 2 + 边距 * 2</p>
     *
     * @param srcImage    源图像
     * @param radius      圆角半径
     * @param border      边框宽度
     * @param borderColor 边框颜色
     * @param padding     内边距
     * @return 处理后的图像
     */
    public static BufferedImage radius(Image srcImage, int radius, int border, Color borderColor, int padding) {
        int originWidth = srcImage.getWidth(null);
        int originHeight = srcImage.getHeight(null);

        int trans = Transparency.TRANSLUCENT;
        BufferedImage bimage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
            .createCompatibleImage(originWidth + padding * 2 + border * 2, originHeight + padding * 2 + border * 2, trans);

        Graphics2D g = bimage.createGraphics();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0F));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRoundRect(padding, padding, originWidth + border * 2, originHeight + border * 2, radius, radius);
        g.setComposite(AlphaComposite.SrcIn);
        g.drawImage(srcImage, padding + border, padding + border, originWidth, originHeight, null);

        if (border > 0) {
            g.setColor(borderColor);
            g.setStroke(new BasicStroke(border));
            g.drawRoundRect(padding + border / 2, padding + border / 2, originWidth + border, originHeight + border, radius, radius);
        }
        g.dispose();

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

    /**
     * 将图像写出到输出流
     *
     * @param image        图像
     * @param fileType     文件类型
     * @param outputStream 输出流
     */
    public static void writeImage(Image image, FileType.Image fileType, OutputStream outputStream) {
        BufferedImage img = toBufferedImage(image, false, null);
        try {
            ImageIO.write(img, fileType.getFormat(), outputStream);
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "图片写出失败", e);
        }
    }

    /**
     * 将文件写出到文件
     *
     * @param image      图像
     * @param fileType   文件类型
     * @param outputFile 文件
     */
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
    public static String encodeWithBase64(Image image, FileType.Image fileType) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            writeImage(image, fileType, os);
            return "data:image/" + fileType.getFormat() + ";base64," + Base64.encode(os.toByteArray());
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "生成图片Base64编码失败", e);
        }
    }

    /**
     * 将Base64编码解析为{@link BufferedImage}对象
     *
     * @param base64Str 图片的Base64编码
     * @return Buffered图像对象
     */
    public static BufferedImage decodeFromBase64(String base64Str) {
        if (StringUtils.isEmpty(base64Str)) {
            throw new SpinException("图片Base64内容不能为空");
        }
        int start = 0;
        if (base64Str.startsWith("data:image/")) {
            start = base64Str.indexOf(',');
            if (start < 12) {
                throw new SpinException("图片Base64格式不合法");
            }
        }
        try (ByteArrayInputStream is = new ByteArrayInputStream(Base64.decode(start > 0 ? base64Str.substring(start + 1) : base64Str))) {
            return ImageIO.read(is);
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "解析Base64编码失败", e);
        }
    }

    /**
     * 将Image对象转换为BufferedImage
     *
     * @param image        源图像
     * @param copy         是否复制(当copy为false, 如果源图像就是BufferedImage且透明模式与声明的一致时会直接返回源图像; 如果copy为true, 不论源图像是何对象, 都将为其生成新的副本)
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
            int type = transparency == null || transparency == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
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
