package org.spin.common.web.view;

import org.spin.core.Assert;
import org.spin.core.util.StringUtils;
import org.spin.core.util.excel.ExcelModel;
import org.spin.core.util.excel.ExcelUtils;
import org.spin.core.util.file.FileType;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by xuweinan on 2017/2/7.
 *
 * @author xuweinan
 */
public class ModelExcelView extends AbstractView {

    private static final String DEFAULT_FILE_NAME = "export";

    private FileType.Document fileType;

    private ExcelModel excelModel;

    public ModelExcelView(FileType.Document fileType, ExcelModel excelModel) {
        this.fileType = Assert.notNull(fileType, "Excel文件类型不能为空");
        setContentType(this.fileType.getContentType());
        this.excelModel = excelModel;
    }

    public ModelExcelView(ExcelModel excelModel) {
        this(FileType.Document.XLSX, excelModel);
    }


    @Override
    protected boolean generatesDownloadContent() {
        return true;
    }

    @Override
    protected final void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

        // Set the content type.
        response.setContentType(getContentType());

        String fileName = StringUtils.isNotEmpty(excelModel.getGrid().getFileName()) ? excelModel.getGrid().getFileName() : DEFAULT_FILE_NAME;
        fileName = StringUtils.urlEncode(fileName.endsWith(fileType.getExtension()) ? fileName : fileName + fileType.getExtension());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);

        // Create a fresh workbook instance for this render step and flush byte array to servlet output stream
        ExcelUtils.generateWorkBook(fileType, excelModel, response::getOutputStream);
    }
}
