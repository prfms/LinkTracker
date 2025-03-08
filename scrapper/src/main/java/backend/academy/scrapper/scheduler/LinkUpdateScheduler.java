package backend.academy.scrapper.scheduler;

import backend.academy.scrapper.service.ScrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
public class LinkUpdateScheduler {

    private final ScrapperService scrapperService;

    @Autowired
    public LinkUpdateScheduler(ScrapperService scrapperService) {
        this.scrapperService = scrapperService;
    }

    @Scheduled(fixedRate = 60_000) // Каждую минуту
    public void checkUpdates() {
        scrapperService.checkAndUpdateLinks();
    }
}
