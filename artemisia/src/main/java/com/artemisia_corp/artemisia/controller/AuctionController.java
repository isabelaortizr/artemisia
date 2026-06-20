package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.config.JwtTokenProvider;
import com.artemisia_corp.artemisia.entity.dto.auction.AuctionBidRequestDto;
import com.artemisia_corp.artemisia.entity.dto.auction.AuctionConfirmPurchaseDto;
import com.artemisia_corp.artemisia.entity.dto.auction.AuctionParticipantResponseDto;
import com.artemisia_corp.artemisia.entity.dto.auction.AuctionRequestDto;
import com.artemisia_corp.artemisia.entity.dto.auction.AuctionResponseDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.entity.enums.AuctionStatus;
import com.artemisia_corp.artemisia.service.AuctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auctions")
@Tag(name = "Auction Management", description = "Endpoints for managing auctions")
public class AuctionController {
    private final AuctionService auctionService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Get all auctions", description = "Returns paginated list of all auctions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Auctions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public ResponseEntity<Page<AuctionResponseDto>> getAllAuctions(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(auctionService.getAllAuctions(pageable));
    }

    @Operation(summary = "Get active auctions", description = "Returns paginated list of currently active auctions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Auctions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/active")
    public ResponseEntity<Page<AuctionResponseDto>> getActiveAuctions(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "endDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(auctionService.getAuctionsByStatus(AuctionStatus.ACTIVE, pageable));
    }

    @Operation(summary = "Get auctions by status", description = "Returns paginated list of auctions filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Auctions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<AuctionResponseDto>> getAuctionsByStatus(
            @PathVariable AuctionStatus status,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(auctionService.getAuctionsByStatus(status, pageable));
    }

    @Operation(summary = "Get auction by ID", description = "Returns a single auction by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Auction found",
                    content = @Content(schema = @Schema(implementation = AuctionResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Auction not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponseDto> getAuctionById(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.getAuctionById(id));
    }

    @Operation(summary = "Get auctions created by a seller", description = "Returns paginated list of auctions created by a given seller")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Auctions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Page<AuctionResponseDto>> getAuctionsBySeller(
            @PathVariable Long sellerId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(auctionService.getAuctionsBySeller(sellerId, pageable));
    }

    @Operation(summary = "Get my auctions", description = "Returns paginated list of auctions created by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Auctions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/mine")
    public ResponseEntity<Page<AuctionResponseDto>> getMyAuctions(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(auctionService.getAuctionsBySeller(jwtTokenProvider.getUserIdFromToken(token), pageable));
    }

    @Operation(summary = "Get auctions won by the authenticated user", description = "Returns paginated list of auctions won by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Auctions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/won")
    public ResponseEntity<Page<AuctionResponseDto>> getAuctionsWon(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return ResponseEntity.ok(auctionService.getAuctionsWonByUser(jwtTokenProvider.getUserIdFromToken(token), pageable));
    }

    @Operation(summary = "Create a new auction", description = "Creates a new auction for a product owned by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Auction created successfully",
                    content = @Content(schema = @Schema(implementation = AuctionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<AuctionResponseDto> createAuction(
            @RequestBody AuctionRequestDto auctionDto,
            @RequestHeader("Authorization") String token) {
        auctionDto.setSellerId(jwtTokenProvider.getUserIdFromToken(token));
        AuctionResponseDto response = auctionService.createAuction(auctionDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Close an auction early", description = "Closes an active auction before its scheduled end date, determining the winner with the current bids")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Auction closed successfully",
                    content = @Content(schema = @Schema(implementation = AuctionResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Auction not found")
    })
    @PutMapping("/{id}/close")
    public ResponseEntity<AuctionResponseDto> closeAuction(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(auctionService.closeAuction(id, jwtTokenProvider.getUserIdFromToken(token)));
    }

    @Operation(summary = "Place a bid on an auction", description = "Registers or updates the authenticated user's bid on an active auction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bid placed successfully",
                    content = @Content(schema = @Schema(implementation = AuctionParticipantResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid bid"),
            @ApiResponse(responseCode = "404", description = "Auction not found")
    })
    @PostMapping("/{id}/bids")
    public ResponseEntity<AuctionParticipantResponseDto> placeBid(
            @PathVariable Long id,
            @RequestBody AuctionBidRequestDto bidDto,
            @RequestHeader("Authorization") String token) {
        bidDto.setAuctionId(id);
        bidDto.setParticipantId(jwtTokenProvider.getUserIdFromToken(token));
        return ResponseEntity.ok(auctionService.placeBid(bidDto));
    }

    @Operation(summary = "Get bids for an auction", description = "Returns paginated list of bids for an auction, ordered from highest to lowest")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bids retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Auction not found")
    })
    @GetMapping("/{id}/bids")
    public ResponseEntity<Page<AuctionParticipantResponseDto>> getBids(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auctionService.getBidsByAuction(id, pageable));
    }

    @Operation(summary = "Confirm auction purchase", description = "Allows the winner of a finished auction to assign a shipping address and confirm the purchase")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchase confirmed successfully",
                    content = @Content(schema = @Schema(implementation = NotaVentaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Auction not ready for confirmation"),
            @ApiResponse(responseCode = "404", description = "Auction, sale note or address not found")
    })
    @PutMapping("/{id}/confirm")
    public ResponseEntity<NotaVentaResponseDto> confirmPurchase(
            @PathVariable Long id,
            @RequestBody AuctionConfirmPurchaseDto confirmDto,
            @RequestHeader("Authorization") String token) {
        confirmDto.setAuctionId(id);
        confirmDto.setBuyerId(jwtTokenProvider.getUserIdFromToken(token));
        return ResponseEntity.ok(auctionService.confirmAuctionPurchase(confirmDto));
    }
}
