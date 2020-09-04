package org.jeecg.modules.quartz.entity;

public class TaskEntity {
    private double consumed;
    private String account;

    public double getConsumed() {
        return consumed;
    }

    public void setConsumed(double consumed) {
        this.consumed = consumed;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
