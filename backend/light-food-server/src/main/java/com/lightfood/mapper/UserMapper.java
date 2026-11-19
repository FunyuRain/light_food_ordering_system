package com.lightfood.mapper;

import com.lightfood.pojo.entity.User;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface UserMapper {

    User selectByLogin(String login);

    int countByUsername(String username);

    int insert(User user);

    int updateLastLogin(@Param("id") Long id, @Param("time") LocalDateTime time);
}
