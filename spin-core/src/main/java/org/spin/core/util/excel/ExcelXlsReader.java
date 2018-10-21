package org.spin.core.util.excel;


import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener;
import org.apache.poi.hssf.eventusermodel.FormatTrackingHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.eventusermodel.MissingRecordAwareHSSFListener;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.ColumnInfoRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.spin.core.function.FinalConsumer;
import org.spin.core.util.DateUtils;
import org.spin.core.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * 基于事件驱动的03格式的读取器
 *
 * @author wangy QQ 837195190
 * <p>Created by thinkpad on 2018/5/5.</p>
 */
public class ExcelXlsReader implements ExcelReader, HSSFListener {
    private static final int[] DATE_INT = new int[]{14, 31, 57, 58, 20, 32, 178};

    private boolean outputFormulaValues = true;

    private SheetRecordCollectingListener workbookBuildingListener;

    // excel2003工作薄
    private HSSFWorkbook stubWorkbook;

    // Records we pick up as we process
    private SSTRecord sstRecord;

    private FormatTrackingHSSFListener formatListener;

    private BoundSheetRecord[] orderedBSRs;

    private ArrayList<BoundSheetRecord> boundSheetRecords = new ArrayList<>();

    private boolean outputNextStringRecord;

    // 存储行记录的容器
    private ExcelRow rowData = new ExcelRow();

    private FinalConsumer<ExcelRow> rowReader;

    private int currentCol = -1;

    /**
     * 遍历excel下所有的sheet
     */
    @Override
    public void process(InputStream is, FinalConsumer<ExcelRow> rowReader) throws IOException {
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
        POIFSFileSystem fs = new POIFSFileSystem(is);
        factory.processWorkbookEvents(request, fs);
    }

    /**
     * HSSFListener 监听方法，处理 Record
     */
    @Override
    public void processRecord(Record record) {
        String value;
        switch (record.getSid()) {
            case ColumnInfoRecord.sid:
                int colNum = ((ColumnInfoRecord) record).getLastColumn() + 1;
                if (colNum > rowData.getColumnNum()) {
                    rowData.setLength(colNum);
                }
                break;
            case BoundSheetRecord.sid:
                boundSheetRecords.add((BoundSheetRecord) record);
                break;
            case BOFRecord.sid:
                BOFRecord br = (BOFRecord) record;
                if (br.getType() == BOFRecord.TYPE_WORKSHEET) {
                    // 如果有需要，则建立子工作薄
                    if (workbookBuildingListener != null && stubWorkbook == null) {
                        stubWorkbook = workbookBuildingListener.getStubHSSFWorkbook();
                    }

                    rowData.sheetIdxInc();
                    if (orderedBSRs == null) {
                        orderedBSRs = BoundSheetRecord.orderByBofPosition(boundSheetRecords);
                    }
                    rowData.setSheetName(orderedBSRs[rowData.getSheetIndex()].getSheetname());
                }
                break;
            case SSTRecord.sid:
                sstRecord = (SSTRecord) record;
                break;
            case BlankRecord.sid:
                rowData.setColumn(((BlankRecord) record).getColumn(), StringUtils.EMPTY);
                break;
            case BoolErrRecord.sid: // 单元格为布尔类型
                BoolErrRecord berec = (BoolErrRecord) record;
                value = String.valueOf(berec.getBooleanValue());
                rowData.setColumn(berec.getColumn(), value);
                break;
            case FormulaRecord.sid: // 单元格为公式类型
                FormulaRecord frec = (FormulaRecord) record;
                if (outputFormulaValues) {
                    if (1 == frec.getCachedResultType()) {
                        // Formula result is a string
                        // This is stored in the next record
                        currentCol = frec.getColumn();
                        outputNextStringRecord = true;
                    } else {
                        value = formatListener.formatNumberDateCell(frec);
                        rowData.setColumn(frec.getColumn(), value);
                    }
                } else {
                    value = HSSFFormulaParser.toFormulaString(stubWorkbook, frec.getParsedExpression());
                    rowData.setColumn(frec.getColumn(), value);
                }
                break;
            case StringRecord.sid:// 单元格中公式的字符串
                if (outputNextStringRecord && currentCol != -1) {
                    // String for formula
                    StringRecord srec = (StringRecord) record;
                    rowData.setColumn(currentCol, srec.getString());
                    currentCol = -1;
                    outputNextStringRecord = false;
                }
                break;
            case LabelRecord.sid:
                LabelRecord lrec = (LabelRecord) record;
                value = lrec.getValue().trim();
                rowData.setColumn(lrec.getColumn(), value);
                break;
            case LabelSSTRecord.sid: // 单元格为字符串类型
                LabelSSTRecord lsrec = (LabelSSTRecord) record;
                if (sstRecord == null) {
                    rowData.setColumn(lsrec.getColumn(), StringUtils.EMPTY);
                } else {
                    value = sstRecord.getString(lsrec.getSSTIndex()).toString().trim();
                    rowData.setColumn(lsrec.getColumn(), value);
                }
                break;
            case NumberRecord.sid: // 单元格为数字类型
                NumberRecord numrec = (NumberRecord) record;
                if (Arrays.binarySearch(DATE_INT, formatListener.getFormatIndex(numrec)) >= 0) {
                    value = DateUtils.formatDateForSecond(HSSFDateUtil.getJavaDate(numrec.getValue()));
                } else {
                    value = formatListener.formatNumberDateCell(numrec).trim();
                }
                // 向容器加入列值
                rowData.setColumn(numrec.getColumn(), value);
                break;
            default:
                break;
        }

        // 行结束时的操作
        if (record instanceof LastCellOfRowDummyRecord) {
            rowData.rowIdxInc();
            // 每行结束时， 调用getRows() 方法
            if (rowData.isNotBlank()) {
                rowReader.accept(rowData);
            }
            // 清空容器
            rowData.cleanRow();
        }
    }
}
