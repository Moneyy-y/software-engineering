package com.catering.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.entity.User;
import com.catering.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminDataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AdminDataInitializer(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void run(String... args) {
        User admin = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("admin123"));
            admin.setNickname("系统管理员");
            admin.setRole("admin");
            admin.setStatus(1);
            userMapper.insert(admin);
        } else if (admin.getPassword() == null
                || !encoder.matches("admin123", admin.getPassword())) {
            // seed.sql 中旧哈希可能与 admin123 不一致，启动时自动纠正
            admin.setPassword(encoder.encode("admin123"));
            userMapper.updateById(admin);
        }
    }
}
