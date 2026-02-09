package org.xyjh.xyjhstartweb.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * XYJH账号信息实体类
 */
public class AccountXYJH {
    private Long id;
    private String account;
    private String password;
    private String accountName;
    private LocalDateTime buyTime;
    private LocalDateTime updateTime;
    private Integer status;
    private LocalDateTime sellTime;
    private BigDecimal sellPrice;
    private BigDecimal buyPrice;
    private Integer greenTicket;
    private Integer yellowTicket;
    private String remark;
    private String strongCharacter;

    // 构造函数
    public AccountXYJH() {}

    public AccountXYJH(String account, String password, String accountName, LocalDateTime buyTime, 
                      BigDecimal buyPrice, Integer greenTicket, Integer yellowTicket) {
        this.account = account;
        this.password = password;
        this.accountName = accountName;
        this.buyTime = buyTime;
        this.buyPrice = buyPrice;
        this.greenTicket = greenTicket;
        this.yellowTicket = yellowTicket;
        this.status = 0; // 默认状态为刷票中
        this.updateTime = LocalDateTime.now();
    }

    // Getter & Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public LocalDateTime getBuyTime() {
        return buyTime;
    }

    public void setBuyTime(LocalDateTime buyTime) {
        this.buyTime = buyTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getSellTime() {
        return sellTime;
    }

    public void setSellTime(LocalDateTime sellTime) {
        this.sellTime = sellTime;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Integer getGreenTicket() {
        return greenTicket;
    }

    public void setGreenTicket(Integer greenTicket) {
        this.greenTicket = greenTicket;
    }

    public Integer getYellowTicket() {
        return yellowTicket;
    }

    public void setYellowTicket(Integer yellowTicket) {
        this.yellowTicket = yellowTicket;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStrongCharacter() {
        return strongCharacter;
    }

    public void setStrongCharacter(String strongCharacter) {
        this.strongCharacter = strongCharacter;
    }

    @Override
    public String toString() {
        return "AccountXYJH{" +
                "id=" + id +
                ", account='" + account + '\'' +
                ", accountName='" + accountName + '\'' +
                ", buyTime=" + buyTime +
                ", updateTime=" + updateTime +
                ", status=" + status +
                ", sellTime=" + sellTime +
                ", sellPrice=" + sellPrice +
                ", buyPrice=" + buyPrice +
                ", greenTicket=" + greenTicket +
                ", yellowTicket=" + yellowTicket +
                ", remark='" + remark + '\'' +
                ", strongCharacter='" + strongCharacter + '\'' +
                '}';
    }
}