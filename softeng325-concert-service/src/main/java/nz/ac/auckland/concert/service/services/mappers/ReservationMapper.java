package nz.ac.auckland.concert.service.services.mappers;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.service.domain.Reservation;

public class ReservationMapper {

    public static ReservationDTO toReservationDTO(Reservation reservation) {
        return new ReservationDTO(
                reservation.getId(),
                new ReservationRequestDTO(
                        reservation.getSeats().size(),
                        reservation.getSeatType(),
                        reservation.getConcert().getId(),
                        reservation.getDate()),
                SeatMapper.toDTOSet(reservation.getSeats())
        );
    }

    public static BookingDTO toBookingDTO(Reservation reservation) {
        return new BookingDTO(
                reservation.getConcert().getId(),
                reservation.getConcert().getTitle(),
                reservation.getDate(),
                SeatMapper.toDTOSet(reservation.getSeats()),
                reservation.getSeatType()
        );
    }
}
