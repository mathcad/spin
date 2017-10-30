package com.shipping.controller.api;

import com.shipping.domain.sys.File;
import com.shipping.service.FileService;
import org.spin.core.throwable.SimplifiedException;
import org.spin.web.FileOperator;
import org.spin.web.RestfulResponse;
import org.spin.web.annotation.RestfulApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文件上传
 * <p>Created by xuweinan on 2017/4/26.</p>
 *
 * @author xuweinan
 */
@RestController
@RequestMapping("/api/file")
public class FileApiController {

    @Autowired
    private FileService fileService;

    /**
     * 上传
     *
     * @param file 上传文件
     */
    @RestfulApi(auth = false, path = "upload", method = RequestMethod.POST)
    public RestfulResponse upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new SimplifiedException("没有上传文件");
        }
        Map<String, Object> resultMap = new HashMap<>();
        try {
            FileOperator.UploadResult uploadResult = FileOperator.upload(file, false);
            File f = fileService.saveFile(uploadResult);
            resultMap.put("fileId", f.getId());
            resultMap.put("storeName", f.getFilePath());

        } catch (IOException e) {
            throw new SimplifiedException("上传失败！");
        }
        return RestfulResponse.ok(resultMap);
    }

    /**
     * 批量上传
     *
     * @param files 上传文件
     */
    @RestfulApi(auth = false, path = "batchUpload", method = RequestMethod.POST)
    public RestfulResponse batchUpload(@RequestParam("files") List<MultipartFile> files) {
        if (null == files || 0 == files.size()) {
            throw new SimplifiedException("没有上传文件");
        }
        List<Map<String, Object>> results;
        try {
            List<FileOperator.UploadResult> uploadResults = FileOperator.upload(files, false);
            results = fileService.saveFiles(uploadResults).stream().map(f -> {
                Map<String, Object> res = new HashMap<>();
                res.put("fileId", f.getId());
                res.put("storeName", f.getFilePath());
                return res;
            }).collect(Collectors.toList());
            return RestfulResponse.ok(results);
        } catch (Exception e) {
            throw new SimplifiedException("上传失败！");
        }
    }
}
