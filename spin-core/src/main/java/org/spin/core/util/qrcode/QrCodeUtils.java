package org.spin.core.util.qrcode;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.function.FinalConsumer;
import org.spin.core.function.serializable.Function;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.ImageUtils;
import org.spin.core.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 二维码工具类
 * <p>Created by xuweinan on 2015/5/30.</p>
 *
 * @author xuweinan
 */
public final class QrCodeUtils extends Util {

    private static final Logger logger = LoggerFactory.getLogger(QrCodeUtils.class);

    private static final float LOGO_RATIO = 100F / 430F;
    private static final float RADIUS_RATIO = 20F / 430F;
    private static final float BORDER_RATIO = 5F / 430F;

    private QrCodeUtils() {
    }

    public static BufferedImage optimizeLogo(Image logo, int qrcodeSize, Color borderColor) {
        return optimizeLogo(logo, qrcodeSize, Math.round(qrcodeSize * RADIUS_RATIO), Math.round(qrcodeSize * BORDER_RATIO), borderColor, 0);
    }

    public static BufferedImage optimizeLogo(Image logo, int qrcodeSize, int radius, int border, Color borderColor, int padding) {
        int logoSize = Math.round(qrcodeSize * LOGO_RATIO);
        int contentSize = logoSize - padding * 2 - border * 2;
        BufferedImage logoContent = ImageUtils.scale(logo, contentSize, contentSize, ImageUtils.ScaleMode.STRETCH, Color.WHITE, Transparency.TRANSLUCENT);
        return ImageUtils.radius(logoContent, radius, border, borderColor, padding);
    }

    /**
     * 对指定内容生成二维码，结果交由用户逻辑处理
     *
     * @param content   内容
     * @param size      二维码尺寸(像素)
     * @param processor 处理逻辑
     * @return 像素矩阵
     */
    public static BitMatrix encode(CharSequence content, int size, FinalConsumer<BitMatrix> processor) {
        return encode(content, size, bitMatrix -> {
            if (null != processor) {
                processor.accept(bitMatrix);
            }
            return bitMatrix;
        });
    }

    public static BufferedImage encode(CharSequence content, int size, Image logo) {
        return encode(content, size, bitMatrix -> {
            return MatrixToImageWriter.toBufferedImage(bitMatrix, logo);
        });
    }

    public static BufferedImage encode(CharSequence content, int size) {
        return encode(content, size, (Function<BitMatrix, BufferedImage>) MatrixToImageWriter::toBufferedImage);
    }

    /**
     * 对指定内容生成二维码，结果交由用户逻辑处理
     *
     * @param content 内容
     * @param size    二维码尺寸(像素)
     * @param mapper  转换逻辑
     * @param <T>     转换类型
     * @return 转换结果
     */
    public static <T> T encode(CharSequence content, int size, Function<BitMatrix, T> mapper) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 0);
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(content.toString(), BarcodeFormat.QR_CODE, size, size, hints);
            return mapper.apply(bitMatrix);
        } catch (WriterException e) {
            throw new SpinException("QRCode generate failed", e);
        }
    }

    /**
     * 从指定的来源读取二维码并返回解析内容
     *
     * @param imageSupplier 图片来源
     * @return 解析结果
     */
    public static Result decode(Supplier<BufferedImage> imageSupplier) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(imageSupplier.get());
            Binarizer binarizer = new HybridBinarizer(source);
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(DecodeHintType.TRY_HARDER, true);

            MultiFormatReader formatReader = new MultiFormatReader();
            return formatReader.decode(binaryBitmap, hints);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
