package org.spin.core.util.excel;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.ExcelNumberFormat;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.spin.core.ErrorCode;
import org.spin.core.function.FinalConsumer;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.DateUtils;
import org.spin.core.util.NumericUtils;
import org.spin.core.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * 基于SaxReader的07格式读取器
 *
 * @author wangy QQ 837195190
 * <p>Created by thinkpad on 2018/5/5.</p>
 */
public class ExcelXlsxReader extends DefaultHandler implements ExcelReader {

    /**
     * 单元格中的数据可能的数据类型
     */
    private enum CellDataType {
        BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER, DATE, NULL
    }

    /**
     * yyyy-MM-dd ---14
     * yyyy年m月d日 ---31
     * yyyy年m月 ---57
     * m月d日 ---58
     * HH:mm ---20
     * h时mm分 ---32
     * yyyy"年"m"月"d"日";@ ---178
     */
    private static final short[] DATE_INT = new short[]{14, 31, 57, 58, 20, 32, 178};

    private static final Map<Character, Integer> ALPHA_NUM = new HashMap<>(52);

    private FinalConsumer<ExcelRow> rowReader;

    /**
     * 共享字符串表
     */
    private SharedStringsTable sst;

    /**
     * 上一次的内容
     */
    private String lastContents;

    /**
     * 字符串标识
     */
    private boolean nextIsString;

    /**
     * 当前列
     */
    private int curCol = -1;

//    private int curRow = -1;

    /**
     * T元素标识
     */
    private boolean isTElement;

    /**
     * 单元格数据类型，默认为字符串类型
     */
    private CellDataType nextDataType = CellDataType.SSTINDEX;

    private final DataFormatter formatter = new DataFormatter();

    private short formatIndex;

    private String formatString;

    /**
     * 单元格
     */
    private StylesTable stylesTable;

    private ExcelRow rowData = new ExcelRow(-1, null, -1, 10);

    static {
        ALPHA_NUM.put('a', 1);
        ALPHA_NUM.put('b', 2);
        ALPHA_NUM.put('c', 3);
        ALPHA_NUM.put('d', 4);
        ALPHA_NUM.put('e', 5);
        ALPHA_NUM.put('f', 6);
        ALPHA_NUM.put('g', 7);
        ALPHA_NUM.put('h', 8);
        ALPHA_NUM.put('i', 9);
        ALPHA_NUM.put('j', 10);
        ALPHA_NUM.put('k', 11);
        ALPHA_NUM.put('l', 12);
        ALPHA_NUM.put('m', 13);
        ALPHA_NUM.put('n', 14);
        ALPHA_NUM.put('o', 15);
        ALPHA_NUM.put('p', 16);
        ALPHA_NUM.put('q', 17);
        ALPHA_NUM.put('r', 18);
        ALPHA_NUM.put('s', 19);
        ALPHA_NUM.put('t', 20);
        ALPHA_NUM.put('u', 21);
        ALPHA_NUM.put('v', 22);
        ALPHA_NUM.put('w', 23);
        ALPHA_NUM.put('x', 24);
        ALPHA_NUM.put('y', 25);
        ALPHA_NUM.put('z', 26);

        ALPHA_NUM.put('A', 1);
        ALPHA_NUM.put('B', 2);
        ALPHA_NUM.put('C', 3);
        ALPHA_NUM.put('D', 4);
        ALPHA_NUM.put('E', 5);
        ALPHA_NUM.put('F', 6);
        ALPHA_NUM.put('G', 7);
        ALPHA_NUM.put('H', 8);
        ALPHA_NUM.put('I', 9);
        ALPHA_NUM.put('J', 10);
        ALPHA_NUM.put('K', 11);
        ALPHA_NUM.put('L', 12);
        ALPHA_NUM.put('M', 13);
        ALPHA_NUM.put('N', 14);
        ALPHA_NUM.put('O', 15);
        ALPHA_NUM.put('P', 16);
        ALPHA_NUM.put('Q', 17);
        ALPHA_NUM.put('R', 18);
        ALPHA_NUM.put('S', 19);
        ALPHA_NUM.put('T', 20);
        ALPHA_NUM.put('U', 21);
        ALPHA_NUM.put('V', 22);
        ALPHA_NUM.put('W', 23);
        ALPHA_NUM.put('X', 24);
        ALPHA_NUM.put('Y', 25);
        ALPHA_NUM.put('Z', 26);
    }

