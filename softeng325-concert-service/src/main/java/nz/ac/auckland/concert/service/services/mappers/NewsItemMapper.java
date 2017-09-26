package nz.ac.auckland.concert.service.services.mappers;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;
import nz.ac.auckland.concert.service.domain.types.NewsItem;

public class NewsItemMapper {

    public static NewsItemDTO toDTO(NewsItem newsItem){
        return new NewsItemDTO(
                newsItem.getId(),
                newsItem.get_timestamp(),
                newsItem.get_content()
        );
    }
}
