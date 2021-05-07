package org.spin.enhance.geo;

import org.spin.core.util.Util;

public abstract class Converter extends Util {
    private Converter() {
    }

    // region 内部变量参数
    private static final double PI = 3.1415926535897932384626D;

    /**
     * SK-42参考椭球 长半轴(米)
     */
    private static final double RE = 6_378_245D;

    /**
     * SK-42参考椭球 短半轴(米)
     */
    private static final double RP = 6_356_863.018773047D;

    /**
     * 第一偏心率的平方  e=(Re^2-Rp^2)^0.5/Re
     */
    private static final double E_SQUARE = (RE * RE - RP * RP) / (RE * RE);

    /**
     * 角度转弧度系数
     */
    private static final double DEG_TO_RAD = PI / 180D;

    private static final double[] MCBAND = {12890594.86D, 8362377.87D, 5591021D, 3481989.83D, 1678043.12D, 0D};
    private static final double[] BDBAND = {75D, 60D, 45D, 30D, 15D, 0D};
    private static final double[][] MC2BD = {
        {
            1.410526172116255e-8D, 0.00000898305509648872D, -1.9939833816331D, 200.9824383106796D, -187.2403703815547D
            , 91.6087516669843, -23.38765649603339, 2.57121317296198, -0.03801003308653, 17337981.2
        },
        {
            -7.435856389565537e-9, 0.000008983055097726239, -0.78625201886289, 96.32687599759846, -1.85204757529826,
            -59.36935905485877, 47.40033549296737, -16.50741931063887, 2.28786674699375, 10260144.86
        },
        {
            -3.030883460898826e-8, 0.00000898305509983578, 0.30071316287616, 59.74293618442277, 7.357984074871,
            -25.38371002664745, 13.45380521110908, -3.29883767235584, 0.32710905363475, 6856817.37
        },
        {
            -1.981981304930552e-8, 0.000008983055099779535, 0.03278182852591, 40.31678527705744, 0.65659298677277,
            -4.44255534477492, 0.85341911805263, 0.12923347998204, -0.04625736007561, 4482777.06
        },
        {
            3.09191371068437e-9, 0.000008983055096812155, 0.00006995724062, 23.10934304144901, -0.00023663490511,
            -0.6321817810242, -0.00663494467273, 0.03430082397953, -0.00466043876332, 2555164.4
        },
        {
            2.890871144776878e-9, 0.000008983055095805407, -3.068298e-8, 7.47137025468032, -0.00000353937994,
            -0.02145144861037, -0.00001234426596, 0.00010322952773, -0.00000323890364, 826088.5
        }
    };
    private static final double[][] BD2MC = {
        {
            -0.0015702102444, 111320.7020616939, 1704480524535203d, -10338987376042340d, 26112667856603880d,
            -35149669176653700d, 26595700718403920d, -10725012454188240d, 1800819912950474d, 82.5
        },
        {
            0.0008277824516172526, 111320.7020463578, 647795574.6671607, -4082003173.641316, 10774905663.51142,
            -15171875531.51559, 12053065338.62167, -5124939663.577472, 913311935.9512032, 67.5
        },
        {
            0.00337398766765, 111320.7020202162, 4481351.045890365, -23393751.19931662, 79682215.47186455,
            -115964993.2797253, 97236711.15602145, -43661946.33752821, 8477230.501135234, 52.5
        },
        {
            0.00220636496208, 111320.7020209128, 51751.86112841131, 3796837.749470245, 992013.7397791013,
            -1221952.21711287, 1340652.697009075, -620943.6990984312, 144416.9293806241, 37.5
        },
        {
            -0.0003441963504368392, 111320.7020576856, 278.2353980772752, 2485758.690035394, 6070.750963243378,
            54821.18345352118, 9540.606633304236, -2710.55326746645, 1405.483844121726, 22.5
        },
        {
            -0.0003218135878613132, 111320.7020701615, 0.00369383431289, 823725.6402795718, 0.46104986909093,
            2351.343141331292, 1.58060784298199, 8.77738589078284, 0.37238884252424, 7.45
        }
    };

    // endregion


