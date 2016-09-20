package org.arvin.test.domain;


import org.infrastructure.annotations.UserEnum;

/**
 * 用户类型
 * 
 * @author zhou
 * @contact 电话: 18963752887, QQ: 251915460
 * @create 2015年3月18日 上午11:31:52
 * @version V1.0
 */
@UserEnum("用户类型")
public enum UserTypeE {
	车辆(0), 货主(1), 货保车保(3),内部用户(7),车队(9),车网代(13),货网代(14);

	public int value;

	UserTypeE(int value) {
		this.value = value;
	}
}
