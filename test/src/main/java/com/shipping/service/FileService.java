package com.shipping.service;

import com.shipping.domain.sys.File;
import com.shipping.repository.sys.FileRepository;
import org.spin.web.FileOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务
 * <p>Created by xuweinan on 2017/4/26.</p>
 *
 * @author xuweinan
 */
@Service
public class FileService {

    @Autowired
    private FileRepository fileDao;

    @Transactional
    public File saveFile(FileOperator.UploadResult uploadResult) {
        File file = new File();
        file.setGuid(UUID.randomUUID().toString());
        file.setOriginName(uploadResult.getOriginName());
        file.setFileName(uploadResult.getStoreName().substring(uploadResult.getStoreName().lastIndexOf('/') + 1));
        file.setFilePath(uploadResult.getStoreName());
        file.setExtension(uploadResult.getExtention());
        file.setSize(uploadResult.getSize());
        return fileDao.save(file);
    }

    @Transactional
    public List<File> saveFiles(Collection<FileOperator.UploadResult> uploadResults) {
        List<File> result = new ArrayList<>();
        for (FileOperator.UploadResult uploadResult : uploadResults) {
            File file = new File();
            file.setGuid(UUID.randomUUID().toString());
            file.setOriginName(uploadResult.getOriginName());
            file.setFileName(uploadResult.getStoreName().substring(uploadResult.getStoreName().lastIndexOf('/') + 1));
            file.setFilePath(uploadResult.getStoreName());
            file.setExtension(uploadResult.getExtention());
            file.setSize(uploadResult.getSize());
            result.add(fileDao.save(file));
        }
        return result;
    }
}
