package org.xyjh.xyjhstartweb.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.xyjh.xyjhstartweb.entity.AdminUser;
import org.xyjh.xyjhstartweb.mapper.AdminUserMapper;

/**
 * 数据初始化器，在应用启动时检查并创建默认管理员账号。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AdminUserMapper adminUserMapper, PasswordEncoder passwordEncoder) {
        this.adminUserMapper = adminUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 检查 'admin' 用户是否已存在
        if (adminUserMapper.findByUsername("admin") == null) {
            AdminUser admin = new AdminUser();
            admin.setUsername("admin");
            // 【重要】请将 "your_strong_default_password" 替换为您自己的强密码
            admin.setPassword(passwordEncoder.encode("test1234"));
            admin.setRole("ADMIN");
            adminUserMapper.insert(admin);
            System.out.println(">>> 默认管理员账号 'admin' 已创建。请使用初始密码登录并尽快修改！<<<");
        }
    }
}