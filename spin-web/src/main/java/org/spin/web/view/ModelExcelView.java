package org.spin.web.view;

import org.apache.poi.ss.usermodel.Workbook;
import org.spin.core.Assert;
import org.spin.core.util.StringUtils;
import org.spin.core.util.excel.ExcelModel;
import org.spin.core.util.excel.ExcelUtils;
import org.spin.core.util.file.FileType;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by xuweinan on 2017/2/7.
 *
 * @author xuweinan
 */
public class ModelExcelView extends AbstractView {

    private static final String defaultFileName = "export";

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
    protected final void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Create a fresh workbook instance for this render step.
        Workbook workbook = ExcelUtils.generateWorkbook(fileType, excelModel);

        // Set the content type.
        response.setContentType(getContentType());

        String fileName = StringUtils.isNotEmpty(excelModel.getGrid().getFileName()) ? excelModel.getGrid().getFileName() : defaultFileName;
        fileName = StringUtils.urlEncode(fileName.endsWith(fileType.getExtension()) ? fileName : fileName + fileType.getExtension());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);

        // Flush byte array to servlet output stream.
        renderWorkbook(workbook, response);
    }


    /**
     * The actual render step: taking the POI {@link Workbook} and rendering
     * it to the given response.
     *
     * @param workbook the POI Workbook to render
     * @param response current HTTP response
     * @throws IOException when thrown by I/O methods that we're delegating to
     */
    private void renderWorkbook(Workbook workbook, HttpServletResponse response) throws IOException {
        ServletOutputStream out = response.getOutputStream();
        workbook.write(out);
        out.flush();
        out.close();
        workbook.close();
    }
}
