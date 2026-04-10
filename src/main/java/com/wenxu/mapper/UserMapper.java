package com.wenxu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wenxu.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表的数据访问层
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 兄弟，这就完事了！
    // 只要继承了 BaseMapper<User>，MyBatis-Plus 已经在底层悄悄帮你写好了
    // insert、delete、update、selectById 等几十个方法！一行 SQL 都不用写！
}