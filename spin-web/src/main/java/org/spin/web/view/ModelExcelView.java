package org.spin.web.view;

import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;
import org.spin.core.util.excel.ExcelModel;
import org.spin.core.util.excel.ExcelUtils;
import org.spin.core.util.file.FileType;
import org.spin.web.RestfulResponse;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Created by xuweinan on 2017/2/7.
 *
 * @author xuweinan
 */
public class ModelExcelView extends AbstractView {

    private static final String DEFAULT_FILE_NAME = "export";

    private final FileType.Document fileType;

    private final ExcelModel excelModel;

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
    protected final void renderMergedOutputModel(@NonNull Map<String, Object> model,
                                                 @NonNull HttpServletRequest request,
                                                 @NonNull HttpServletResponse response) {

        // Set the content type.
        response.setContentType(getContentType());

        String fileName = StringUtils.isNotEmpty(excelModel.getGrid().getFileName()) ? excelModel.getGrid().getFileName() : DEFAULT_FILE_NAME;
        fileName = StringUtils.urlEncode(fileName.endsWith(fileType.getFirstExt()) ? fileName : fileName + fileType.getFirstExt());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);

        // Create a fresh workbook instance for this render step and flush byte array to servlet output stream
        ServletOutputStream outputStream;
        try {
            outputStream = response.getOutputStream();
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "Excel写出workbook失败", e);
        }

        try {
            ExcelUtils.generateWorkBook(fileType, excelModel, outputStream);
        } catch (SpinException e) {
            if (e.getExceptionType().getCode() == ErrorCode.IO_FAIL.getCode()) {
                throw e;
            } else {
                response.setCharacterEncoding("UTF-8");
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setHeader("Encoded", "1");
                error(outputStream, e.getExceptionType(), e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Excel渲染发生错误", e);
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Encoded", "1");
            error(outputStream, ErrorCode.INTERNAL_ERROR);
        }
    }

    private void error(OutputStream response, ErrorCode errorCode, String... message) {
        try {
            response.write(JsonUtils.toJson(RestfulResponse
                .error(errorCode, ((null == message || message.length == 0 || StringUtils.isEmpty(message[0])) ? errorCode.getDesc() : message[0]))).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
