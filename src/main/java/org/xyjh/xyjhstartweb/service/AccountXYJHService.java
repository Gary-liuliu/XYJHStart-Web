package org.xyjh.xyjhstartweb.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xyjh.xyjhstartweb.dto.AccountXYJHAccountingSummary;
import org.xyjh.xyjhstartweb.dto.CreateAccountXYJHRequest;
import org.xyjh.xyjhstartweb.dto.PagedResult;
import org.xyjh.xyjhstartweb.dto.QueryAccountXYJHRequest;
import org.xyjh.xyjhstartweb.dto.SyncAccountXYJHRequest;
import org.xyjh.xyjhstartweb.dto.UpdateAccountXYJHRequest;
import org.xyjh.xyjhstartweb.entity.AccountXYJH;
import org.xyjh.xyjhstartweb.mapper.AccountXYJHMapper;
import org.xyjh.xyjhstartweb.util.Result;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class AccountXYJHService {

    @Autowired
    private AccountXYJHMapper accountXYJHMapper;

    /**
     * 创建新账号
     */
    public Result<AccountXYJH> createAccount(CreateAccountXYJHRequest request) {
        try {
            // 转换DTO到Entity
            AccountXYJH account = new AccountXYJH();
            BeanUtils.copyProperties(request, account);
            account.setUpdateTime(LocalDateTime.now());
            
            // 设置默认值
            if (account.getGreenTicket() == null) {
                account.setGreenTicket(0);
            }
            if (account.getYellowTicket() == null) {
                account.setYellowTicket(0);
            }
            
            int result = accountXYJHMapper.insertAccount(account);
            if (result > 0) {
                return Result.success("账号创建成功", account);
            } else {
                return Result.fail("账号创建失败");
            }
        } catch (Exception e) {
            return Result.fail("创建账号时发生错误: " + e.getMessage());
        }
    }

    public Result<AccountXYJH> syncRefreshResult(SyncAccountXYJHRequest request) {
        try {
            if (isBlank(request.getAccountName()) && isBlank(request.getAccount())) {
                return Result.fail("账号名或账号至少填写一项");
            }

            AccountXYJH existingAccount = null;
            if (!isBlank(request.getAccount())) {
                existingAccount = accountXYJHMapper.selectAccountByAccount(request.getAccount().trim());
            }
            if (existingAccount == null && !isBlank(request.getAccountName())) {
                existingAccount = accountXYJHMapper.selectAccountByAccountName(request.getAccountName().trim());
            }

            if (existingAccount == null) {
                AccountXYJH account = new AccountXYJH();
                account.setAccount(defaultAccountName(request));
                account.setPassword(trimToNull(request.getPassword()));
                account.setAccountName(defaultAccountName(request));
                account.setGreenTicket(defaultTicket(request.getGreenTicket()));
                account.setYellowTicket(defaultTicket(request.getYellowTicket()));
                account.setStatus(0);
                account.setBuyTime(LocalDateTime.now());
                account.setUpdateTime(LocalDateTime.now());
                account.setRemark(trimToNull(request.getRemark()));

                int result = accountXYJHMapper.insertAccount(account);
                return result > 0 ? Result.success("同步创建成功", account) : Result.fail("同步创建失败");
            }

            AccountXYJH accountToUpdate = new AccountXYJH();
            accountToUpdate.setId(existingAccount.getId());
            accountToUpdate.setAccount(trimToNull(request.getAccount()));
            accountToUpdate.setPassword(trimToNull(request.getPassword()));
            accountToUpdate.setAccountName(trimToNull(request.getAccountName()));
            accountToUpdate.setGreenTicket(request.getGreenTicket());
            accountToUpdate.setYellowTicket(request.getYellowTicket());
            accountToUpdate.setRemark(trimToNull(request.getRemark()));
            accountToUpdate.setUpdateTime(LocalDateTime.now());

            int result = accountXYJHMapper.updateAccountById(accountToUpdate);
            if (result <= 0) {
                return Result.fail("同步更新失败");
            }

            AccountXYJH updatedAccount = accountXYJHMapper.selectAccountById(existingAccount.getId());
            return Result.success("同步更新成功", updatedAccount);
        } catch (Exception e) {
            return Result.fail("同步账号时发生错误: " + e.getMessage());
        }
    }

    public Result<AccountXYJHAccountingSummary> getAccountingSummary() {
        try {
            List<AccountXYJH> accounts = accountXYJHMapper.selectAccountedAccounts();
            BigDecimal huoxinggeTotal = BigDecimal.ZERO;
            BigDecimal kakaTotal = BigDecimal.ZERO;

            for (AccountXYJH account : accounts) {
                BigDecimal sellPrice = account.getSellPrice();
                BigDecimal buyPrice = account.getBuyPrice();
                if (sellPrice == null || buyPrice == null || sellPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                BigDecimal adjustedSellPrice = sellPrice.subtract(sellPrice.multiply(new BigDecimal("0.006")));
                BigDecimal kakaShare = adjustedSellPrice.subtract(buyPrice).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
                kakaTotal = kakaTotal.add(kakaShare);
                huoxinggeTotal = huoxinggeTotal.add(kakaShare);
            }

            AccountXYJHAccountingSummary summary = new AccountXYJHAccountingSummary();
            summary.setHuoxinggeTotal(huoxinggeTotal.setScale(2, RoundingMode.HALF_UP));
            summary.setKakaTotal(kakaTotal.setScale(2, RoundingMode.HALF_UP));
            summary.setAccountedCount(accounts.size());
            return Result.success(summary);
        } catch (Exception e) {
            return Result.fail("查询记账统计时发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新账号信息
     */
    public Result<AccountXYJH> updateAccount(UpdateAccountXYJHRequest request) {
        try {
            // 先检查账号是否存在
            AccountXYJH existingAccount = accountXYJHMapper.selectAccountById(request.getId());
            if (existingAccount == null) {
                return Result.fail("账号不存在");
            }

            // 更新字段
            AccountXYJH accountToUpdate = new AccountXYJH();
            BeanUtils.copyProperties(request, accountToUpdate);
            accountToUpdate.setUpdateTime(LocalDateTime.now());
            
            int result = accountXYJHMapper.updateAccountById(accountToUpdate);
            if (result > 0) {
                AccountXYJH updatedAccount = accountXYJHMapper.selectAccountById(request.getId());
                return Result.success("账号更新成功", updatedAccount);
            } else {
                return Result.fail("账号更新失败");
            }
        } catch (Exception e) {
            return Result.fail("更新账号时发生错误: " + e.getMessage());
        }
    }

    /**
     * 删除账号
     */
    public Result<String> deleteAccount(Long id) {
        try {
            AccountXYJH existingAccount = accountXYJHMapper.selectAccountById(id);
            if (existingAccount == null) {
                return Result.fail("账号不存在");
            }

            int result = accountXYJHMapper.deleteAccountById(id);
            if (result > 0) {
                return Result.success("账号删除成功", "删除成功");
            } else {
                return Result.fail("账号删除失败");
            }
        } catch (Exception e) {
            return Result.fail("删除账号时发生错误: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询账号
     */
    public Result<AccountXYJH> getAccountById(Long id) {
        try {
            AccountXYJH account = accountXYJHMapper.selectAccountById(id);
            if (account != null) {
                return Result.success(account);
            } else {
                return Result.fail("账号不存在");
            }
        } catch (Exception e) {
            return Result.fail("查询账号时发生错误: " + e.getMessage());
        }
    }

    /**
     * 分页查询账号列表
     */
    public Result<PagedResult<AccountXYJH>> getAccountsByPage(QueryAccountXYJHRequest request) {
        try {
            // 处理分页参数
            if (request.getPage() == null || request.getPage() < 0) {
                request.setPage(0);
            }
            if (request.getSize() == null || request.getSize() <= 0) {
                request.setSize(10);
            }
            
            // 计算偏移量
            int offset = request.getPage() * request.getSize();
            request.setPage(offset);

            // 查询数据
            List<AccountXYJH> accounts = accountXYJHMapper.selectAccountsByCondition(request);
            
            // 查询总数
            int totalCount = accountXYJHMapper.countAccountsByCondition(request);
            
            // 封装分页结果
            PagedResult<AccountXYJH> pagedResult = new PagedResult<>();
            pagedResult.setContent(accounts);
            pagedResult.setTotalElements(totalCount);
            pagedResult.setPageNumber(request.getPage() / request.getSize());
            pagedResult.setPageSize(request.getSize());
            pagedResult.setTotalPages((int) Math.ceil((double) totalCount / request.getSize()));

            return Result.success(pagedResult);
        } catch (Exception e) {
            return Result.fail("查询账号列表时发生错误: " + e.getMessage());
        }
    }

    /**
     * 根据状态查询账号列表
     */
    public Result<List<AccountXYJH>> getAccountsByStatus(Integer status) {
        try {
            List<AccountXYJH> accounts = accountXYJHMapper.selectAccountsByStatus(status);
            return Result.success(accounts);
        } catch (Exception e) {
            return Result.fail("根据状态查询账号时发生错误: " + e.getMessage());
        }
    }

    /**
     * 根据关键字查询账号
     */
    public Result<List<AccountXYJH>> searchAccountsByKeyword(String keyword) {
        try {
            List<AccountXYJH> accounts = accountXYJHMapper.selectAccountsByKeyword(keyword);
            return Result.success(accounts);
        } catch (Exception e) {
            return Result.fail("搜索账号时发生错误: " + e.getMessage());
        }
    }

    private String defaultAccountName(SyncAccountXYJHRequest request) {
        String accountName = trimToNull(request.getAccountName());
        if (accountName != null) {
            return accountName;
        }
        return trimToNull(request.getAccount());
    }

    private Integer defaultTicket(Integer ticket) {
        return ticket == null ? 0 : ticket;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
