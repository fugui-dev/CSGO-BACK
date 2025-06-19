package com.ruoyi.domain.common.constant.LockKey;

public enum UpgradeLock {

    UPGRADE_LOCK("upgrade_lock:");

    private String lock;

    UpgradeLock(String lock){
        this.lock = lock;
    }

    public String getLock(){
        return this.lock;
    }

}
