package nz.ac.auckland.concert.common.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO class to represent reservations.
 * <p>
 * A ReservationDTO describes a reservation in terms of:
 * _id                 the unique identifier for a reservation.
 * _reservationRequest details of the corresponding reservation request,
 * including the number of seats and their type, concert
 * identity, and the date/time of the concert for which a
 * reservation was requested.
 * _seats              the seats that have been reserved (represented as a Set
 * of SeatDTO objects).
 */
@XmlRootElement(name = "reservation")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReservationDTO {

    @XmlAttribute(name = "id")
    private Long _id;

    @XmlElement(name = "request")
    private ReservationRequestDTO _request;

    @XmlElement(name = "seats")
    private Set<SeatDTO> _seats;

    public ReservationDTO() {
    }

    public ReservationDTO(Long id, ReservationRequestDTO request, Set<SeatDTO> seats) {
        _id = id;
        _request = request;
        _seats = new HashSet<>(seats);
    }

    public Long getId() {
        return _id;
    }

    public ReservationRequestDTO getReservationRequest() {
        return _request;
    }

    public Set<SeatDTO> getSeats() {
        return Collections.unmodifiableSet(_seats);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ReservationDTO))
            return false;
        if (obj == this)
            return true;

        ReservationDTO rhs = (ReservationDTO) obj;
        return new EqualsBuilder().
                append(_request, rhs._request).
                append(_seats, rhs._seats).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(_request).
                append(_seats).
                hashCode();
    }
}
