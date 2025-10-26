package org.xyjh.xyjhstartweb.service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.xyjh.xyjhstartweb.entity.AccountInfo;
import org.xyjh.xyjhstartweb.entity.AccountLoginResult;
import org.xyjh.xyjhstartweb.mapper.AccountInfoMapper;
import org.xyjh.xyjhstartweb.util.login.LoginUtil;
import org.xyjh.xyjhstartweb.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@Slf4j
public class AccountInfoService {

    @Autowired
    private AccountInfoMapper accountInfoMapper;
    private static final Logger log = LoggerFactory.getLogger(AccountInfoService.class);

    /**
     * 添加账号并验证
     * @param accountInfo 账号信息
     * @return Result 封装登录验证结果和提示信息
     */
    public Result<AccountLoginResult> addAccount(AccountInfo accountInfo) {

        // 使用 log 对象记录日志
        log.info("开始处理添加账号请求，账号为: {}", accountInfo.getAccount());
        try {
            // 调用原有登录验证逻辑（无需代理）
            String[] loginResult = LoginUtil.interactiveLogin(true, 7890,
                    accountInfo.getAccount(), accountInfo.getPassword());

            // 验证账号是否有效
            if (loginResult[0] == null || loginResult[0].isEmpty()) {
                return Result.fail("验证失败：登录返回信息为空");
            }
            log.info("外部验证成功，账号: {}", accountInfo.getAccount());
            // 保存到数据库
            log.debug("准备将账号信息插入数据库...");
            accountInfoMapper.insertAccount(accountInfo);
            log.info("账号信息已成功保存到数据库，用户ID: {}", accountInfo.getId());
            // 封装 token 和 aid
            AccountLoginResult loginData = new AccountLoginResult(loginResult[0], loginResult[1]);

            // 返回成功 Result
            return Result.success("验证成功，账号已保存", loginData);
        } catch (Exception e) {
            log.error("添加账号时发生未知异常，账号: {}", accountInfo.getAccount(), e);
            return Result.fail("验证失败：" + e.getMessage());
        }
    }

}