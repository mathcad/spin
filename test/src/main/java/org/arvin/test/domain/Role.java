/**
 * 
 */
package org.arvin.test.domain;

import org.hibernate.annotations.Type;
import org.infrastructure.jpa.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**   
 * 系统角色
 * @author lijian
 * @contact 电话: 18055335518, QQ: 2630418388
 * @create 2015-4-21 21:28:24
 * @version V1.0   
 */
@Entity
@Table(name = "sys_role")
public class Role extends AbstractEntity implements java.io.Serializable {

	/**	名称**/
	@Column(length = 20)
	private String name;

	/**	代码**/
	@Column(length = 20)
	private String code;

	/**	超级用户**/
	@Column
	boolean admin;
	
	/** 内部用户(0), 货主(1), 司机(2); */
	@Column(length = 2)
	@Type(type = "org.infrastructure.jpa.extend.UserEnumType")
	UserTypeE type;

	/**	备注**/
	@Column(length = 100)
	String remark;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public UserTypeE getType() {
		return type;
	}

	public void setType(UserTypeE type) {
		this.type = type;
	}

    @Override
    public String toString() {
        return "[" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", admin=" + admin +
                ", type=" + (null != type ? type.value: null) +
                ", remark='" + remark + '\'' +
                ']';
    }
}
