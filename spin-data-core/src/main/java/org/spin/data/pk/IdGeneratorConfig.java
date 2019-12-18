package org.spin.data.pk;

import org.spin.data.pk.generator.provider.MachineIdProvider;
import org.spin.data.pk.meta.IdGenMethodE;
import org.spin.data.pk.meta.IdTypeE;

/**
 * 分布式ID生成器配置
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2017/5/5</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class IdGeneratorConfig {

    private Class<? extends MachineIdProvider> providerType;
    private String initParams;
    private IdGenMethodE genMethod = IdGenMethodE.EMBED;
    private IdTypeE idType = IdTypeE.MAX_PEAK;
    private long version = 0;

    public Class<? extends MachineIdProvider> getProviderType() {
        return providerType;
    }

    public void setProviderType(Class<? extends MachineIdProvider> providerType) {
        this.providerType = providerType;
    }

    public String getInitParams() {
        return initParams;
    }

    public void setInitParams(String initParams) {
        this.initParams = initParams;
    }

    public IdGenMethodE getGenMethod() {
        return genMethod;
    }

    public void setGenMethod(IdGenMethodE genMethod) {
        this.genMethod = genMethod;
    }

    public IdTypeE getIdType() {
        return idType;
    }

    public void setIdType(IdTypeE idType) {
        this.idType = idType;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
