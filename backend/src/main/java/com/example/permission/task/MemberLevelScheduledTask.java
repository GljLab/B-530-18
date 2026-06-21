package com.example.permission.task;

import com.example.permission.service.MemberLevelUpgradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MemberLevelScheduledTask {

    @Autowired
    private MemberLevelUpgradeService memberLevelUpgradeService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void executeUpgradeCheck() {
        try {
            memberLevelUpgradeService.executeUpgradeCheck(null, null, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 1 1 1 ?")
    public void executeYearlyDowngradeAndReset() {
        try {
            memberLevelUpgradeService.executeDowngradeCheck(null, null, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            memberLevelUpgradeService.executeYearlyReset(null, null, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
