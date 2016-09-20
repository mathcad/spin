package org.arvin.test.domain;


import org.infrastructure.annotations.UserEnum;

/**
 * 机构团队类型
 * @author gqs
 * @contact 电话: 15375536619, QQ: 240306310
 * @create 2016年3月25日10:17:15
 * @version V1.4.2 
 */
@UserEnum("机构团队类型")
public enum OrganTeamTypeE {
	地推团队(1);

	int value;
	OrganTeamTypeE(int value) {
		this.value = value;
	}
}
