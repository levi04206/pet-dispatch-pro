package com.wenxu.converter;

import com.wenxu.entity.User;
import com.wenxu.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConverter {

    UserVO toVO(User user);
}
