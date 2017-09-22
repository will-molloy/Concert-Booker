package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Performer;

import java.util.stream.Collectors;


public class ConcertMapper {

    static Concert toDomainModel(ConcertDTO concertDTO) {
        return new Concert(
                concertDTO.getTitle()
        );
    }

    static ConcertDTO toDto(Concert concert) {

        return new ConcertDTO(
                concert.getId(),
                concert.getTitle(),
                concert.getDates(),
                concert.getTariff(),
                concert.getPerformers().stream().map(Performer::getId).collect(Collectors.toSet())
        );
    }


}
