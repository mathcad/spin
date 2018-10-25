package com.shipping.service

import com.shipping.domain.sys.File
import org.spin.data.extend.RepositoryContext
import org.spin.web.FileOperator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 文件服务
 *
 * Created by xuweinan on 2017/4/26.
 *
 * @author xuweinan
 */
@Service
class FileService {

    @Autowired
    private lateinit var repoCtx: RepositoryContext

    @Transactional
    fun saveFile(uploadResult: FileOperator.UploadResult): File =
        repoCtx.save(File(guid = UUID.randomUUID().toString(),
            originName = uploadResult.originName,
            fileName = uploadResult.storeName.substring(uploadResult.storeName.lastIndexOf('/') + 1),
            filePath = uploadResult.storeName,
            extension = uploadResult.extention,
            size = uploadResult.size
        ))

    @Transactional
    fun saveFiles(uploadResults: Collection<FileOperator.UploadResult>): List<File> =
        uploadResults.map {
           saveFile(it)
        }

}
