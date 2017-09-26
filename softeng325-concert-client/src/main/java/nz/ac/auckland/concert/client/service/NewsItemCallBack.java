package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;

public class NewsItemCallBack implements InvocationCallback<NewsItemDTO> {

    private final WebTarget target;
    private ConcertService.NewsItemListener listener;

    private static Logger logger = LoggerFactory
            .getLogger(NewsItemCallBack.class);

    public NewsItemCallBack(ConcertService.NewsItemListener listener, WebTarget target) {
        this.listener = listener;
        this.target = target;
    }

    @Override
    public void completed(NewsItemDTO newsItem) {
        logger.info("Received item: " + newsItem.getContent());
        listener.newsItemReceived(newsItem);
        target.request().async().get(this);
    }

    @Override
    public void failed(Throwable throwable) {
        throwable.printStackTrace();
    }
}

