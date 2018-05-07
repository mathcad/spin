package org.spin.core.util.excel;


import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener;
import org.apache.poi.hssf.eventusermodel.*;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.spin.core.collection.FixedVector;
import org.spin.core.util.DateUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * description
 *
 * @author wangy QQ 837195190
 * <p>Created by thinkpad on 2018/5/5.<p/>
 */

public class ExcelXlsReader implements HSSFListener {

    private int minColumns = -1;

    private POIFSFileSystem fs;

    private int lastRowNumber;

    private int lastColumnNumber;

    /**
     * Should we output the formula, or the value it has?
     */
    private boolean outputFormulaValues = true;

    /**
     * For parsing Formulas
     */
    private SheetRecordCollectingListener workbookBuildingListener;

    // excel2003工作薄
    private HSSFWorkbook stubWorkbook;

    // Records we pick up as we process
    private SSTRecord sstRecord;

    private FormatTrackingHSSFListener formatListener;

    // 表索引
    private int sheetIndex = -1;

    private BoundSheetRecord[] orderedBSRs;

    private ArrayList boundSheetRecords = new ArrayList();

    // For handling formulas with string results
    private int nextRow;

    private int nextColumn;

    private boolean outputNextStringRecord;

    // 当前行
    private int curRow = 0;

    // 存储行记录的容器
    private FixedVector<String> rowData = null;
    private String sheetName;

    private RowReader rowReader;


    /**
     * 遍历excel下所有的sheet
     */
    public void process(InputStream is, RowReader rowReader) {
        try {
            this.fs = new POIFSFileSystem(is);
            this.rowReader = rowReader;
            MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
            formatListener = new FormatTrackingHSSFListener(listener);
            HSSFEventFactory factory = new HSSFEventFactory();
            HSSFRequest request = new HSSFRequest();
            if (outputFormulaValues) {
                request.addListenerForAllRecords(formatListener);
            } else {
                workbookBuildingListener = new SheetRecordCollectingListener(formatListener);
                request.addListenerForAllRecords(workbookBuildingListener);
            }
            factory.processWorkbookEvents(request, fs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int[] dateInt = new int[]{14, 31, 57, 58, 20, 32, 178};
    /**
     * HSSFListener 监听方法，处理 Record
     */
    public void processRecord(Record record) {
        int thisRow = -1;
        int thisColumn = -1;
        String value = null;
        switch (record.getSid()) {
            case BoundSheetRecord.sid:
                boundSheetRecords.add(record);
                break;
            case BOFRecord.sid:
//                record.getRecordSize()
                BOFRecord br = (BOFRecord) record;
                if (br.getType() == BOFRecord.TYPE_WORKSHEET) {
                    // 如果有需要，则建立子工作薄
                    if (workbookBuildingListener != null && stubWorkbook == null) {
                        stubWorkbook = workbookBuildingListener.getStubHSSFWorkbook();
                    }

                    sheetIndex++;
                    if (orderedBSRs == null) {
                        orderedBSRs = BoundSheetRecord.orderByBofPosition(boundSheetRecords);
                    }
                    rowData = new FixedVector<>(orderedBSRs[sheetIndex].getRecordSize() * 3);
                    sheetName = orderedBSRs[sheetIndex].getSheetname();
                }
                break;

            case SSTRecord.sid:
                sstRecord = (SSTRecord) record;
                break;

            case BlankRecord.sid:
                BlankRecord brec = (BlankRecord) record;
                thisRow = brec.getRow();
                rowData.add("");
                break;
            case BoolErrRecord.sid: // 单元格为布尔类型
                BoolErrRecord berec = (BoolErrRecord) record;
                value = berec.getBooleanValue() + "";
                rowData.add(value);
                break;

            case FormulaRecord.sid: // 单元格为公式类型
                FormulaRecord frec = (FormulaRecord) record;
                if (outputFormulaValues) {
                    if (Double.isNaN(frec.getValue())) {
                        // Formula result is a string
                        // This is stored in the next record
                        outputNextStringRecord = true;
                        nextRow = frec.getRow();
                        nextColumn = frec.getColumn();
                    } else {
                        value = formatListener.formatNumberDateCell(frec);
                    }
                } else {
                    value = '"' + HSSFFormulaParser.toFormulaString(stubWorkbook, frec.getParsedExpression()) + '"';
                }
                rowData.add(value);
                break;
            case StringRecord.sid:// 单元格中公式的字符串
                if (outputNextStringRecord) {
                    // String for formula
//                    StringRecord srec = (StringRecord) record;
//                    thisStr = srec.getString();
                    thisRow = nextRow;
                    thisColumn = nextColumn;
                    outputNextStringRecord = false;
                }
                break;
            case LabelRecord.sid:
                LabelRecord lrec = (LabelRecord) record;
                curRow = thisRow = lrec.getRow();
                value = lrec.getValue().trim();
                rowData.add(value);
                break;
            case LabelSSTRecord.sid: // 单元格为字符串类型
                LabelSSTRecord lsrec = (LabelSSTRecord) record;
                curRow = thisRow = lsrec.getRow();
                if (sstRecord == null) {
                    rowData.add("");
                } else {
                    value = sstRecord.getString(lsrec.getSSTIndex()).toString().trim();
                    rowData.add(value);
                }
                break;
            case NumberRecord.sid: // 单元格为数字类型
                NumberRecord numrec = (NumberRecord) record;
                curRow = thisRow = numrec.getRow();
                if (Arrays.binarySearch(dateInt, formatListener.getFormatIndex(numrec)) >= 0) {
                    value = DateUtils.formatDateForSecond(HSSFDateUtil.getJavaDate(numrec.getValue()));
                } else {
                    value = formatListener.formatNumberDateCell(numrec).trim();
                }
                // 向容器加入列值
                rowData.add(value);
                break;
            default:
                break;
        }

        // 遇到新行的操作
        if (thisRow != -1 && thisRow != lastRowNumber) {
            lastColumnNumber = -1;
        }

        // 空值的操作
        if (record instanceof MissingCellDummyRecord) {
            MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
            curRow = thisRow = mc.getRow();
            thisColumn = mc.getColumn();
        }

        // 更新行和列的值
        if (thisRow > -1)
            lastRowNumber = thisRow;
        if (thisColumn > -1)
            lastColumnNumber = thisColumn;

        // 行结束时的操作
        if (record instanceof LastCellOfRowDummyRecord) {
            if (minColumns > 0) {
                // 列值重新置空
                if (lastColumnNumber == -1) {
                    lastColumnNumber = 0;
                }
            }
            lastColumnNumber = -1;

            // 每行结束时， 调用getRows() 方法
            rowReader.readRow(new RowData(sheetIndex, sheetName, curRow, rowData));
            // 清空容器
            rowData.clear();
        }
    }
}


