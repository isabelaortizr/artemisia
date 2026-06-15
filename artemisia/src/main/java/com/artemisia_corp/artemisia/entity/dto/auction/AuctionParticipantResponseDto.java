package com.artemisia_corp.artemisia.entity.dto.auction;

import com.artemisia_corp.artemisia.entity.AuctionParticipant;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AuctionParticipantResponseDto {
    private Long id;
    private Long auctionId;
    private Long participantId;
    private String participantName;
    private Double bidAmount;
    private LocalDateTime bidDate;

    public AuctionParticipantResponseDto(AuctionParticipant participant) {
        this.id = participant.getId();
        this.auctionId = participant.getAuction().getId();
        this.participantId = participant.getParticipant().getId();
        this.participantName = participant.getParticipant().getName();
        this.bidAmount = participant.getBidAmount();
        this.bidDate = participant.getBidDate();
    }
}
