package com.shipping.service;

import com.shipping.domain.sys.Function;
import com.shipping.repository.sys.FunctionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Objects;

/**
 * <p>Created by xuweinan on 2017/9/4.</p>
 *
 * @author xuweinan
 */
@Service
public class FunctionService {
    private static final Logger logger = LoggerFactory.getLogger(FunctionService.class);

    @Autowired
    private FunctionRepository functionDao;

    public void add(Function func) {
        if (Objects.nonNull(func.getParent())) {
            Function parent = functionDao.get(func.getParent().getId());
            functionDao.save(func, true);
            functionDao.doWork(connection -> {Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM sys_function");});
        }
    }
}
