package org.spin.core.util.excel;

import org.spin.core.Assert;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Excel表格定义
 * Created by xuweinan on 2017/2/7.
 *
 * @author xuweinan
 */
public class ExcelGrid implements Serializable {
    private static final long serialVersionUID = 3306654738869974692L;

    private String fileName;

    private List<ExcelSheet> sheets = new LinkedList<>();


    public static ExcelGrid ofFileName(String fileName) {
        return new ExcelGrid(fileName);
    }

    public ExcelGrid() {
    }

    public ExcelGrid(String fileName) {
        this.fileName = fileName;
    }

    public ExcelGrid appendSheet(GridColumn... columns) {
        sheets.add(new ExcelSheet(validateSheetName(null)).appendColumns(columns));
        return this;
    }

    public ExcelGrid appendSheet(Consumer<ExcelSheet> sheetConsumer) {
        ExcelSheet sheet = new ExcelSheet(validateSheetName(null));
        sheetConsumer.accept(sheet);
        sheets.add(sheet);
        return this;
    }

    public ExcelGrid appendSheet(String sheetName, GridColumn... columns) {
        sheets.add(new ExcelSheet(validateSheetName(sheetName)).appendColumns(columns));
        return this;
    }

    public ExcelGrid appendSheet(String sheetName, Consumer<ExcelSheet> sheetConsumer) {
        ExcelSheet sheet = new ExcelSheet(validateSheetName(sheetName));
        sheetConsumer.accept(sheet);
        sheets.add(sheet);
        return this;
    }

    public ExcelGrid appendSheets(Iterable<ExcelSheet> sheets) {
        sheets.forEach(it -> {
            it.setSheetName(validateSheetName(it.getSheetName()));
            this.sheets.add(it);
        });
        return this;
    }

    public ExcelGrid appendSheets(ExcelSheet... sheets) {
        for (ExcelSheet sheet : sheets) {
            sheet.setSheetName(validateSheetName(sheet.getSheetName()));
            this.sheets.add(sheet);
        }
        return this;
    }

    public ExcelGrid withSheets(List<ExcelSheet> sheets) {
        this.sheets.clear();
        appendSheets(sheets);
        return this;
    }

    public ExcelGrid removeSheets(Predicate<ExcelSheet> predicate) {
        sheets = sheets.stream().filter(it -> !predicate.test(it)).collect(Collectors.toCollection(LinkedList::new));
        return this;
    }

    public ExcelGrid removeSheet(String sheetName) {
        sheets.removeIf(it -> it.getSheetName().equals(sheetName));
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<ExcelSheet> getSheets() {
        return sheets;
    }

    private String validateSheetName(String sheetName) {
        if (null == sheetName) {
            sheetName = "Sheet" + (sheets.size() + 1);
        }
        Assert.notTrue(sheets.stream().map(ExcelSheet::getSheetName).anyMatch(sheetName::equals), "Sheet名称[" + sheetName + "]在当前工作簿中已经存在");
        return sheetName;
    }
}
