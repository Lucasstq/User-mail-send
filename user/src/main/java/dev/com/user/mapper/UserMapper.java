package dev.com.user.mapper;

import dev.com.user.dtos.request.UserRequest;
import dev.com.user.dtos.response.UserResponse;
import dev.com.user.entities.UserEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserMapper {

    public static UserEntity toEnity(UserRequest request) {
        return UserEntity
                .builder()
                .email(request.email())
                .name(request.name())
                .build();
    }

    public static UserResponse toResponse(UserEntity entity) {
        return UserResponse
                .builder()
                .userId(entity.getUserId())
                .email(entity.getEmail())
                .name(entity.getName())
                .build();
    }

}
