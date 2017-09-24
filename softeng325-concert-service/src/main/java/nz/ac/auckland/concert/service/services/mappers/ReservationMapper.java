package nz.ac.auckland.concert.service.services.mappers;

import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.service.domain.Reservation;

import java.util.HashSet;
import java.util.Set;

public class ReservationMapper {

    public static ReservationDTO toDto(Reservation reservation) {
        Set<SeatDTO> seatDTOs = new HashSet<>();
        reservation.getSeats().forEach(s -> seatDTOs.add(SeatMapper.toDto(s)));

        return new ReservationDTO(
                reservation.getId(),
                new ReservationRequestDTO(
                        reservation.getSeats().size(),
                        reservation.getSeatType(),
                        reservation.getConcert().getId(),
                        reservation.getDate()),
                seatDTOs
        );
    }
}
