package org.xyjh.xyjhstartweb.dto;

import java.io.Serializable;
import java.util.Objects;

public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色编号 (主键), 例如: H001
     */
    private String id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 基础攻击力
     */
    private Integer baseAttack;

    /**
     * 基础防御力
     */
    private Integer baseDefense;

    /**
     * 基础生命力 (HP)
     */
    private Integer baseHp;

    /**
     * 基础速度
     */
    private Integer baseSpeed;

    /**
     * 调教速度
     */
    private Integer trainingSpeed;

    // 默认构造函数
    public Role() {
    }

    // 全参构造函数
    public Role(String id, String name, Integer baseAttack, Integer baseDefense, 
                Integer baseHp, Integer baseSpeed, Integer trainingSpeed) {
        this.id = id;
        this.name = name;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.baseHp = baseHp;
        this.baseSpeed = baseSpeed;
        this.trainingSpeed = trainingSpeed;
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

    public Integer getBaseAttack() {
        return baseAttack;
    }

    public void setBaseAttack(Integer baseAttack) {
        this.baseAttack = baseAttack;
    }

    public Integer getBaseDefense() {
        return baseDefense;
    }

    public void setBaseDefense(Integer baseDefense) {
        this.baseDefense = baseDefense;
    }

    public Integer getBaseHp() {
        return baseHp;
    }

    public void setBaseHp(Integer baseHp) {
        this.baseHp = baseHp;
    }

    public Integer getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(Integer baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    public Integer getTrainingSpeed() {
        return trainingSpeed;
    }

    public void setTrainingSpeed(Integer trainingSpeed) {
        this.trainingSpeed = trainingSpeed;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", baseAttack=" + baseAttack +
                ", baseDefense=" + baseDefense +
                ", baseHp=" + baseHp +
                ", baseSpeed=" + baseSpeed +
                ", trainingSpeed=" + trainingSpeed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}