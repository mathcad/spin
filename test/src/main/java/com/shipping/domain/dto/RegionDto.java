package com.shipping.domain.dto;

import com.shipping.domain.sys.Region;

import java.util.List;

/**
 * <p>Created by xuweinan on 2017/9/13.</p>
 *
 * @author xuweinan
 */
public class RegionDto {
    private String label;
    private String value;
    private List<RegionDto> children;
    private transient String parent;
    private transient Integer level;

    public RegionDto(Region region) {
        this.label = region.getName();
        this.value = region.getCode();
        this.level = region.getLevel().getValue();
        this.parent = region.getParentCode();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<RegionDto> getChildren() {
        return children;
    }

    public void setChildren(List<RegionDto> children) {
        this.children = children;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}
