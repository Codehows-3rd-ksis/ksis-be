package com.codehows.ksisbe.status.service;

import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.status.dto.CrawlProgressMessage;
import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
import com.codehows.ksisbe.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class CrawlProgressPushService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public void pushProgress(CrawlWork crawlWork) {

        CrawlProgressMessage message =
                new CrawlProgressMessage(
                        crawlWork.getProgress()
                );

        //관리자는 모든 유저의 진행도
        simpMessagingTemplate.convertAndSend(
                "/topic/crawling-progress",
                message
        );

        //유저는 자신 진행도
        if (crawlWork.getStartedBy() != null) {
            simpMessagingTemplate.convertAndSendToUser(
                    crawlWork.getStartedBy().getUsername(),
                    "/queue/crawling-progress",
                    message
            );
        }
    }
}
