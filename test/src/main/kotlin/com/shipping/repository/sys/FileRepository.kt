package com.shipping.repository.sys

import com.shipping.domain.sys.File
import org.spin.data.core.ARepository
import org.springframework.stereotype.Repository

/**
 *
 * Created by xuweinan on 2017/7/26.
 *
 * @author xuweinan
 */
@Repository
class FileRepository : ARepository<File, Long>()
