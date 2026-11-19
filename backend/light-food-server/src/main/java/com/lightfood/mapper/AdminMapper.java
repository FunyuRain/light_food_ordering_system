package com.lightfood.mapper;

import com.lightfood.pojo.entity.Admin;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface AdminMapper {

    Admin selectByUsername(String username);

    int countByUsername(String username);

    int insert(Admin admin);

    int updateLastLogin(@Param("id") Long id, @Param("time") LocalDateTime time);
}
