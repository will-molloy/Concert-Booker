package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;

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
}
