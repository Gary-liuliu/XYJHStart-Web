package org.xyjh.xyjhstartweb.dto;

import java.io.Serializable;
import java.util.Objects;

public class Artifact implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 遗物编号 (主键), 例如: A0001, Carnival006
     */
    private String id;

    /**
     * 遗物名称
     */
    private String name;

    // 默认构造函数
    public Artifact() {
    }

    // 全参构造函数
    public Artifact(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact artifact = (Artifact) o;
        return Objects.equals(id, artifact.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}