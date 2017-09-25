package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class CreditCard {

    @Id
    @GeneratedValue
    @Column(nullable = false, unique = true)
    private long id;

    @Enumerated
    private CreditCardDTO.Type type;

    @OneToOne(mappedBy = "creditCard", cascade = CascadeType.PERSIST,
            fetch = FetchType.LAZY, optional = false)
    private User user;

    private String number;

    private LocalDate expiryDate;

    protected CreditCard() {
    }

    public CreditCard(CreditCardDTO.Type type, User user, String number, LocalDate expiryDate) {
        this.type = type;
        this.user = user;
        this.number = number;
        this.expiryDate = expiryDate;

        user.setCreditCard(this);
    }

    public CreditCardDTO.Type getType() {
        return type;
    }

    public void setType(CreditCardDTO.Type type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CreditCard that = (CreditCard) o;

        if (id != that.id) return false;
        if (type != that.type) return false;
        if (number != null ? !number.equals(that.number) : that.number != null) return false;
        return expiryDate != null ? expiryDate.equals(that.expiryDate) : that.expiryDate == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (expiryDate != null ? expiryDate.hashCode() : 0);
        return result;
    }
}
