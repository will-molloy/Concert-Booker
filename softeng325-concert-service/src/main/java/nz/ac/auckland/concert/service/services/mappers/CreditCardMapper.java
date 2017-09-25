package nz.ac.auckland.concert.service.services.mappers;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.service.domain.CreditCard;
import nz.ac.auckland.concert.service.domain.User;

public class CreditCardMapper {

    public static CreditCard toDomain(CreditCardDTO creditCardDTO, User user) {
        return new CreditCard(creditCardDTO.getType(),
                user,
                creditCardDTO.getNumber(),
                creditCardDTO.getExpiryDate()
        );
    }
}
