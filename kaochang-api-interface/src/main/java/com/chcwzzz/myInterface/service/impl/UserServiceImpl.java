package com.chcwzzz.myInterface.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chcwzzz.myInterface.domain.User;
import com.chcwzzz.myInterface.service.UserService;
import com.chcwzzz.myInterface.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author Chcwzzz
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-02-05 17:28:49
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

}