    @Override
    public void process(InputStream is, FinalConsumer<ExcelRow> rowReader) {
        this.rowReader = rowReader;
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        XSSFReader.SheetIterator sheets;
        SAXParser parser;
        try {
            parser = parserFactory.newSAXParser();
            OPCPackage pkg = OPCPackage.open(is);
            XSSFReader xssfReader = new XSSFReader(pkg);
            stylesTable = xssfReader.getStylesTable();
            sst = xssfReader.getSharedStringsTable();
            sheets = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "文件读取异常", e);
        } catch (OpenXML4JException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "XML解析异常", e);
        } catch (SAXException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "SAX解析异常", e);
        } catch (ParserConfigurationException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "SAX解析器创建异常", e);
        }

        while (sheets.hasNext()) {
            rowData.cleanRow();
            rowData.sheetIdxInc();
            try (InputStream sheet = sheets.next()) {
                rowData.setSheetName(sheets.getSheetName());
                InputSource sheetSource = new InputSource(sheet);
                parser.parse(sheetSource, this);
            } catch (IOException e) {
                throw new SpinException(ErrorCode.IO_FAIL, "文件读取异常", e);
            } catch (SAXException e) {
                throw new SpinException(ErrorCode.IO_FAIL, "SAX解析异常", e);
            }
        }
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) {
        // c => 单元格
        if ("c".equals(name)) {
            // 设定单元格类型
            setNextDataType(attributes);
            // Figure out if the value is an index in the SST
            String cellType = attributes.getValue("t");
            nextIsString = cellType != null && cellType.equals("s");
        }

        // 当元素为t时
        isTElement = "t".equals(name);

        // 置空
        lastContents = "";
    }

    @Override
    public void endElement(String uri, String localName, String name) {
        // 根据SST的索引值的到单元格的真正要存储的字符串
        // 这时characters()方法可能会被调用多次
        if (nextIsString && StringUtils.isNotEmpty(lastContents) && StringUtils.isNumeric(lastContents)) {
            int idx = Integer.parseInt(lastContents);
            lastContents = sst.getItemAt(idx).toString();
        }

        // t元素也包含字符串
        if (isTElement) {
            // 将单元格内容加入rowlist中，在这之前先去掉字符串前后的空白符
            String value = lastContents.trim();
            rowData.setColumn(curCol, value);
            isTElement = false;
        } else if ("v".equals(name)) {
            // v => 单元格的值，如果单元格是字符串则v标签的值为该字符串在SST中的索引
            String value = getDataValue(lastContents.trim());
            rowData.setColumn(curCol, value);
        } else {
            // 如果标签名称为 row ，这说明已到行尾，调用 optRows() 方法
            if (name.equals("row")) {
                if (rowData.isNotBlank()) {
                    rowReader.accept(rowData);
                }

                if (rowData.getColumnNum() > 0 && rowData.getColumnNum() != rowData.getLength()) {
                    rowData.setLength(rowData.getColumnNum());
                }
                rowData.cleanRow();
                rowData.rowIdxInc();
                curCol = -1;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        // 得到单元格内容的值
        lastContents += new String(ch, start, length);
    }

    private void setNextDataType(Attributes attributes) {
        String r = attributes.getValue("r");
        resolveColIdx(r);

        nextDataType = CellDataType.NUMBER;
        formatIndex = -1;
        formatString = null;
        String cellType = attributes.getValue("t");
        String cellStyleStr = attributes.getValue("s");

        if ("b".equals(cellType)) {
            nextDataType = CellDataType.BOOL;
        } else if ("e".equals(cellType)) {
            nextDataType = CellDataType.ERROR;
        } else if ("inlineStr".equals(cellType)) {
            nextDataType = CellDataType.INLINESTR;
        } else if ("s".equals(cellType)) {
            nextDataType = CellDataType.SSTINDEX;
        } else if ("str".equals(cellType)) {
            nextDataType = CellDataType.FORMULA;
        }

        if (cellStyleStr != null) {
            int styleIndex = Integer.parseInt(cellStyleStr);
            XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
            formatIndex = style.getDataFormat();
            formatString = style.getDataFormatString();

            if (Arrays.binarySearch(DATE_INT, formatIndex) >= 0 && isMightBeDateFormatted(style)) {
                nextDataType = CellDataType.DATE;
                formatString = "yyyy-MM-dd hh:mm:ss";
            }

            if (formatString == null) {
                nextDataType = CellDataType.NULL;
                formatString = BuiltinFormats.getBuiltinFormat(formatIndex);
            }
        }
    }

    /**
     * 对解析出来的数据进行类型处理
     *
     * @param value 单元格的值（这时候是一串数字）
     */
    private String getDataValue(String value) {
        String thisStr;
        switch (nextDataType) {
            // 这几个的顺序不能随便交换，交换了很可能会导致数据错误
            case BOOL:
                char first = value.charAt(0);
                thisStr = first == '0' ? "FALSE" : "TRUE";
                break;
            case ERROR:
                thisStr = "\"ERROR:" + value + '"';
                break;
            case FORMULA:
                thisStr = '"' + value + '"';
                break;
            case INLINESTR:
                XSSFRichTextString rtsi = new XSSFRichTextString(value);
                thisStr = rtsi.toString();
                break;
            case SSTINDEX:
                try {
                    int idx = Integer.parseInt(value);
                    RichTextString rtss = sst.getItemAt(idx);
                    thisStr = rtss.toString();
                } catch (NumberFormatException ex) {
                    thisStr = value;
                }
                break;
            case NUMBER:
                if (formatString != null) {
                    thisStr = formatter.formatRawCellContents(Double.parseDouble(value), formatIndex, formatString).trim();
                } else {
                    thisStr = value;
                }
                thisStr = thisStr.replace("_", StringUtils.EMPTY).trim();
                break;
            case DATE:
                if (NumericUtils.isNum(value)) {
                    thisStr = DateUtils.formatDateForSecond(HSSFDateUtil.getJavaDate(Double.parseDouble(value)));
                } else {
                    thisStr = value;
                }
                break;
            default:
                thisStr = " ";
                break;
        }

        return thisStr;
    }

    private void resolveColIdx(String colName) {
        int idx = -1;
        if (StringUtils.isNotEmpty(colName)) {
            String col = colName.split("\\d")[0];
            for (int i = 0; i != col.length(); ++i) {
                char c = col.charAt(i);
                idx = idx + ALPHA_NUM.get(c) * (col.length() - i > 1 ? (int) (Math.pow(26, col.length() - 1 - i)) : 1);
            }
            curCol = idx;
            rowData.setRowIndex(Integer.parseInt(colName.substring(col.length())) - 1);
        }
    }

    private static boolean isMightBeDateFormatted(CellStyle style) {
        if (style == null) return false;
        boolean bDate;

        ExcelNumberFormat nf = ExcelNumberFormat.from(style);
        bDate = DateUtil.isADateFormat(nf);
        return bDate;
    }

    public static boolean isCellDateFormatted(double cellValue, CellStyle style) {
        if (style == null) return false;
        boolean bDate = false;

        if (DateUtil.isValidExcelDate(cellValue)) {
            ExcelNumberFormat nf = ExcelNumberFormat.from(style);
            bDate = DateUtil.isADateFormat(nf);
        }
        return bDate;
    }
}
