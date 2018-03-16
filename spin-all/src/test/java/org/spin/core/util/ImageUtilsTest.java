package org.spin.core.util;

import java.awt.*;

/**
 * <p>Created by xuweinan on 2017/10/10.</p>
 *
 * @author xuweinan
 */
public class ImageUtilsTest {

    public void test() {
        // 1-缩放图像：
        // 方法一：按比例缩放
        ImageUtils.scale("e:/abc.jpg", "e:/abc_scale.jpg", 2);//测试OK
        // 方法二：按高度和宽度缩放
        ImageUtils.scale("e:/abc.jpg", "e:/abc_scale2.jpg", 500, 300, true);//测试OK


        // 2-切割图像：
        // 方法一：按指定起点坐标和宽高切割
        ImageUtils.cut("e:/abc.jpg", "e:/abc_cut.jpg", 0, 0, 400, 400);//测试OK
        // 方法二：指定切片的行数和列数
        ImageUtils.cut2("e:/abc.jpg", "e:/", 2, 2);//测试OK
        // 方法三：指定切片的宽度和高度
        ImageUtils.cut3("e:/abc.jpg", "e:/", 300, 300);//测试OK


        // 3-图像类型转换：
        ImageUtils.convert("e:/abc.jpg", "GIF", "e:/abc_convert.gif");//测试OK


        // 4-彩色转黑白：
        ImageUtils.gray("e:/abc.jpg", "e:/abc_gray.jpg");//测试OK


        // 5-给图片添加文字水印：
        // 方法一：
        ImageUtils.pressText("我是水印文字", "e:/abc.jpg", "e:/abc_pressText.jpg", "宋体", Font.BOLD, Color.white, 80, 0, 0, 0.5f);//测试OK
        // 方法二：
        ImageUtils.pressText2("我也是水印文字", "e:/abc.jpg", "e:/abc_pressText2.jpg", "黑体", 36, Color.white, 80, 0, 0, 0.5f);//测试OK

        // 6-给图片添加图片水印：
        ImageUtils.pressImage("e:/abc2.jpg", "e:/abc.jpg", "e:/abc_pressImage.jpg", 0, 0, 0.5f);//测试OK
    }
}
