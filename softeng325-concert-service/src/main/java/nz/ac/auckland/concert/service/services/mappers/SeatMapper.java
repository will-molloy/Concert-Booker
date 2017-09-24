package nz.ac.auckland.concert.service.services.mappers;

import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.service.domain.Seat;

public class SeatMapper {

    public static SeatDTO toDto(Seat seat) {
        return new SeatDTO(
                seat.getRow(),
                seat.getNumber()
        );
    }
}
