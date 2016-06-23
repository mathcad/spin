package org.infrastructure.jpa.core;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

/**
 * 通用实体类型
 */
@MappedSuperclass
public abstract class AbstractEntity implements IEntity {
	private static final long serialVersionUID = 4497191615275262107L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(updatable = false)
	private String createBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(updatable = false)
	private AUser createUser;

	@Column(name = "create_time", nullable = false, updatable = false)
	private Timestamp createTime;

	private String lastUpdateBy;

	@ManyToOne(fetch = FetchType.LAZY)
	private AUser lastUpdateUser;

	@Column(name = "last_update_time", nullable = false)
	private Timestamp lastUpdateTime;

	@Column(precision = 16, scale = 2)
	private double orderno = 1;

	/**
	 * 是否有效用于逻辑删除
	 */
	private boolean valid = true;

	@Version
	private int version;

	/**
	 * 创建带Id的引用对象
	 * 
	 * @param id
	 * @return
	 */
	public static <T extends AbstractEntity> T newEntity(Class<T> clazz, Long id) {
		T entity;
		try {
			entity = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
		entity.setId(id);
		return entity;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (this.getId() == null || obj == null || !(this.getClass().equals(obj.getClass()))) {
			return false;
		}
		IEntity that = (IEntity) obj;
		return this.getId().equals(that.getId());
	}

	@Override
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
