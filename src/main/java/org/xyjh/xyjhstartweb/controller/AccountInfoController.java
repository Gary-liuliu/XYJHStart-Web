package org.xyjh.xyjhstartweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xyjh.xyjhstartweb.entity.AccountInfo;
import org.xyjh.xyjhstartweb.service.AccountInfoService;
import org.xyjh.xyjhstartweb.util.Result;

@RestController
@RequestMapping("/account")
public class AccountInfoController {

    @Autowired
    private AccountInfoService accountInfoService;

    @PostMapping("/add")
    public Result createAccount(@RequestBody AccountInfo accountInfo) {
        return accountInfoService.addAccount(accountInfo);

    }
}