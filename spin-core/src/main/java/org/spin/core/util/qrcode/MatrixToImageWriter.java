package org.spin.core.util.qrcode;

import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * <p>Created by xuweinan on 2015/5/30.</p>
 *
 * @author xuweinan
 */
public final class MatrixToImageWriter {
    private static final MatrixToImageConfig DEFAULT_CONFIG = new MatrixToImageConfig();

    private MatrixToImageWriter() {
    }

    /**
     * Renders a {@link BitMatrix} as an image, where "false" bits are rendered
     * as white, and "true" bits are rendered as black. Uses default configuration.
     *
     * @param matrix {@link BitMatrix} to write
     * @return {@link BufferedImage} representation of the input
     */
    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        return toBufferedImage(matrix, null, DEFAULT_CONFIG);
    }

    /**
     * As {@link #toBufferedImage(BitMatrix)}, but allows customization of the output.
     *
     * @param matrix {@link BitMatrix} to write
     * @param config output configuration
     * @return {@link BufferedImage} representation of the input
     */
    public static BufferedImage toBufferedImage(BitMatrix matrix, MatrixToImageConfig config) {
        return toBufferedImage(matrix, null, config);
    }

    /**
     * As {@link #toBufferedImage(BitMatrix)}, but allows customization of the output.
     *
     * @param matrix {@link BitMatrix} to write
     * @return {@link BufferedImage} representation of the input
     */
    public static BufferedImage toBufferedImage(BitMatrix matrix, Image logo) {
        return toBufferedImage(matrix, logo, DEFAULT_CONFIG);
    }

    /**
     * As {@link #toBufferedImage(BitMatrix)}, but allows customization of the output.
     *
     * @param matrix {@link BitMatrix} to write
     * @param config output configuration
     * @return {@link BufferedImage} representation of the input
     */
    public static BufferedImage toBufferedImage(BitMatrix matrix, Image logo, MatrixToImageConfig config) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, config.getBufferedImageColorModel());

        BufferedImage bufferedLogo = null;
        int startX = 0;
        int endX = 0;
        int startY = 0;
        int endY = 0;

        if (null != logo) {
            if (logo.getWidth(null) > width || logo.getHeight(null) > height) {
                throw new SpinException("the Logo size is greater than qrcode image's size");
            }
            bufferedLogo = ImageUtils.toBufferedImage(logo, false, null);
            startX = width - bufferedLogo.getWidth(null) / 2;
            endX = startX + bufferedLogo.getWidth(null);
            startY = height - bufferedLogo.getHeight(null) / 2;
            endY = startY + bufferedLogo.getHeight(null);
        }
        int onColor = config.getPixelOnColor();
        int offColor = config.getPixelOffColor();
        int[] rowPixels = new int[width];
        BitArray row = new BitArray(width);
        boolean inY;
        for (int y = 0; y < height; y++) {
            row = matrix.getRow(y, row);
            inY = null != bufferedLogo && y >= startY && y < endY;
            for (int x = 0; x < width; x++) {
                if (inY && x >= startX && x < endX) {
                    rowPixels[x] = bufferedLogo.getRGB(x - startX, y - startY);
                } else {
                    rowPixels[x] = row.get(x) ? onColor : offColor;
                }
            }
            image.setRGB(0, y, width, 1, rowPixels, 0, width);
        }
        return image;
    }

    /**
     * @param matrix {@link BitMatrix} to write
     * @param format image format
     * @param file   file {@link File} to write image to
     * @throws IOException if writes to the file fail
     */
    public static void writeToFile(BitMatrix matrix, String format, File file) throws IOException {
        writeToPath(matrix, format, file.toPath());
    }

    /**
     * Writes a {@link BitMatrix} to a file with default configuration.
     *
     * @param matrix {@link BitMatrix} to write
     * @param format image format
     * @param file   file {@link Path} to write image to
     * @throws IOException if writes to the stream fail
     * @see #toBufferedImage(BitMatrix)
     */
    public static void writeToPath(BitMatrix matrix, String format, Path file) throws IOException {
        writeToPath(matrix, format, file, DEFAULT_CONFIG);
    }

    /**
     * @param matrix {@link BitMatrix} to write
     * @param format image format
     * @param file   file {@link File} to write image to
     * @param config output configuration
     * @throws IOException if writes to the file fail
     */
    public static void writeToFile(BitMatrix matrix, String format, File file, MatrixToImageConfig config)
        throws IOException {
        writeToPath(matrix, format, file.toPath(), config);
    }

    /**
     * As {@link #writeToPath(BitMatrix, String, Path)}, but allows customization of the output.
     *
     * @param matrix {@link BitMatrix} to write
     * @param format image format
     * @param file   file {@link Path} to write image to
     * @param config output configuration
     * @throws IOException if writes to the file fail
     */
    public static void writeToPath(BitMatrix matrix, String format, Path file, MatrixToImageConfig config)
        throws IOException {
        BufferedImage image = toBufferedImage(matrix, config);
        if (!ImageIO.write(image, format, file.toFile())) {
            throw new IOException("Could not write an image of format " + format + " to " + file);
        }
    }

    /**
     * Writes a {@link BitMatrix} to a stream with default configuration.
     *
     * @param matrix {@link BitMatrix} to write
     * @param format image format
     * @param stream {@link OutputStream} to write image to
     * @throws IOException if writes to the stream fail
     * @see #toBufferedImage(BitMatrix)
     */
    public static void writeToStream(BitMatrix matrix, String format, OutputStream stream) throws IOException {
        writeToStream(matrix, format, stream, DEFAULT_CONFIG);
    }

    /**
     * As {@link #writeToStream(BitMatrix, String, OutputStream)}, but allows customization of the output.
     *
     * @param matrix {@link BitMatrix} to write
     * @param format image format
     * @param stream {@link OutputStream} to write image to
     * @param config output configuration
     * @throws IOException if writes to the stream fail
     */
    public static void writeToStream(BitMatrix matrix, String format, OutputStream stream, MatrixToImageConfig config)
        throws IOException {
        BufferedImage image = toBufferedImage(matrix, config);
        if (!ImageIO.write(image, format, stream)) {
            throw new IOException("Could not write an image of format " + format);
        }
    }

}
