package org.infrastructure.jpa.core;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 通用实体类型
* 
* @author zhou
* @contact 电话: 18963752887, QQ: 251915460
* @create 2015年3月21日 上午11:26:45 
* @version V1.0
 */
@MappedSuperclass
public abstract class AEntity implements Serializable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	Long id;
	
	@Column(updatable=false)
	String createBy;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(updatable = false)
	AUser createUser;
	
	@Column(name="create_time", nullable=false, updatable=false)   
	Timestamp createTime;
	
	String lastUpdateBy;
	
	@ManyToOne(fetch=FetchType.LAZY)
	AUser lastUpdateUser;
	
	@Column(name="last_update_time", nullable=false) 
	Timestamp lastUpdateTime;
	
	@Column(precision=16,scale=2)
	double orderno =1;
	
	/**
	 * 是否有效用于逻辑删除
	 */
	boolean valid = true;
	
	@Version
	int version;
	
	/**
	 * 创建带Id的引用对象
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends AEntity> T ref(Long id){
		this.setId(id);
		return (T)this;
	}
				
	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (this.getId() == null || obj == null || !(this.getClass().equals(obj.getClass()))) {
			return false;
		}

		AbstractEntity that = (AbstractEntity) obj;

		return this.getId().equals(that.getId());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getLastUpdateBy() {
		return lastUpdateBy;
	}

	public void setLastUpdateBy(String lastUpdateBy) {
		this.lastUpdateBy = lastUpdateBy;
	}

	public Timestamp getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Timestamp lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public double getOrderno() {
		return orderno;
	}

	public void setOrderno(double orderno) {
		this.orderno = orderno;
	}
	
	public AUser getCreateUser() {
		return createUser;
	}

	public void setCreateUser(AUser createUser) {
		this.createUser = createUser;
	}

	public AUser getLastUpdateUser() {
		return lastUpdateUser;
	}

	public void setLastUpdateUser(AUser lastUpdateUser) {
		this.lastUpdateUser = lastUpdateUser;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
}
