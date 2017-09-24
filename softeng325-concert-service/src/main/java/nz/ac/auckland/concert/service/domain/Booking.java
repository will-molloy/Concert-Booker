package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "BOOKINGS")
public class Booking {

    @Id
    @GeneratedValue
    @Column(nullable = false, unique = true)
    private long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false, unique = true)
    private Concert concert;

    private LocalDateTime date;

    @ElementCollection
    @CollectionTable(
            name = "BOOKING_SEATS",
            joinColumns = @JoinColumn(name = "booking_id", nullable = false)
    )
    private Set<Seat> seats;

    @Enumerated(EnumType.STRING)
    private PriceBand price;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false, unique = true)
    private User user;

    protected Booking (){}

    public Concert getConcert() {
        return concert;
    }

    public void setConcert(Concert concert) {
        this.concert = concert;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Set<Seat> getSeats() {
        return seats;
    }

    public void setSeats(Set<Seat> seats) {
        this.seats = seats;
    }

    public PriceBand getPrice() {
        return price;
    }

    public void setPrice(PriceBand price) {
        this.price = price;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
