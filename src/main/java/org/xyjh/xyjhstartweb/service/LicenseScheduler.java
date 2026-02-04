package org.xyjh.xyjhstartweb.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xyjh.xyjhstartweb.dto.PagedResult;
import org.xyjh.xyjhstartweb.entity.LicenseKey;
import org.xyjh.xyjhstartweb.mapper.LicenseKeyMapper;
import org.xyjh.xyjhstartweb.util.Result;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LicenseScheduler {

    private final LicenseKeyMapper licenseKeyMapper;

    private static final Logger log = LoggerFactory.getLogger(AccountInfoService.class);

    public LicenseScheduler(LicenseKeyMapper licenseKeyMapper) {
        this.licenseKeyMapper = licenseKeyMapper;
    }

    /**
     * 每天凌晨 00:01 执行
     * cron表达式: 秒 分 时 日 月 周
     * "0 1 0 * * ?" 表示每天的 0点1分0秒 执行
     */
    @Scheduled(cron = "0 1 0 * * ?")
    public void updateExpiredLicenses() {
        log.info("【定时任务】开始扫描已过期的许可证...");

        List<LicenseKey> expiredKeys = licenseKeyMapper.findExpiredButActiveKeys();

        if (expiredKeys.isEmpty()) {
            log.info("【定时任务】未发现已过期的许可证。");
            return;
        }

        List<Long> idsToExpire = expiredKeys.stream()
                .map(LicenseKey::getId)
                .collect(Collectors.toList());

        int updatedCount = licenseKeyMapper.batchUpdateStatus(idsToExpire, "EXPIRED");

        log.info("【定时任务】扫描完成。共发现 {} 个已过期的许可证，已成功更新 {} 个。", expiredKeys.size(), updatedCount);
    }

}