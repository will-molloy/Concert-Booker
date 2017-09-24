package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.types.PriceBand;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
public class Reservation {

    @Id
    @GeneratedValue
    @Column(nullable = false, unique = true)
    private long id;

    @ElementCollection
    @CollectionTable(
            name = "RESERVATION_SEATS",
            joinColumns = @JoinColumn(name = "reservation_id", nullable = false)
    )
    private Set<Seat> seats;

    @Enumerated(EnumType.STRING)
    private PriceBand seatType;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false, unique = true)
    private Concert concert;

    private LocalDateTime date;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false, unique = true)
    private User user;

    private boolean confirmed = false;

    public Reservation(Concert concert, LocalDateTime date, User user, Set<Seat> seats){
        this.concert = concert;
        this.date = date;
        this.user = user;
        this.seats = seats;
    }

    protected Reservation() {
    }

    public Set<Seat> getSeats() {
        return seats;
    }

    public void setSeats(Set<Seat> seats) {
        this.seats = seats;
    }

    public PriceBand getSeatType() {
        return seatType;
    }

    public void setSeatType(PriceBand seatType) {
        this.seatType = seatType;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Concert getConcert() {
        return concert;
    }

    public void setConcert(Concert concert) {
        this.concert = concert;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Reservation))
            return false;
        if (obj == this)
            return true;

        Reservation rhs = (Reservation) obj;
        return new EqualsBuilder().
                append(concert, rhs.concert).
                append(date, rhs.date).
                append(user, rhs.user).
                append(confirmed, rhs.confirmed).
                append(seats, rhs.seats).
                append(seatType, rhs.seatType).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(concert).
                append(date).
                append(user).
                append(confirmed).
                append(seats).
                append(seatType).
                hashCode();
    }
}
