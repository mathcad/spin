package org.spin.enhance.pinyin;

import org.junit.jupiter.api.Test;
import org.spin.enhance.pinyin.format.PinyinCaseType;
import org.spin.enhance.pinyin.format.PinyinOutputFormat;
import org.spin.enhance.pinyin.format.PinyinToneType;
import org.spin.enhance.pinyin.format.PinyinVCharType;
import org.spin.enhance.pinyin.format.exception.BadPinyinOutputFormatCombination;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PinyinHelperTest {

    @Test
    void testPy() throws BadPinyinOutputFormatCombination {

        String src = "aa";
        String s = PinyinHelper.toPinyinHeadString(src, true);

        System.out.println(s);
    }

    public void testToTongyongPinyinStringArray() {
        // any input of non-Chinese characters will return null
        {
            assertNull(PinyinHelper.toTongyongPinyinStringList('A'));
            assertNull(PinyinHelper.toTongyongPinyinStringList('z'));
            assertNull(PinyinHelper.toTongyongPinyinStringList(','));
            assertNull(PinyinHelper.toTongyongPinyinStringList('。'));
        }

        // Chinese characters
        // single pronounciation
        {
            String[] expectedPinyinArray = new String[]{"li3"};
            List<String> pinyinArray = PinyinHelper.toTongyongPinyinStringList('李');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }
        {
            String[] expectedPinyinArray = new String[]{"ciou2"};
            List<String> pinyinArray = PinyinHelper.toTongyongPinyinStringList('球');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }
        {
            String[] expectedPinyinArray = new String[]{"jhuang1"};
            List<String> pinyinArray = PinyinHelper.toTongyongPinyinStringList('桩');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }

        // multiple pronounciations
        {
            String[] expectedPinyinArray = new String[]{"chuan2", "jhuan4"};
            List<String> pinyinArray = PinyinHelper.toTongyongPinyinStringList('传');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }

        {
            String[] expectedPinyinArray = new String[]{"lyu4", "lu4"};
            List<String> pinyinArray = PinyinHelper.toTongyongPinyinStringList('绿');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }
    }

    public void testToWadeGilesPinyinStringArray() {
        // any input of non-Chinese characters will return null
        {
            assertNull(PinyinHelper.toWadeGilesPinyinStringList('A'));
            assertNull(PinyinHelper.toWadeGilesPinyinStringList('z'));
            assertNull(PinyinHelper.toWadeGilesPinyinStringList(','));
            assertNull(PinyinHelper.toWadeGilesPinyinStringList('。'));
        }

        // Chinese characters
        // single pronounciation
        {
            String[] expectedPinyinArray = new String[]{"li3"};
            List<String> pinyinArray = PinyinHelper.toWadeGilesPinyinStringList('李');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }
        {
            String[] expectedPinyinArray = new String[]{"ch`iu2"};
            List<String> pinyinArray = PinyinHelper.toWadeGilesPinyinStringList('球');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }
        {
            String[] expectedPinyinArray = new String[]{"chuang1"};
            List<String> pinyinArray = PinyinHelper.toWadeGilesPinyinStringList('桩');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }

        // multiple pronounciations
        {
            String[] expectedPinyinArray = new String[]{"ch`uan2", "chuan4"};
            List<String> pinyinArray = PinyinHelper.toWadeGilesPinyinStringList('传');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }

        {
            String[] expectedPinyinArray = new String[]{"lu:4", "lu4"};
            List<String> pinyinArray = PinyinHelper.toWadeGilesPinyinStringList('绿');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }
    }

    public void testToMPS2PinyinStringArray() {
        // any input of non-Chinese characters will return null
        {
            assertNull(PinyinHelper.toMPS2PinyinStringList('A'));
            assertNull(PinyinHelper.toMPS2PinyinStringList('z'));
            assertNull(PinyinHelper.toMPS2PinyinStringList(','));
            assertNull(PinyinHelper.toMPS2PinyinStringList('。'));
        }

        // Chinese characters
        // single pronounciation
        {
            String[] expectedPinyinArray = new String[]{"li3"};
            List<String> pinyinArray = PinyinHelper.toMPS2PinyinStringList('李');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }
        {
            String[] expectedPinyinArray = new String[]{"chiou2"};
            List<String> pinyinArray = PinyinHelper.toMPS2PinyinStringList('球');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }
        {
            String[] expectedPinyinArray = new String[]{"juang1"};
            List<String> pinyinArray = PinyinHelper.toMPS2PinyinStringList('桩');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }

        // multiple pronounciations
        {
            String[] expectedPinyinArray = new String[]{"chuan2", "juan4"};
            List<String> pinyinArray = PinyinHelper.toMPS2PinyinStringList('传');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }

        {
            String[] expectedPinyinArray = new String[]{"liu4", "lu4"};
            List<String> pinyinArray = PinyinHelper.toMPS2PinyinStringList('绿');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }
    }

    public void testToYalePinyinStringArray() {
        // any input of non-Chinese characters will return null
        {
            assertNull(PinyinHelper.toYalePinyinStringList('A'));
            assertNull(PinyinHelper.toYalePinyinStringList('z'));
            assertNull(PinyinHelper.toYalePinyinStringList(','));
            assertNull(PinyinHelper.toYalePinyinStringList('。'));
        }

        // Chinese characters
        // single pronounciation
        {
            String[] expectedPinyinArray = new String[]{"li3"};
            List<String> pinyinArray = PinyinHelper.toYalePinyinStringList('李');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }
        {
            String[] expectedPinyinArray = new String[]{"chyou2"};
            List<String> pinyinArray = PinyinHelper.toYalePinyinStringList('球');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }
        {
            String[] expectedPinyinArray = new String[]{"jwang1"};
            List<String> pinyinArray = PinyinHelper.toYalePinyinStringList('桩');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }

        // multiple pronounciations
        {
            String[] expectedPinyinArray = new String[]{"chwan2", "jwan4"};
            List<String> pinyinArray = PinyinHelper.toYalePinyinStringList('传');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }

        {
            String[] expectedPinyinArray = new String[]{"lyu4", "lu4"};
            List<String> pinyinArray = PinyinHelper.toYalePinyinStringList('绿');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }
    }

    public void testToGwoyeuRomatzyhStringArray() {
        // any input of non-Chinese characters will return null
        {
            assertNull(PinyinHelper.toGwoyeuRomatzyhStringList('A'));
            assertNull(PinyinHelper.toGwoyeuRomatzyhStringList('z'));
            assertNull(PinyinHelper.toGwoyeuRomatzyhStringList(','));
            assertNull(PinyinHelper.toGwoyeuRomatzyhStringList('。'));
        }

        // Chinese characters
        // single pronounciation
        {
            String[] expectedPinyinArray = new String[]{"lii"};
            List<String> pinyinArray = PinyinHelper.toGwoyeuRomatzyhStringList('李');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }
        {
            String[] expectedPinyinArray = new String[]{"chyou"};
            List<String> pinyinArray = PinyinHelper.toGwoyeuRomatzyhStringList('球');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }
        {
            String[] expectedPinyinArray = new String[]{"juang"};
            List<String> pinyinArray = PinyinHelper.toGwoyeuRomatzyhStringList('桩');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }

        {
            String[] expectedPinyinArray = new String[]{"fuh"};
            List<String> pinyinArray = PinyinHelper.toGwoyeuRomatzyhStringList('付');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }

        // multiple pronounciations
        {
            String[] expectedPinyinArray = new String[]{"chwan", "juann"};
            List<String> pinyinArray = PinyinHelper.toGwoyeuRomatzyhStringList('传');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }

        {
            String[] expectedPinyinArray = new String[]{".me", ".mha", "iau"};
            List<String> pinyinArray = PinyinHelper.toGwoyeuRomatzyhStringList('么');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }

        {
            String[] expectedPinyinArray = new String[]{"liuh", "luh"};
            List<String> pinyinArray = PinyinHelper.toGwoyeuRomatzyhStringList('绿');

            assertEquals(expectedPinyinArray.length, pinyinArray.size());

            for (int i = 0; i < expectedPinyinArray.length; i++) {
                assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
            }
        }
    }

    public void testToPinyinStringArray() {

        // any input of non-Chinese characters will return null
        {
            PinyinOutputFormat defaultFormat = new PinyinOutputFormat();
            try {
                assertNull(PinyinHelper.toPinyinStringList('A', defaultFormat));
                assertNull(PinyinHelper.toPinyinStringList('z', defaultFormat));
                assertNull(PinyinHelper.toPinyinStringList(',', defaultFormat));
                assertNull(PinyinHelper.toPinyinStringList('。', defaultFormat));
            } catch (BadPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        }

        // Chinese characters
        // single pronounciation
        {
            try {
                PinyinOutputFormat defaultFormat = new PinyinOutputFormat();

                String[] expectedPinyinArray = new String[]{"li3"};
                List<String> pinyinArray = PinyinHelper.toPinyinStringList('李', defaultFormat);

                assertEquals(expectedPinyinArray.length, pinyinArray.size());

                for (int i = 0; i < expectedPinyinArray.length; i++) {
                    assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
                }
            } catch (BadPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        }
        {
            try {
                PinyinOutputFormat upperCaseFormat = new PinyinOutputFormat();
                upperCaseFormat.setCaseType(PinyinCaseType.UPPERCASE);

                String[] expectedPinyinArray = new String[]{"LI3"};
                List<String> pinyinArray = PinyinHelper.toPinyinStringList('李', upperCaseFormat);

                assertEquals(expectedPinyinArray.length, pinyinArray.size());

                for (int i = 0; i < expectedPinyinArray.length; i++) {
                    assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
                }
            } catch (BadPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        }
        {
            try {
                PinyinOutputFormat defaultFormat = new PinyinOutputFormat();

                String[] expectedPinyinArray = new String[]{"lu:3"};
                List<String> pinyinArray = PinyinHelper.toPinyinStringList('吕', defaultFormat);

                assertEquals(expectedPinyinArray.length, pinyinArray.size());

                for (int i = 0; i < expectedPinyinArray.length; i++) {
                    assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
                }
            } catch (BadPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        }
        {
            try {
                PinyinOutputFormat vCharFormat = new PinyinOutputFormat();
                vCharFormat.setVCharType(PinyinVCharType.WITH_V);

                String[] expectedPinyinArray = new String[]{"lv3"};
                List<String> pinyinArray = PinyinHelper.toPinyinStringList('吕', vCharFormat);

                assertEquals(expectedPinyinArray.length, pinyinArray.size());

                for (int i = 0; i < expectedPinyinArray.length; i++) {
                    assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
                }
            } catch (BadPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        }

        // multiple pronounciations
        {
            try {
                PinyinOutputFormat defaultFormat = new PinyinOutputFormat();

                String[] expectedPinyinArray = new String[]{"jian1", "jian4"};
                List<String> pinyinArray = PinyinHelper.toPinyinStringList('间', defaultFormat);

                assertEquals(expectedPinyinArray.length, pinyinArray.size());

                for (int i = 0; i < expectedPinyinArray.length; i++) {
                    assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
                }
            } catch (BadPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        }

        {
            try {
                PinyinOutputFormat defaultFormat = new PinyinOutputFormat();

                String[] expectedPinyinArray = new String[]{"hao3", "hao4"};
                List<String> pinyinArray = PinyinHelper.toPinyinStringList('好', defaultFormat);

                assertEquals(expectedPinyinArray.length, pinyinArray.size());

                for (int i = 0; i < expectedPinyinArray.length; i++) {
                    assertEquals(expectedPinyinArray[i], pinyinArray.get(i));
                }
            } catch (BadPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * test for combination of output formats
     */
    public void testOutputCombination() {
        try {
            PinyinOutputFormat outputFormat = new PinyinOutputFormat();

            // fix case type to lowercase firstly, change VChar and Tone
            // combination
            outputFormat.setCaseType(PinyinCaseType.LOWERCASE);

            // WITH_U_AND_COLON and WITH_TONE_NUMBER
            outputFormat.setVCharType(PinyinVCharType.WITH_U_AND_COLON);
            outputFormat.setToneType(PinyinToneType.WITH_TONE_NUMBER);

            assertEquals("lu:3", PinyinHelper.toPinyinStringList('吕', outputFormat).get(0));

            // WITH_V and WITH_TONE_NUMBER
            outputFormat.setVCharType(PinyinVCharType.WITH_V);
            outputFormat.setToneType(PinyinToneType.WITH_TONE_NUMBER);

            assertEquals("lv3", PinyinHelper.toPinyinStringList('吕', outputFormat).get(0));

            // WITH_U_UNICODE and WITH_TONE_NUMBER
            outputFormat.setVCharType(PinyinVCharType.WITH_U_UNICODE);
            outputFormat.setToneType(PinyinToneType.WITH_TONE_NUMBER);

            assertEquals("lü3", PinyinHelper.toPinyinStringList('吕', outputFormat).get(0));

            // // WITH_U_AND_COLON and WITHOUT_TONE
            outputFormat.setVCharType(PinyinVCharType.WITH_U_AND_COLON);
            outputFormat.setToneType(PinyinToneType.WITHOUT_TONE);

            assertEquals("lu:", PinyinHelper.toPinyinStringList('吕', outputFormat).get(0));

            // WITH_V and WITHOUT_TONE
            outputFormat.setVCharType(PinyinVCharType.WITH_V);
            outputFormat.setToneType(PinyinToneType.WITHOUT_TONE);

            assertEquals("lv", PinyinHelper.toPinyinStringList('吕', outputFormat).get(0));

            // WITH_U_UNICODE and WITHOUT_TONE
            outputFormat.setVCharType(PinyinVCharType.WITH_U_UNICODE);
            outputFormat.setToneType(PinyinToneType.WITHOUT_TONE);

            assertEquals("lü", PinyinHelper.toPinyinStringList('吕', outputFormat).get(0));

            // WITH_U_AND_COLON and WITH_TONE_MARK is forbidden

            // WITH_V and WITH_TONE_MARK is forbidden

            // WITH_U_UNICODE and WITH_TONE_MARK
            outputFormat.setVCharType(PinyinVCharType.WITH_U_UNICODE);
            outputFormat.setToneType(PinyinToneType.WITH_TONE_MARK);

            assertEquals("lǚ", PinyinHelper.toPinyinStringList('吕', outputFormat).get(0));

            // fix case type to UPPERCASE, change VChar and Tone
            // combination
            outputFormat.setCaseType(PinyinCaseType.UPPERCASE);

            // WITH_U_AND_COLON and WITH_TONE_NUMBER
            outputFormat.setVCharType(PinyinVCharType.WITH_U_AND_COLON);
            outputFormat.setToneType(PinyinToneType.WITH_TONE_NUMBER);

            assertEquals("LU:3", PinyinHelper.toPinyinStringList('吕', outputFormat).get(0));

            // WITH_V and WITH_TONE_NUMBER
            outputFormat.setVCharType(PinyinVCharType.WITH_V);
            outputFormat.setToneType(PinyinToneType.WITH_TONE_NUMBER);

            assertEquals("LV3", PinyinHelper.toPinyinStringList('吕', outputFormat).get(0));

            // WITH_U_UNICODE and WITH_TONE_NUMBER
            outputFormat.setVCharType(PinyinVCharType.WITH_U_UNICODE);
            outputFormat.setToneType(PinyinToneType.WITH_TONE_NUMBER);

            assertEquals("LÜ3", PinyinHelper.toPinyinStringList('吕', outputFormat).get(0));

            // // WITH_U_AND_COLON and WITHOUT_TONE
            outputFormat.setVCharType(PinyinVCharType.WITH_U_AND_COLON);
            outputFormat.setToneType(PinyinToneType.WITHOUT_TONE);

            assertEquals("LU:", PinyinHelper.toPinyinStringList('吕', outputFormat).get(0));

            // WITH_V and WITHOUT_TONE
            outputFormat.setVCharType(PinyinVCharType.WITH_V);
            outputFormat.setToneType(PinyinToneType.WITHOUT_TONE);

            assertEquals("LV", PinyinHelper.toPinyinStringList('吕', outputFormat).get(0));

            // WITH_U_UNICODE and WITHOUT_TONE
            outputFormat.setVCharType(PinyinVCharType.WITH_U_UNICODE);
            outputFormat.setToneType(PinyinToneType.WITHOUT_TONE);

            assertEquals("LÜ", PinyinHelper.toPinyinStringList('吕', outputFormat).get(0));

            // WITH_U_AND_COLON and WITH_TONE_MARK is forbidden

            // WITH_V and WITH_TONE_MARK is forbidden

            // WITH_U_UNICODE and WITH_TONE_MARK
            outputFormat.setVCharType(PinyinVCharType.WITH_U_UNICODE);
            outputFormat.setToneType(PinyinToneType.WITH_TONE_MARK);

            assertEquals("LǙ", PinyinHelper.toPinyinStringList('吕', outputFormat).get(0));
        } catch (BadPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
    }
}
