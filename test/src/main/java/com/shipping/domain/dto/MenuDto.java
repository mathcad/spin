package com.shipping.domain.dto;

import com.shipping.domain.enums.FunctionTypeE;
import com.shipping.domain.sys.Function;
import org.spin.data.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>Created by xuweinan on 2017/9/3.</p>
 *
 * @author xuweinan
 */
public class MenuDto {

    private Long id;

    private String name;

    private FunctionTypeE type;

    private String code;

    private String icon;

    private String link;

    private Long parent;

    private String idPath;

    private float orderNo;

    private boolean isLeaf;

    private List<MenuDto> children = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FunctionTypeE getType() {
        return type;
    }

    public void setType(FunctionTypeE type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public String getIdPath() {
        return idPath;
    }

    public void setIdPath(String idPath) {
        this.idPath = idPath;
    }

    public float getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(float orderNo) {
        this.orderNo = orderNo;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public List<MenuDto> getChildren() {
        return children;
    }

    public void setChildren(List<MenuDto> children) {
        this.children = children;
    }

    public static MenuDto toDto(Function function) {
        MenuDto dto = new MenuDto();
        EntityUtils.copyTo(function, dto, "id", "name", "type", "code", "icon", "link", "idPath", "orderNo", "isLeaf");
        dto.setParent(Objects.isNull(function.getParent()) ? null : function.getParent().getId());
        return dto;
    }
}
