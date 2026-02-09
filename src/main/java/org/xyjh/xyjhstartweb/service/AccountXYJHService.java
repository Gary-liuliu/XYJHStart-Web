package org.xyjh.xyjhstartweb.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xyjh.xyjhstartweb.dto.CreateAccountXYJHRequest;
import org.xyjh.xyjhstartweb.dto.PagedResult;
import org.xyjh.xyjhstartweb.dto.QueryAccountXYJHRequest;
import org.xyjh.xyjhstartweb.dto.UpdateAccountXYJHRequest;
import org.xyjh.xyjhstartweb.entity.AccountXYJH;
import org.xyjh.xyjhstartweb.mapper.AccountXYJHMapper;
import org.xyjh.xyjhstartweb.util.Result;

import java.time.LocalDateTime;
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
}