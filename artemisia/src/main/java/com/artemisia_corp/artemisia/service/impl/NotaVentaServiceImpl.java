package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.*;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.*;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailRequestDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.ManageProductDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import com.artemisia_corp.artemisia.entity.enums.ProductStatus;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import com.artemisia_corp.artemisia.integracion.SterumPayService;
import com.artemisia_corp.artemisia.integracion.impl.dtos.EstadoResponseDto;
import com.artemisia_corp.artemisia.integracion.impl.dtos.StereumPagaDto;
import com.artemisia_corp.artemisia.integracion.impl.dtos.StereumPagaResponseDto;
import com.artemisia_corp.artemisia.repository.*;
import com.artemisia_corp.artemisia.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class NotaVentaServiceImpl implements NotaVentaService {
    @Autowired
    private NotaVentaRepository notaVentaRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    @Lazy
    private AddressRepository addressRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ProductService productService;
    @Autowired
    private LogsService logsService;
    @Autowired
    private SterumPayService sterumPayService;

    @Override
    public Page<NotaVentaResponseDto> getAllNotasVenta(Pageable pageable) {
        logsService.info("Fetching all sales notes");
        Page<NotaVentaResponseDto> notaVentaPage = notaVentaRepository.findAllNotaVentas(pageable);
        return notaVentaPage.map(this::enrichWithOrderDetails);
    }

    @Override
    public NotaVentaResponseDto getNotaVentaById(Long id) {
        logsService.info("Fetching sale note with ID: " + id);
        NotaVenta notaVenta = notaVentaRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("Sale note not found with ID: " + id);
                    throw new RuntimeException("Sale note not found");
                });
        return convertToDtoWithDetails(notaVenta);
    }

    @Override
    @Transactional
    public NotaVentaResponseDto createNotaVenta(NotaVentaRequestDto notaVentaDto) {
        User buyer = userRepository.findById(notaVentaDto.getUserId())
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + notaVentaDto.getUserId());
                    throw new RuntimeException("User not found");
                });

        Address address = addressRepository.findById(notaVentaDto.getBuyerAddress())
                .orElseThrow(() -> {
                    logsService.error("Address not found with ID: " + notaVentaDto.getBuyerAddress());
                    throw new RuntimeException("Address not found");
                });

        NotaVenta notaVenta = NotaVenta.builder()
                .buyer(buyer)
                .buyerAddress(address)
                .estadoVenta(VentaEstado.valueOf(notaVentaDto.getEstadoVenta()))
                .date(notaVentaDto.getDate() != null ? notaVentaDto.getDate() : LocalDateTime.now())
                .build();

        double total = 0.0;
        HashMap<Long, Product> products = new HashMap<>();
        for (OrderDetailRequestDto detailDto : notaVentaDto.getDetalles()) {
            ProductResponseDto p = productService.getProductById(detailDto.getProductId());
            products.put(detailDto.getProductId(), convertToProduct(p.getProductId(), p));

            productService.manageStock(new ManageProductDto(p.getProductId(),
                    detailDto.getQuantity(),
                    false));
            logsService.info("Reduced stock for product ID: " + detailDto.getProductId() +
                    " by quantity: " + detailDto.getQuantity());

            detailDto.setTotal(detailDto.getQuantity() * p.getPrice());
            total += detailDto.getTotal();
        }
        notaVenta.setTotalGlobal(total);

        NotaVenta savedNotaVenta = notaVentaRepository.save(notaVenta);
        logsService.info("Sale note created with ID: " + savedNotaVenta.getId());

        for (OrderDetailRequestDto detailDto : notaVentaDto.getDetalles()) {
            detailDto.setGroupId(savedNotaVenta.getId());
            orderDetailService.createOrderDetail(detailDto, savedNotaVenta, products.get(detailDto.getProductId()));
        }
        return convertToDtoWithDetails(savedNotaVenta);
    }

    @Override
    @Transactional
    public NotaVentaResponseDto updateNotaVenta(Long id, NotaVentaRequestDto notaVentaDto) {
        User buyer = userRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + id);
                    throw new RuntimeException("User not found");
                });

        NotaVenta notaVenta = notaVentaRepository.findByBuyer_IdAndEstadoVenta(id)
                .orElseThrow(() -> {
                    logsService.error("Sale note not found with ID: " + id);
                    throw new RuntimeException("Sale note not found");
                });

        Address address = addressRepository.findById(notaVentaDto.getBuyerAddress())
                .orElseThrow(() -> {
                    logsService.error("Address not found with ID: " + notaVentaDto.getBuyerAddress());
                    throw new RuntimeException("Address not found");
                });

        notaVenta.setBuyer(buyer);
        notaVenta.setBuyerAddress(address);
        notaVenta.setEstadoVenta(VentaEstado.valueOf(notaVentaDto.getEstadoVenta()));
        notaVenta.setDate(notaVentaDto.getDate() != null ? notaVentaDto.getDate() : LocalDateTime.now());

        double total = 0.0;
        HashMap<Long, Product> products = new HashMap<>();
        for (OrderDetailRequestDto detailDto : notaVentaDto.getDetalles()) {
            ProductResponseDto p = productService.getProductById(detailDto.getProductId());
            products.put(detailDto.getProductId(), convertToProduct(p.getProductId(), p));

            detailDto.setTotal(detailDto.getQuantity() * p.getPrice());
            total += detailDto.getTotal();
        }
        notaVenta.setTotalGlobal(total);

        NotaVenta updatedNotaVenta = notaVentaRepository.save(notaVenta);
        logsService.info("Sale note updated with ID: " + updatedNotaVenta.getId());

        List<OrderDetailResponseDto> existingDetails = orderDetailService.getOrderDetailsByNotaVenta(id);

        for (OrderDetailResponseDto existingDetail : existingDetails) {
            boolean found = notaVentaDto.getDetalles().stream()
                    .anyMatch(d -> d.getProductId().equals(existingDetail.getProductId()));
            if (!found) {
                orderDetailService.deleteOrderDetail(existingDetail.getId());
            }
        }

        for (OrderDetailRequestDto detailDto : notaVentaDto.getDetalles()) {
            detailDto.setGroupId(notaVenta.getId());
            OrderDetailResponseDto existingDetail = existingDetails.stream()
                    .filter(d -> d.getProductId().equals(detailDto.getProductId()))
                    .findFirst()
                    .orElse(null);

            if (existingDetail != null) {
                detailDto.setGroupId(notaVenta.getId());
                orderDetailService.updateOrderDetail(existingDetail.getId(), detailDto);
            } else {
                orderDetailService.createOrderDetail(detailDto, updatedNotaVenta, products.get(detailDto.getProductId()));
            }
        }

        return convertToDtoWithDetails(updatedNotaVenta);
    }

    @Override
    @Transactional
    public void deleteNotaVenta(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + id);
                    throw new RuntimeException("User not found");
                });

        NotaVenta notaVenta = notaVentaRepository.findByBuyer_IdAndEstadoVenta(id)
                .orElseThrow(() -> {
                    logsService.error("Sale note not found with ID: " + id);
                    throw new RuntimeException("Sale note not found");
                });

        if (notaVenta == null) {
            logsService.error("Sale note not found with ID: " + id);
            throw new RuntimeException("Sale note not found");
        }

        logsService.info("Deleting Details affiliated with ID: " + id);
        /*
        orderDetailService.getOrderDetailsByNotaVenta(id).forEach(detail -> {
            orderDetailService.deleteOrderDetail(detail.getId());
        });
         */

        notaVenta.setEstadoVenta(VentaEstado.DELETED);
        notaVentaRepository.save(notaVenta);
        logsService.info("Sale note deleted with ID: " + notaVenta.getId());
    }

    @Override
    @Transactional
    public void completeNotaVenta(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + id);
                    throw new RuntimeException("User not found");
                });

        NotaVenta notaVenta = notaVentaRepository.findByBuyer_IdAndEstadoVenta(id)
                .orElseThrow(() -> {
                    logsService.error("Sale note not found with ID: " + id);
                    throw new RuntimeException("Sale note not found");
                });

        if (notaVenta.getEstadoVenta() == VentaEstado.PAYED) {
            logsService.warning("Sale note already completed with ID: " + id);
            return;
        }

        if (isNotaVentaCompleted(id)) {
            notaVenta.setEstadoVenta(VentaEstado.PAYED);
            notaVentaRepository.save(notaVenta);
            logsService.info("Sale note completed with ID: " + id);
        } else throw new RuntimeException("Compra no fue completada");
    }

    @Override
    @Transactional
    public void cancelarNotaVenta(Long id) {
        NotaVenta notaVenta = notaVentaRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("Sale note not found with ID: " + id);
                    throw new RuntimeException("Sale note not found");
                });

        if (!isNotaVentaCompleted(id)) throw new RuntimeException("Compra no fue completada");

        List<OrderDetailResponseDto> detalles = orderDetailService.getOrderDetailsByNotaVenta(id);
        for (OrderDetailResponseDto detalle : detalles) {
            productService.manageStock(new ManageProductDto(detalle.getProductId(),
                    detalle.getQuantity(),
                    false));
            logsService.info("Augmented stock for product ID: " + detalle.getProductId() +
                    " by quantity: " + detalle.getQuantity());
        }

        notaVenta.setEstadoVenta(VentaEstado.PAYED);
        notaVentaRepository.save(notaVenta);
        logsService.info("Sale note completed with ID: " + id);
    }

    @Override
    public Page<NotaVentaResponseDto> getNotasVentaByEstado(VentaEstado estado, Pageable pageable) {
        logsService.info("Fetching sale notes with status: " + estado);
        Page<NotaVentaResponseDto> notaVentaPage = notaVentaRepository.findByEstadoVenta(estado, pageable);
        return notaVentaPage.map(this::enrichWithOrderDetails);
    }

    @Override
    public Page<NotaVentaResponseDto> getCompletedSalesByUser(Long userId, Pageable pageable) {
        logsService.info("Fetching completed sales for user ID: " + userId);
        Page<NotaVentaResponseDto> notaVentaPage = notaVentaRepository.findAllNotaVentasByBuyer_Id(userId, pageable);
        return notaVentaPage.map(this::enrichWithOrderDetails);
    }

    @Override
    public void ingresarIdTransaccion(String idTransaccion, Long notaVentaId) {
        NotaVenta notaVenta = notaVentaRepository.findById(notaVentaId)
                .orElseThrow(() -> {
                    logsService.error("Sale note not found with ID: " + notaVentaId);
                    throw new RuntimeException("Sale note not found");
                });

        notaVenta.setIdTransaccion(idTransaccion);
        notaVentaRepository.save(notaVenta);
        logsService.info("Sale note updated with ID: " + notaVentaId);
    }

    @Override
    public void obtenerRespuestaTransaccion(RespuestaVerificacionNotaVentaDto respuesta){
        NotaVenta notaVenta = notaVentaRepository.findNotaVentaByIdTransaccion(respuesta.getId());

        EstadoResponseDto estado = sterumPayService.obtenerEstadoCobro(notaVenta.getIdTransaccion());

        if ("PAGADO".equals(estado.getStatus())) {
            logsService.info("Transaction completed successfully for ID: " + notaVenta.getId());
            completeNotaVenta(notaVenta.getBuyer().getId());
        } else if ("CANCELADA".equals(estado.getStatus())) {
            logsService.info("Transaction canceled for ID: " + notaVenta.getId());
            deleteNotaVenta(notaVenta.getBuyer().getId());
        } else {
            logsService.warning("Transaction has not been finalized for ID: " + notaVenta.getId() + " with status: " + estado.getStatus());
        }
    }

    @Override
    @Transactional
    public NotaVentaResponseDto getActiveCartByUserId(Long userId) {
        Optional<NotaVenta> existingNotaVenta = notaVentaRepository.findByBuyer_IdAndEstadoVenta(userId);

        if (existingNotaVenta.isPresent()) {
                return convertToDtoWithDetails(existingNotaVenta.get());
        }

        logsService.info("No active cart found for user ID: " + userId + ", creating new one");

        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + userId);
                    throw new RuntimeException("User not found");
                });

        Address address = addressRepository.findLastAddressByUser_Id(userId);
        if (address == null) {
            logsService.error("User has no addresses");
            throw new RuntimeException("User must have at least one address");
        }

        NotaVenta newNotaVenta = NotaVenta.builder()
                .buyer(buyer)
                .buyerAddress(address)
                .estadoVenta(VentaEstado.ON_CART)
                .date(LocalDateTime.now())
                .totalGlobal(0.0)
                .build();

        NotaVenta savedNotaVenta = notaVentaRepository.save(newNotaVenta);
        logsService.info("Created new sale note for user ID: " + userId);

        return convertToDtoWithDetails(savedNotaVenta);
    }

    @Override
    @Transactional
    public NotaVentaResponseDto addProductToCart(AddToCartDto addToCartDto) {
        NotaVenta cart = notaVentaRepository.findByBuyer_IdAndEstadoVenta(addToCartDto.getUserId())
                .orElseGet(() -> {
                    User buyer = userRepository.findById(addToCartDto.getUserId())
                            .orElseThrow(() -> {
                                logsService.error("User not found with ID: " + addToCartDto.getUserId());
                                throw new RuntimeException("User not found");
                            });

                    Address address = addressRepository.findLastAddressByUser_Id(buyer.getId());
                    if (address == null) {
                        logsService.error("User has no addresses");
                        throw new RuntimeException("User must have at least one address");
                    }

                    NotaVenta newCart = NotaVenta.builder()
                            .buyer(buyer)
                            .buyerAddress(address)
                            .estadoVenta(VentaEstado.ON_CART)
                            .date(LocalDateTime.now())
                            .totalGlobal(0.0)
                            .build();

                    return notaVentaRepository.save(newCart);
                });

        Product product = productRepository.findById(addToCartDto.getProductId())
                .orElseThrow(() -> {
                    logsService.error("Product not found with ID: " + addToCartDto.getProductId());
                    throw new RuntimeException("Product not found");
                });

        Optional<OrderDetail> existingDetail = orderDetailRepository.findByGroupIdAndProductId(cart.getId(), product.getId());

        if (existingDetail.isPresent()) {
            OrderDetail detail = existingDetail.get();
            int newQuantity = detail.getQuantity() + addToCartDto.getQuantity();

            if (newQuantity > product.getStock()) {
                logsService.error("Not enough stock for product ID: " + product.getId());
                throw new RuntimeException("Not enough stock available");
            }

            detail.setQuantity(newQuantity);
            detail.setTotal(newQuantity * product.getPrice());
            orderDetailRepository.save(detail);
        } else {
            if (addToCartDto.getQuantity() > product.getStock()) {
                logsService.error("Not enough stock for product ID: " + product.getId());
                throw new RuntimeException("Not enough stock available");
            }

            OrderDetailRequestDto detailDto = OrderDetailRequestDto.builder()
                    .groupId(cart.getId())
                    .productId(product.getId())
                    .sellerId(product.getSeller().getId())
                    .productName(product.getName())
                    .quantity(addToCartDto.getQuantity())
                    .total(addToCartDto.getQuantity() * product.getPrice())
                    .build();

            orderDetailService.createOrderDetail(detailDto, cart, product);
        }

        List<OrderDetailResponseDto> details = orderDetailService.getOrderDetailsByNotaVenta(cart.getId());

        double newTotal = details.stream()
                .mapToDouble(OrderDetailResponseDto::getTotal)
                .sum();

        cart.setTotalGlobal(newTotal);
        notaVentaRepository.save(cart);

        return convertToDtoWithDetails(cart);
    }

    @Override
    @Transactional
    public StereumPagaResponseDto getPaymentInfo(RequestPaymentDto request) {
        NotaVenta notaVenta = notaVentaRepository.findByBuyer_IdAndEstadoVenta(request.getUserId())
                .orElseThrow(() -> {
                    logsService.error("Sale note not found for user ID: " + request.getUserId());
                    throw new RuntimeException("Sale note not found");
                });

        return sterumPayService.crearCargoCobro(StereumPagaDto.builder()
                    .country(request.getCountry())
                    .amount(notaVenta.getTotalGlobal().toString())
                    .network(request.getNetwork())
                    .currency(request.getCurrency())
                    .chargeReason(request.getChargeReason())
                    .build(),
                request.getUserId());
    }
    private boolean isNotaVentaCompleted(Long notaVentaId) {
        NotaVenta notaVenta = notaVentaRepository.getReferenceById(notaVentaId);
        EstadoResponseDto estado = sterumPayService.obtenerEstadoCobro(notaVenta.getIdTransaccion());

        return estado.getStatus().equals("PAGADO");
    }

    private NotaVentaResponseDto convertToDtoWithDetails(NotaVenta notaVenta) {
        List<OrderDetailResponseDto> detalles = orderDetailService.getOrderDetailsByNotaVenta(notaVenta.getId());

        return NotaVentaResponseDto.builder()
                .id(notaVenta.getId())
                .userId(notaVenta.getBuyer().getId())
                .buyerAddress(notaVenta.getBuyerAddress().getId())
                .estadoVenta(notaVenta.getEstadoVenta().name())
                .totalGlobal(notaVenta.getTotalGlobal())
                .date(notaVenta.getDate())
                .idTransaccion(notaVenta.getIdTransaccion())
                .detalles(detalles)
                .build();
    }

    private NotaVentaResponseDto enrichWithOrderDetails(NotaVentaResponseDto dto) {
        Pageable firstPage = PageRequest.of(0, Integer.MAX_VALUE);
        Page<OrderDetailResponseDto> details = orderDetailService.getOrderDetailsByNotaVenta(dto.getId(), firstPage);
        dto.setDetalles(details.getContent());
        return dto;
    }

    private Product convertToProduct(Long productId, ProductResponseDto preProduct) {
        return Product.builder()
                .id(productId)
                .name(preProduct.getName())
                .technique(PaintingTechnique.valueOf(preProduct.getTechnique()))
                .materials(preProduct.getMaterials())
                .description(preProduct.getDescription())
                .price(preProduct.getPrice())
                .stock(preProduct.getStock())
                .status(ProductStatus.valueOf(preProduct.getStatus()))
                .imageUrl(preProduct.getImage())
                .category(PaintingCategory.valueOf(preProduct.getCategory()))
                .seller(userRepository.getReferenceById(preProduct.getSellerId()))
                .build();
    }
}