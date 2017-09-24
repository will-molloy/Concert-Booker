package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class Seat {

    @Enumerated(EnumType.STRING)
    private SeatRow row;

    private SeatNumber number;

    public Seat(SeatRow row, SeatNumber number) {
        this.row = row;
        this.number = number;
    }

    protected Seat() {
    }

    public SeatRow getRow() {
        return row;
    }

    public void setRow(SeatRow row) {
        this.row = row;
    }

    public SeatNumber getNumber() {
        return number;
    }

    public void setNumber(SeatNumber number) {
        this.number = number;
    }
}
