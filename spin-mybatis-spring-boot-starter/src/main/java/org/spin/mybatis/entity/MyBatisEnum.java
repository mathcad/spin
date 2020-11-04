package org.spin.mybatis.entity;

import com.baomidou.mybatisplus.annotation.IEnum;
import org.spin.core.trait.FrendlyEnum;

import java.io.Serializable;

/**
 * 标记MyBatis的整型枚举属性
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/9/9</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface MyBatisEnum<T extends Serializable> extends IEnum<T>, FrendlyEnum<T> {
}