    public static void main(String[] args) {

        System.out.println((RE * RE - RP * RP) / (RE * RE));
        System.out.println(E_SQUARE);
        System.out.println(59560D / 8898289D);
//        // 百度坐标
//        double[] doubles = {118.346802, 31.298912};
//        Coordinate gc = bd09ToGcj02(doubles[1], doubles[0]); //OK
//        System.out.println(gc);
//        System.out.println(gcj02ToGps(gc.latitude, gc.longitude));
//        // gps坐标
        Coordinate origin = new Coordinate(CoordinateSystem.GCJ02, 118.421084, 31.351456);
        System.out.println(origin);
        origin = gcj02ToGps(origin.getLongitude(), origin.getLatitude());
        System.out.println(origin);
        origin = gpsToGcj02(origin.getLongitude(), origin.getLatitude());
        System.out.println(origin);

        // 有问题
        origin = gcj02ToBd09(origin.getLongitude(), origin.getLatitude());
        System.out.println(origin);
//
//        // 墨卡托坐标
//        Coordinate bd = mcToBd09(13183349.840729073, 3656615.7680585813);
//        System.out.println(bd);
//        System.out.println(bd09ToGcj02(bd.getLatitude(), bd.getLongitude()));
    }


    /**
     * GPS to 国测坐标系 (GCJ-02)
     *
     * @param lng GPS经度
     * @param lat GPS纬度
     * @return GC坐标
     */
    public static Coordinate gpsToGcj02(double lng, double lat) {
        Coordinate offset = gcj02Offset(lng, lat);
        // 返回加偏结果
        return new Coordinate(CoordinateSystem.GCJ02, lng + offset.getLongitude(), lat + offset.getLatitude());
    }

    /**
     * 计算国测坐标在GPS下的偏移量
     *
     * @param lng GPS经度
     * @param lat GPS纬度
     * @return 偏移
     */
    public static Coordinate gcj02Offset(double lng, double lat) {
        if (outOfChina(lng, lat)) {
            return new Coordinate(CoordinateSystem.OFFSET, 0, 0);
        }
        double radLat = DEG_TO_RAD * lat;
        double magic = Math.sin(radLat);
        magic = 1D - E_SQUARE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);

        double dLng = calcLngOffset(lng - 105D, lat - 35D);
        // 转换为SK-42参考椭球下的偏离经度
        dLng = (dLng * 180D) / (RE / sqrtMagic * Math.cos(radLat) * PI);

        double dLat = calcLatOffset(lng - 105D, lat - 35D);
        // 转换为SK-42参考椭球下的偏离纬度
        dLat = (dLat * 180D) / ((RE * (1D - E_SQUARE)) / (magic * sqrtMagic) * PI);

