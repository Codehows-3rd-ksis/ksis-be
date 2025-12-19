package com.codehows.ksisbe.status.service;

import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.status.dto.webSocketMessageDto.CrawlMessage;
import com.codehows.ksisbe.status.dto.webSocketMessageDto.CrawlProgressDto;
import com.codehows.ksisbe.status.dto.webSocketMessageDto.CrawlProgressMessage;
import com.codehows.ksisbe.status.dto.webSocketMessageDto.CrawlResultItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static org.springframework.data.util.TypeUtils.type;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlProgressPushService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    //현황페이지 진행도
    public void pushProgress(CrawlWork crawlWork) {

        CrawlProgressMessage message =
                new CrawlProgressMessage(
                        crawlWork.getWorkId(),
                        crawlWork.getProgress()
                );

        //관리자는 모든 유저의 진행도
        simpMessagingTemplate.convertAndSend(
                "/topic/crawling-progress",
                message



        );
        log.info("WS SEND progress workId={} progress={}",
                crawlWork.getWorkId(),
                crawlWork.getProgress());

        //유저는 자신 진행도
        if (crawlWork.getStartedBy() != null) {
            simpMessagingTemplate.convertAndSendToUser(
                    crawlWork.getStartedBy().getUsername(),
                    "/queue/crawling-progress",
                    message
            );
        }
    }

    //상세페이지 추출결과
    public void pushCollect(CrawlWork crawlWork, CrawlResultItem crawlResultItem) {
        boolean finished =
                crawlWork.getCollectCount() >= crawlWork.getTotalCount();

        String finalState = null;
        LocalDateTime endAt = null;

        if (finished) {
            finalState = calculateFinalState(
                    crawlWork.getTotalCount(),
                    crawlWork.getFailCount()
            );
            endAt = LocalDateTime.now();
        }
        CrawlMessage message = CrawlMessage.builder()
                .type("COLLECT_UPDATE")
                .workId(crawlWork.getWorkId())
                .data(
                        CrawlProgressDto.builder()
                                .totalCount(crawlWork.getTotalCount())
                                .collectCount(crawlWork.getCollectCount())
                                .failCount(crawlWork.getFailCount())
                                .progress(crawlWork.getProgress())
                                .expectEndAt(crawlWork.getExpectEndAt())
                                .endAt(endAt)
                                .state(finalState)
                                .build()
                )
                .crawlResultItem(
                        CrawlResultItemDto.builder()
                                .id(crawlResultItem.getItemId())
                                .seq(crawlResultItem.getSeq())
                                .resultValue(crawlResultItem.getResultValue())
                                .state(crawlResultItem.getState())
                                .url(crawlResultItem.getPageUrl())
                                .build()
                )
                .build();

        //관리자
        simpMessagingTemplate.convertAndSend(
                "/topic/crawling-progress/" + crawlWork.getWorkId(),
                message
        );

        //유저
        if (crawlWork.getStartedBy() != null) {
            simpMessagingTemplate.convertAndSendToUser(
                    crawlWork.getStartedBy().getUsername(),
                    "/queue/crawling-progress/" + crawlWork.getWorkId(),
                    message
            );
        }
    }

    private String calculateFinalState(int total, int failCount) {
        int successCount = total - failCount;

        if (failCount == 0) {
            return "SUCCESS";
        } else if (successCount == 0) {
            return "FAILED";
        } else {
            return "PARTIAL";
        }
    }
}
