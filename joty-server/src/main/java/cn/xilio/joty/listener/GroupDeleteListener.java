package cn.xilio.joty.listener;

import cn.xilio.joty.domain.event.GroupDeleteEvent;
import cn.xilio.joty.service.ShortUrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * GroupDeleteEvent handler
 */
@Component
public class GroupDeleteListener {
    private final Logger LOGGER = LoggerFactory.getLogger(GroupDeleteListener.class);
    @Autowired
    private ShortUrlService shortUrlService;

    @EventListener
    @Async("shortUrlEventExecutor")
    public void handle(GroupDeleteEvent event) {
        try {
            LOGGER.info("GroupDeleteEvent:[{}]", event.getGroupIds());
            List<String> groupIds = event.getGroupIds();
            //Migrate group to new group
            shortUrlService.migrateGroupToDefault(groupIds);
        } catch (Exception e) {
            LOGGER.error("GroupDeleteEvent", e);
        }
    }
}
