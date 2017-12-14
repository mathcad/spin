package com.shipping.service

import com.shipping.domain.sys.File
import com.shipping.repository.sys.FileRepository
import org.spin.web.FileOperator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.util.ArrayList
import java.util.UUID

/**
 * 文件服务
 *
 * Created by xuweinan on 2017/4/26.
 *
 * @author xuweinan
 */
@Service
open class FileService {

    @Autowired
    private lateinit var fileDao: FileRepository

    @Transactional
    open fun saveFile(uploadResult: FileOperator.UploadResult): File {
        val file = File()
        file.guid = UUID.randomUUID().toString()
        file.originName = uploadResult.originName
        file.fileName = uploadResult.storeName.substring(uploadResult.storeName.lastIndexOf('/') + 1)
        file.filePath = uploadResult.storeName
        file.extension = uploadResult.extention
        file.size = uploadResult.size
        return fileDao.save(file)
    }

    @Transactional
    open fun saveFiles(uploadResults: Collection<FileOperator.UploadResult>): List<File> {
        val result = ArrayList<File>()
        for (uploadResult in uploadResults) {
            val file = File()
            file.guid = UUID.randomUUID().toString()
            file.originName = uploadResult.originName
            file.fileName = uploadResult.storeName.substring(uploadResult.storeName.lastIndexOf('/') + 1)
            file.filePath = uploadResult.storeName
            file.extension = uploadResult.extention
            file.size = uploadResult.size
            result.add(fileDao.save(file))
        }
        return result
    }
}
