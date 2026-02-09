package org.xyjh.xyjhstartweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xyjh.xyjhstartweb.dto.CreateAccountXYJHRequest;
import org.xyjh.xyjhstartweb.dto.QueryAccountXYJHRequest;
import org.xyjh.xyjhstartweb.dto.UpdateAccountXYJHRequest;
import org.xyjh.xyjhstartweb.entity.AccountXYJH;
import org.xyjh.xyjhstartweb.service.AccountXYJHService;
import org.xyjh.xyjhstartweb.util.Result;

@RestController
@RequestMapping("/api/account-xyjh")
public class AccountXYJHController {

    @Autowired
    private AccountXYJHService accountXYJHService;

    /**
     * 创建新账号
     */
    @PostMapping("/create")
    public Result<AccountXYJH> createAccount(@RequestBody CreateAccountXYJHRequest request) {
        return accountXYJHService.createAccount(request);
    }

    /**
     * 更新账号信息
     */
    @PutMapping("/update")
    public Result<AccountXYJH> updateAccount(@RequestBody UpdateAccountXYJHRequest request) {
        return accountXYJHService.updateAccount(request);
    }

    /**
     * 删除账号
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteAccount(@PathVariable Long id) {
        return accountXYJHService.deleteAccount(id);
    }

    /**
     * 根据ID获取账号详情
     */
    @GetMapping("/{id}")
    public Result<AccountXYJH> getAccountById(@PathVariable Long id) {
        return accountXYJHService.getAccountById(id);
    }

    /**
     * 分页查询账号列表
     */
    @PostMapping("/list")
    public Result<?> getAccountsByPage(@RequestBody QueryAccountXYJHRequest request) {
        return accountXYJHService.getAccountsByPage(request);
    }

    /**
     * 根据状态查询账号列表
     */
    @GetMapping("/status/{status}")
    public Result<?> getAccountsByStatus(@PathVariable Integer status) {
        return accountXYJHService.getAccountsByStatus(status);
    }

    /**
     * 关键字搜索账号
     */
    @GetMapping("/search")
    public Result<?> searchAccounts(@RequestParam String keyword) {
        return accountXYJHService.searchAccountsByKeyword(keyword);
    }
}