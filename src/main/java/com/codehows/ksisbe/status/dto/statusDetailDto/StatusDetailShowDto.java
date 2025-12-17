package com.codehows.ksisbe.status.dto.statusDetailDto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatusDetailShowDto {
    private BasicInfo basicInfo;
    private List<FailureItem> failureList;
    private CollectionData collectionData;
    private ProgressInfo progress;
}
