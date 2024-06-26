package nz.ac.auckland.concert.common.dto;

import nz.ac.auckland.concert.common.types.PriceBand;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO class to represent bookings (confirmed reservations).
 * <p>
 * A BookingDTO describes a booking in terms of:
 * _concertId      the unique identifier for a concert.
 * _concertTitle   the concert's title.
 * _dateTime       the concert's scheduled date and time for which the booking
 * applies.
 * _seats          the seats that have been booked (represented as a  Set of
 * SeatDTO objects).
 * _priceBand      the price band of the booked seats (all seats are within the
 * same price band).
 */
@XmlRootElement(name = "booking")
@XmlAccessorType(XmlAccessType.FIELD)
public class BookingDTO {

    @XmlElement(name = "concert-id")
    private Long _concertId;

    @XmlElement(name = "concert-title")
    private String _concertTitle;

    @XmlElement(name = "date-time")
    private LocalDateTime _dateTime;

    @XmlElement(name = "seats")
    private Set<SeatDTO> _seats;

    @XmlElement(name = "price-band")
    private PriceBand _priceBand;

    public BookingDTO() {
    }

    public BookingDTO(Long concertId, String concertTitle,
                      LocalDateTime dateTime, Set<SeatDTO> seats, PriceBand priceBand) {
        _concertId = concertId;
        _concertTitle = concertTitle;
        _dateTime = dateTime;

        _seats = new HashSet<>();
        _seats.addAll(seats);

        _priceBand = priceBand;
    }

    public Long getConcertId() {
        return _concertId;
    }

    public String getConcertTitle() {
        return _concertTitle;
    }

    public LocalDateTime getDateTime() {
        return _dateTime;
    }

    public Set<SeatDTO> getSeats() {
        return Collections.unmodifiableSet(_seats);
    }

    public PriceBand getPriceBand() {
        return _priceBand;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BookingDTO))
            return false;
        if (obj == this)
            return true;

        BookingDTO rhs = (BookingDTO) obj;
        return new EqualsBuilder().append(_concertId, rhs._concertId)
                .append(_concertTitle, rhs._concertTitle)
                .append(_dateTime, rhs._dateTime)
                .append(_seats, rhs._seats)
                .append(_priceBand, rhs._priceBand).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(_concertId)
                .append(_concertTitle).append(_dateTime).append(_seats)
                .append(_priceBand).hashCode();
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("concert: ");
        buffer.append(_concertTitle);
        buffer.append(", date/time ");
        buffer.append(_seats.size());
        buffer.append(" ");
        buffer.append(_priceBand);
        buffer.append(" seats.");
        return buffer.toString();
    }
}
