package com.example.Kcsj.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Kcsj.common.Result;
import com.example.Kcsj.entity.User;
import com.example.Kcsj.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search) {
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery();
        wrapper.orderByDesc(User::getId);
        if (StrUtil.isNotBlank(search)) {
            wrapper.like(User::getUsername, search);
        }
        Page<User> userPage = userMapper.selectPage(new Page<User>(pageNum, pageSize), wrapper);
        return Result.success(userPage);
    }

    @GetMapping("/{username}")
    public Result<?> getByUsername(@PathVariable String username) {
        return Result.success(userMapper.selectByUsername(username));
    }

    @GetMapping("/all")
    public Result<?> getAll() {
        return Result.success(userMapper.selectList(null));
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody User userParam) {
        try {
            User user = userMapper.selectByUsername(userParam.getUsername());
            if (user == null) {
                return Result.error("-1", "用户名不存在");
            }

            if (!passwordMatches(userParam.getPassword(), user.getPassword())) {
                return Result.error("-1", "密码错误");
            }

            // 旧明文密码用户首次登录后自动升级为 BCrypt
            if (!isBcrypt(user.getPassword()) && Objects.equals(userParam.getPassword(), user.getPassword())) {
                user.setPassword(passwordEncoder.encode(userParam.getPassword()));
                userMapper.updateById(user);
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("-1", "登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        User exists = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, user.getUsername()));
        if (exists != null) {
            return Result.error("-1", "用户名重复");
        }

        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setName("张三");
        newUser.setSex("男");
        newUser.setRole("common");
        newUser.setEmail("123@qq.com");
        newUser.setTime(new Date());
        newUser.setTel("1234567889");
        newUser.setAvatar("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        userMapper.insert(newUser);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<?> updates(@RequestBody User user) {
        if (StrUtil.isNotBlank(user.getPassword()) && !isBcrypt(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.updateById(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable int id) {
        userMapper.deleteById(id);
        return Result.success();
    }

    @PostMapping
    public Result<?> save(@RequestBody User user) {
        if (StrUtil.isNotBlank(user.getPassword()) && !isBcrypt(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.insert(user);
        return Result.success();
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword == null || rawPassword == null) {
            return false;
        }
        if (isBcrypt(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return Objects.equals(rawPassword, storedPassword);
    }

    private boolean isBcrypt(String value) {
        return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
