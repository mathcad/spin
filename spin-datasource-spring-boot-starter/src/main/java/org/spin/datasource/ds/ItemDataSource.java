package org.spin.datasource.ds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.datasource.enums.SeataMode;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class ItemDataSource extends AbstractDataSource implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ItemDataSource.class);

    private String name;

    private DataSource realDataSource;

    private DataSource dataSource;

    private Boolean p6spy;

    private Boolean seata;

    private SeataMode seataMode;

    public ItemDataSource() {
    }

    public ItemDataSource(String name, DataSource realDataSource, DataSource dataSource, Boolean p6spy, Boolean seata, SeataMode seataMode) {
        this.name = name;
        this.realDataSource = realDataSource;
        this.dataSource = dataSource;
        this.p6spy = p6spy;
        this.seata = seata;
        this.seataMode = seataMode;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return dataSource.getConnection(username, password);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return super.isWrapperFor(iface) || iface.isInstance(realDataSource) || iface.isInstance(dataSource);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        if (iface.isInstance(realDataSource)) {
            return (T) realDataSource;
        }
        if (iface.isInstance(dataSource)) {
            return (T) dataSource;
        }
        return null;
    }

    @Override
    public void close() {
        Class<? extends DataSource> clazz = realDataSource.getClass();
        try {
            Method closeMethod = clazz.getDeclaredMethod("close");
            closeMethod.invoke(realDataSource);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.warn("dynamic-datasource close the datasource named [{}] failed,", name, e);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataSource getRealDataSource() {
        return realDataSource;
    }

    public void setRealDataSource(DataSource realDataSource) {
        this.realDataSource = realDataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Boolean getP6spy() {
        return p6spy;
    }

    public void setP6spy(Boolean p6spy) {
        this.p6spy = p6spy;
    }

    public Boolean getSeata() {
        return seata;
    }

    public void setSeata(Boolean seata) {
        this.seata = seata;
    }

    public SeataMode getSeataMode() {
        return seataMode;
    }

    public void setSeataMode(SeataMode seataMode) {
        this.seataMode = seataMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemDataSource)) return false;
        ItemDataSource that = (ItemDataSource) o;
        return Objects.equals(name, that.name) && Objects.equals(realDataSource, that.realDataSource) && Objects.equals(dataSource, that.dataSource) && Objects.equals(p6spy, that.p6spy) && Objects.equals(seata, that.seata) && Objects.equals(seataMode, that.seataMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, realDataSource, dataSource, p6spy, seata, seataMode);
    }

    @Override
    public String toString() {
        return "ItemDataSource{" +
            "name='" + name + '\'' +
            ", realDataSource=" + realDataSource +
            ", dataSource=" + dataSource +
            ", p6spy=" + p6spy +
            ", seata=" + seata +
            ", seataMode=" + seataMode +
            '}';
    }
}
