package nz.ac.auckland.concert.service.services.mappers;

import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.types.User;

public class UserMapper {
    public static User toDomain(UserDTO userDTO, String uuid) {
        return new User(
                userDTO.getUsername(),
                userDTO.getPassword(),
                userDTO.getLastname(),
                userDTO.getFirstname(),
                uuid
        );
    }

    public static UserDTO toDTO(User user) {
        return new UserDTO(
                user.getUsername(),
                user.getPassword(),
                user.getLastname(),
                user.getFirstname()
        );
    }
}
