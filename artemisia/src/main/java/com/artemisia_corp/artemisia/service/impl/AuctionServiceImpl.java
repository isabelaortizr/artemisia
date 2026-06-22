package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Address;
import com.artemisia_corp.artemisia.entity.Auction;
import com.artemisia_corp.artemisia.entity.AuctionParticipant;
import com.artemisia_corp.artemisia.entity.NotaVenta;
import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.dto.auction.AuctionBidRequestDto;
import com.artemisia_corp.artemisia.entity.dto.auction.AuctionConfirmPurchaseDto;
import com.artemisia_corp.artemisia.entity.dto.auction.AuctionParticipantResponseDto;
import com.artemisia_corp.artemisia.entity.dto.auction.AuctionRequestDto;
import com.artemisia_corp.artemisia.entity.dto.auction.AuctionResponseDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.NotaVentaResponseDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailRequestDto;
import com.artemisia_corp.artemisia.entity.enums.AuctionStatus;
import com.artemisia_corp.artemisia.entity.enums.ProductStatus;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import com.artemisia_corp.artemisia.exception.NotDataFoundException;
import com.artemisia_corp.artemisia.exception.OperationException;
import com.artemisia_corp.artemisia.exception.UnauthorizedAccessException;
import com.artemisia_corp.artemisia.repository.AddressRepository;
import com.artemisia_corp.artemisia.repository.AuctionParticipantRepository;
import com.artemisia_corp.artemisia.repository.AuctionRepository;
import com.artemisia_corp.artemisia.repository.NotaVentaRepository;
import com.artemisia_corp.artemisia.repository.ProductRepository;
import com.artemisia_corp.artemisia.repository.UserRepository;
import com.artemisia_corp.artemisia.service.AuctionService;
import com.artemisia_corp.artemisia.service.ImageService;
import com.artemisia_corp.artemisia.service.LogsService;
import com.artemisia_corp.artemisia.service.NotaVentaService;
import com.artemisia_corp.artemisia.service.OrderDetailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AuctionServiceImpl implements AuctionService {
    private final AuctionRepository auctionRepository;
    private final AuctionParticipantRepository auctionParticipantRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotaVentaRepository notaVentaRepository;
    private final AddressRepository addressRepository;
    private final OrderDetailService orderDetailService;
    private final NotaVentaService notaVentaService;
    private final LogsService logsService;
    private final ImageService imageService;

    @Override
    @Transactional(readOnly = true)
    public Page<AuctionResponseDto> getAllAuctions(Pageable pageable) {
        logsService.info("Fetching all auctions");
        return auctionRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuctionResponseDto> getAuctionsByStatus(AuctionStatus status, Pageable pageable) {
        if (status == null) {
            throw new IllegalArgumentException("Auction status is required.");
        }
        logsService.info("Fetching auctions with status: " + status);
        return auctionRepository.findByStatus(status, pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuctionResponseDto> getAuctionsBySeller(Long sellerId, Pageable pageable) {
        if (sellerId == null || sellerId <= 0) {
            throw new IllegalArgumentException("Valid seller ID is required.");
        }
        logsService.info("Fetching auctions for seller ID: " + sellerId);
        return auctionRepository.findBySeller_Id(sellerId, pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuctionResponseDto> getAuctionsWonByUser(Long winnerId, Pageable pageable) {
        if (winnerId == null || winnerId <= 0) {
            throw new IllegalArgumentException("Valid winner ID is required.");
        }
        logsService.info("Fetching auctions won by user ID: " + winnerId);
        return auctionRepository.findByWinner_Id(winnerId, pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public AuctionResponseDto getAuctionById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Auction ID must be greater than 0.");
        }

        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("Auction not found with ID: " + id);
                    return new NotDataFoundException("Auction not found with ID: " + id);
                });

        auction = refreshIfExpired(auction);
        return toDto(auction);
    }

    @Override
    @Transactional
    public AuctionResponseDto createAuction(AuctionRequestDto auctionDto) {
        if (auctionDto == null) {
            throw new IllegalArgumentException("Auction data is required.");
        }
        if (auctionDto.getProductId() == null || auctionDto.getProductId() <= 0) {
            throw new IllegalArgumentException("Valid product ID is required.");
        }
        if (auctionDto.getSellerId() == null || auctionDto.getSellerId() <= 0) {
            throw new IllegalArgumentException("Valid seller ID is required.");
        }
        if (auctionDto.getStartingPrice() == null || auctionDto.getStartingPrice() <= 0) {
            throw new IllegalArgumentException("Starting price must be greater than 0.");
        }
        if (auctionDto.getEndDate() == null) {
            throw new IllegalArgumentException("End date is required.");
        }

        Product product = productRepository.findProductById(auctionDto.getProductId());
        if (product == null) {
            logsService.error("Product not found with ID: " + auctionDto.getProductId());
            throw new NotDataFoundException("Product not found with ID: " + auctionDto.getProductId());
        }

        if (!product.getSeller().getId().equals(auctionDto.getSellerId())) {
            logsService.error("User ID: " + auctionDto.getSellerId() + " attempted to auction product ID: " + product.getId() + " which they do not own");
            throw new UnauthorizedAccessException("No puedes subastar una obra que no es tuya");
        }

        if (product.getStatus() != ProductStatus.AVAILABLE) {
            logsService.error("Product ID: " + product.getId() + " is not available for auction (status: " + product.getStatus() + ")");
            throw new OperationException("El producto no está disponible para subastar");
        }

        if (auctionRepository.findActiveAuctionByProductId(product.getId()).isPresent()) {
            logsService.error("Product ID: " + product.getId() + " already has an active auction");
            throw new OperationException("Ya existe una subasta activa para este producto");
        }

        LocalDateTime startDate = auctionDto.getStartDate() != null ? auctionDto.getStartDate() : LocalDateTime.now();
        LocalDateTime endDate = auctionDto.getEndDate();

        if (!endDate.isAfter(startDate)) {
            throw new OperationException("La fecha final debe ser posterior a la fecha de inicio");
        }

        Auction auction = Auction.builder()
                .product(product)
                .seller(product.getSeller())
                .status(AuctionStatus.ACTIVE)
                .startDate(startDate)
                .endDate(endDate)
                .startingPrice(auctionDto.getStartingPrice())
                .currentPrice(auctionDto.getStartingPrice())
                .build();

        Auction savedAuction = auctionRepository.save(auction);

        product.setStatus(ProductStatus.ON_AUCTION);
        productRepository.save(product);

        logsService.info("Auction created with ID: " + savedAuction.getId() + " for product ID: " + product.getId());
        return toDto(savedAuction);
    }

    @Override
    @Transactional
    public AuctionResponseDto closeAuction(Long auctionId, Long sellerId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> {
                    logsService.error("Auction not found with ID: " + auctionId);
                    return new NotDataFoundException("Auction not found with ID: " + auctionId);
                });

        if (!auction.getSeller().getId().equals(sellerId)) {
            logsService.error("User ID: " + sellerId + " attempted to close auction ID: " + auctionId + " which they do not own");
            throw new UnauthorizedAccessException("No puedes cerrar una subasta que no es tuya");
        }

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new OperationException("La subasta no está activa");
        }

        Auction finished = finishAuction(auction);
        logsService.info("Auction ID: " + auctionId + " closed manually by seller ID: " + sellerId);
        return toDto(finished);
    }

    @Override
    @Transactional
    public AuctionParticipantResponseDto placeBid(AuctionBidRequestDto bidDto) {
        if (bidDto == null || bidDto.getAuctionId() == null || bidDto.getParticipantId() == null) {
            throw new IllegalArgumentException("Auction ID and participant ID are required.");
        }
        if (bidDto.getBidAmount() == null || bidDto.getBidAmount() <= 0) {
            throw new IllegalArgumentException("Bid amount must be greater than 0.");
        }

        Auction auction = auctionRepository.findById(bidDto.getAuctionId())
                .orElseThrow(() -> {
                    logsService.error("Auction not found with ID: " + bidDto.getAuctionId());
                    return new NotDataFoundException("Auction not found with ID: " + bidDto.getAuctionId());
                });

        auction = refreshIfExpired(auction);

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new OperationException("La subasta no está activa");
        }

        if (auction.getSeller().getId().equals(bidDto.getParticipantId())) {
            throw new OperationException("No puedes pujar en tu propia subasta");
        }

        if (bidDto.getBidAmount() <= auction.getCurrentPrice()) {
            throw new OperationException("La puja debe ser mayor al monto actual: " + auction.getCurrentPrice());
        }

        User participant = userRepository.findById(bidDto.getParticipantId())
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + bidDto.getParticipantId());
                    return new NotDataFoundException("User not found with ID: " + bidDto.getParticipantId());
                });

        AuctionParticipant auctionParticipant = auctionParticipantRepository
                .findByAuction_IdAndParticipant_Id(auction.getId(), participant.getId())
                .orElse(AuctionParticipant.builder()
                        .auction(auction)
                        .participant(participant)
                        .build());

        auctionParticipant.setBidAmount(bidDto.getBidAmount());
        auctionParticipant.setBidDate(LocalDateTime.now());
        AuctionParticipant savedParticipant = auctionParticipantRepository.save(auctionParticipant);

        auction.setCurrentPrice(bidDto.getBidAmount());
        auctionRepository.save(auction);

        logsService.info("User ID: " + participant.getId() + " placed a bid of " + bidDto.getBidAmount() + " on auction ID: " + auction.getId());
        return new AuctionParticipantResponseDto(savedParticipant);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuctionParticipantResponseDto> getBidsByAuction(Long auctionId, Pageable pageable) {
        if (!auctionRepository.existsById(auctionId)) {
            logsService.error("Auction not found with ID: " + auctionId);
            throw new NotDataFoundException("Auction not found with ID: " + auctionId);
        }
        return auctionParticipantRepository.findByAuctionIdOrderByBidAmountDesc(auctionId, pageable)
                .map(AuctionParticipantResponseDto::new);
    }

    @Override
    @Transactional
    public NotaVentaResponseDto confirmAuctionPurchase(AuctionConfirmPurchaseDto confirmDto) {
        if (confirmDto == null || confirmDto.getAuctionId() == null || confirmDto.getBuyerId() == null || confirmDto.getAddressId() == null) {
            throw new IllegalArgumentException("Auction ID, buyer ID and address ID are required.");
        }

        Auction auction = auctionRepository.findById(confirmDto.getAuctionId())
                .orElseThrow(() -> {
                    logsService.error("Auction not found with ID: " + confirmDto.getAuctionId());
                    return new NotDataFoundException("Auction not found with ID: " + confirmDto.getAuctionId());
                });

        if (auction.getStatus() != AuctionStatus.FINISHED) {
            throw new OperationException("La subasta no tiene un ganador pendiente de confirmación");
        }

        if (auction.getWinner() == null || !auction.getWinner().getId().equals(confirmDto.getBuyerId())) {
            logsService.error("User ID: " + confirmDto.getBuyerId() + " attempted to confirm auction ID: " + auction.getId() + " without being the winner");
            throw new UnauthorizedAccessException("No eres el ganador de esta subasta");
        }

        NotaVenta notaVenta = auction.getNotaVenta();
        if (notaVenta == null) {
            logsService.error("Auction ID: " + auction.getId() + " has no associated NotaVenta");
            throw new NotDataFoundException("No se encontró la nota de venta asociada a la subasta");
        }

        Address address = addressRepository.findAddressByIdAndUser_Id(confirmDto.getAddressId(), confirmDto.getBuyerId())
                .orElseThrow(() -> {
                    logsService.error("Address not found with ID: " + confirmDto.getAddressId() + " for user ID: " + confirmDto.getBuyerId());
                    return new NotDataFoundException("Address not found with ID: " + confirmDto.getAddressId());
                });

        notaVenta.setBuyerAddress(address);
        notaVentaRepository.save(notaVenta);

        auction.setStatus(AuctionStatus.COMPLETED);
        auctionRepository.save(auction);

        logsService.info("Auction ID: " + auction.getId() + " purchase confirmed by winner user ID: " + confirmDto.getBuyerId());
        return notaVentaService.convertToDtoWithDetails(notaVenta);
    }

    @Override
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processExpiredAuctions() {
        List<Auction> expiredAuctions = auctionRepository.findExpiredActiveAuctions(LocalDateTime.now());
        for (Auction auction : expiredAuctions) {
            finishAuction(auction);
        }
    }

    private AuctionResponseDto toDto(Auction auction) {
        String image = imageService.getLatestImage(auction.getProduct().getId());
        return new AuctionResponseDto(auction, image);
    }

    private Auction refreshIfExpired(Auction auction) {
        if (auction.getStatus() == AuctionStatus.ACTIVE && !auction.getEndDate().isAfter(LocalDateTime.now())) {
            return finishAuction(auction);
        }
        return auction;
    }

    private Auction finishAuction(Auction auction) {
        Page<AuctionParticipant> topBid = auctionParticipantRepository
                .findByAuctionIdOrderByBidAmountDesc(auction.getId(), PageRequest.of(0, 1));

        Product product = auction.getProduct();

        if (!topBid.isEmpty()) {
            AuctionParticipant winnerBid = topBid.getContent().get(0);
            User winner = winnerBid.getParticipant();

            auction.setWinner(winner);
            auction.setCurrentPrice(winnerBid.getBidAmount());
            auction.setStatus(AuctionStatus.FINISHED);

            NotaVenta notaVenta = NotaVenta.builder()
                    .buyer(winner)
                    .estadoVenta(VentaEstado.ON_CART)
                    .date(LocalDateTime.now())
                    .totalGlobal(winnerBid.getBidAmount())
                    .monedaCarrito("BOB")
                    .preciosConvertidos(true)
                    .build();
            NotaVenta savedNotaVenta = notaVentaRepository.save(notaVenta);

            OrderDetailRequestDto detailDto = OrderDetailRequestDto.builder()
                    .groupId(savedNotaVenta.getId())
                    .productId(product.getId())
                    .sellerId(auction.getSeller().getId())
                    .productName(product.getName())
                    .quantity(1)
                    .total(winnerBid.getBidAmount())
                    .build();
            orderDetailService.createOrderDetail(detailDto, savedNotaVenta, product);

            auction.setNotaVenta(savedNotaVenta);
            logsService.info("Auction ID: " + auction.getId() + " finished. Winner: user ID " + winner.getId() + " with bid " + winnerBid.getBidAmount());
        } else {
            auction.setStatus(AuctionStatus.CANCELLED);
            logsService.info("Auction ID: " + auction.getId() + " cancelled (no bids received)");
        }

        if (product.getStatus() == ProductStatus.ON_AUCTION) {
            product.setStatus(product.getStock() > 0 ? ProductStatus.AVAILABLE : ProductStatus.UNAVAILABLE);
            productRepository.save(product);
        }

        return auctionRepository.save(auction);
    }
}