        return new Coordinate(CoordinateSystem.OFFSET, dLng, dLat);
    }

    /**
     * 国测坐标系 (GCJ-02) to GPS
     *
     * @param lng 经度
     * @param lat 纬度
     * @return GPS坐标
     */
    public static Coordinate gcj02ToGps(double lng, double lat) {
        // 利用原加偏超越方程局部近似线性的特点来近似逼近
        Coordinate offset = gcj02Offset(lng, lat);
        Coordinate coord = new Coordinate(CoordinateSystem.GPS, lng - offset.getLongitude(), lat - offset.getLatitude());

        offset = gcj02Offset(coord.getLongitude(), coord.getLatitude());
        coord = new Coordinate(CoordinateSystem.GPS, lng - offset.getLongitude(), lat - offset.getLatitude());
        return coord;
    }

    /**
     * GCJ-02 坐标转换成 BD-09 坐标
     *
     * @param lat 纬度
     * @param lng 经度
     * @return 百度坐标
     */
    public static Coordinate gcj02ToBd09(double lat, double lng) {
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002D * Math.sin(lat * PI * 3000D / 180D);
        double theta = Math.atan2(lat, lng) + 0.000003D * Math.cos(lng * PI * 3000D / 180D);
        double bd_lng = z * Math.cos(theta) + 0.0065D;
        double bd_lat = z * Math.sin(theta) + 0.006D;
        return new Coordinate(CoordinateSystem.BAIDU, bd_lat, bd_lng);
    }

    /**
     * 将 BD-09 坐标转换成GCJ-02 坐标
     *
     * @param lat 纬度
     * @param lng 经度
     * @return GC坐标
     */
    public static Coordinate bd09ToGcj02(double lat, double lng) {
        double x = lng - 0.0065D, y = lat - 0.006D;
        double z = Math.sqrt(x * x + y * y) - 0.00002D * Math.sin(y * PI * 3000D / 180D);
        double theta = Math.atan2(y, x) - 0.000003D * Math.cos(x * PI * 3000D / 180D);
        double gg_lng = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new Coordinate(CoordinateSystem.GCJ02, gg_lat, gg_lng);
    }

    /**
     * 将 BD-09 坐标转换成GPS 坐标
     *
     * @param lat 纬度
     * @param lng 经度
     * @return GPS坐标
     */
    public static Coordinate bd09ToGps(double lat, double lng) {
        Coordinate gcj02 = bd09ToGcj02(lat, lng);
        return gcj02ToGps(gcj02.getLatitude(), gcj02.getLongitude());
    }

    /**
     * 将GPS坐标转换为BD-09坐标
     *
     * @param lat 纬度
     * @param lng 经度
     * @return 百度坐标
     */
    public static Coordinate gpsToBd09ll(double lat, double lng) {
        Coordinate gcj02 = gpsToGcj02(lat, lng);
        return gcj02ToBd09(gcj02.getLatitude(), gcj02.getLongitude());
    }

    /**
     * 计算纬度的非线性偏移
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 偏移距离(米)
     */
    private static double calcLatOffset(double lng, double lat) {
        return -100D + 2D * lng + 3D * lat + 0.2D * lat * lat + 0.1D * lng * lat + 0.2D * Math.sqrt(Math.abs(lng))
            + (20D * Math.sin(6D * PI * lng) + 20D * Math.sin(2D * PI * lng)) * 2D / 3D
            + (20D * Math.sin(PI * lat) + 40D * Math.sin(PI / 3D * lat)) * 2D / 3D
            + (160D * Math.sin(PI / 12D * lat) + 320D * Math.sin(PI / 30D * lat)) * 2D / 3D;
    }

    /**
     * 计算经度的非线性偏移
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 偏移距离(米)
     */
    private static double calcLngOffset(double lng, double lat) {
        return 300D + lng + 2.0D * lat + 0.1D * lng * lng + 0.1D * lng * lat + 0.1D * Math.sqrt(Math.abs(lng))
            + (20D * Math.sin(6D * PI * lng) + 20D * Math.sin(2D * PI * lng)) * 2D / 3D
            + (20D * Math.sin(PI * lng) + 40D * Math.sin(PI / 3D * lng)) * 2D / 3D
            + (150D * Math.sin(PI / 12D * lng) + 300D * Math.sin(PI / 30D * lng)) * 2D / 3D;
    }

    private static boolean outOfChina(double lng, double lat) {
        if (lng < 72.004 || lng > 137.8347)
            return true;
        return lat < 0.8293 || lat > 55.8271;
    }


    /**
     * 墨卡托坐标转经纬度坐标
     */
    private static Coordinate mcToBd09(double x, double y) {
        double[] cF = null;
        x = Math.abs(x);
        y = Math.abs(y);
        for (int cE = 0; cE < MCBAND.length; cE++) {
            if (y >= MCBAND[cE]) {
                cF = MC2BD[cE];
                break;
            }
        }
        return converter(x, y, cF, true);
    }

    /**
     * 经纬度坐标转墨卡托坐标
     */
    private static Coordinate bd09Tomc(double lat, double lng) {
        double[] cE = null;
        lng = getLoop(lng, -180, 180);
        lat = getRange(lat, -74, 74);
        for (int i = 0; i < BDBAND.length; i++) {
            if (lat >= BDBAND[i]) {
                cE = BD2MC[i];
                break;
            }
        }
        if (cE != null) {
            for (int i = BDBAND.length - 1; i >= 0; i--) {
                if (lat <= -BDBAND[i]) {
                    cE = BD2MC[i];
                    break;
                }
            }
        }
        return converter(lng, lat, cE, false);
    }

    private static Coordinate converter(double x, double y, double[] cE, boolean revert) {
        double xTemp = cE[0] + cE[1] * Math.abs(x);
        double cC = Math.abs(y) / cE[9];
        double yTemp = cE[2] + cE[3] * cC + cE[4] * cC * cC + cE[5] * cC * cC * cC + cE[6] * cC * cC * cC * cC + cE[7] * cC * cC * cC * cC * cC + cE[8] * cC * cC * cC * cC * cC * cC;
        xTemp *= (x < 0 ? -1 : 1);
        yTemp *= (y < 0 ? -1 : 1);
        return revert ? new Coordinate(CoordinateSystem.BAIDU, yTemp, xTemp) : new Coordinate(CoordinateSystem.MERCATOR, xTemp, yTemp);
    }

    private static double getLoop(double lng, int min, int max) {
        while (lng > max) {
            lng -= max - min;
        }
        while (lng < min) {
            lng += max - min;
        }
        return lng;
    }

    private static double getRange(double lat, Integer min, Integer max) {
        if (min != null) {
            lat = Math.max(lat, min);
        }
        if (max != null) {
            lat = Math.min(lat, max);
        }
        return lat;
    }
}
