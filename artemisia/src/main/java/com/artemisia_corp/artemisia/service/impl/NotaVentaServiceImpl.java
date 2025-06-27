package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.*;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.*;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailRequestDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.ManageProductDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.UpdateOrderDetailDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.UpdateQuantityDetailDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.entity.enums.*;
import com.artemisia_corp.artemisia.exception.IncompleteAddressException;
import com.artemisia_corp.artemisia.exception.NotDataFoundException;
import com.artemisia_corp.artemisia.integracion.SterumPayService;
import com.artemisia_corp.artemisia.integracion.impl.dtos.EstadoResponseDto;
import com.artemisia_corp.artemisia.integracion.impl.dtos.StereumPagaDto;
import com.artemisia_corp.artemisia.integracion.impl.dtos.StereumPagaResponseDto;
import com.artemisia_corp.artemisia.repository.*;
import com.artemisia_corp.artemisia.service.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
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
import java.util.Objects;
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
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("NotaVenta ID must be greater than 0.");
        }

        NotaVenta notaVenta = notaVentaRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("Sale note not found with ID: " + id);
                    return new NotDataFoundException("Sale note not found");
                });

        return convertToDtoWithDetails(notaVenta);
    }

    @Override
    @Transactional
    public NotaVentaResponseDto createNotaVenta(NotaVentaRequestDto notaVentaDto) {
        if (notaVentaDto == null) {
            log.error("NotaVenta data is required.");
            logsService.error("NotaVenta data is required.");
            throw new IllegalArgumentException("NotaVenta data is required.");
        }
        if (notaVentaDto.getUserId() == null || notaVentaDto.getUserId() <= 0) {
            log.error("Valid User ID is required.");
            logsService.error("Valid User ID is required.");
            throw new IllegalArgumentException("Valid User ID is required.");
        }

        User buyer = userRepository.findById(notaVentaDto.getUserId())
                .orElseThrow(() -> {
                    log.error("User not found with ID: " + notaVentaDto.getUserId());
                    logsService.error("User not found with ID: " + notaVentaDto.getUserId());
                    return new NotDataFoundException("User not found with ID: " + notaVentaDto.getUserId());
                });

        NotaVentaResponseDto nv = this.getActiveCartByUserId(buyer.getId());

        NotaVenta notaVenta = NotaVenta.builder()
                .buyer(buyer)
                .totalGlobal(0.0)
                .estadoVenta(VentaEstado.ON_CART)
                .date(LocalDateTime.now())
                .build();

        if (notaVentaDto.getDetalles() != null && !notaVentaDto.getDetalles().isEmpty()) {
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

        return convertToDtoWithDetails(notaVentaRepository.save(notaVenta));
    }

    @Override
    @Transactional
    public NotaVentaResponseDto updateNotaVenta(Long id, NotaVentaRequestDto notaVentaDto) {
        User buyer = userRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + id);
                    return new NotDataFoundException("User not found");
                });

        NotaVenta notaVenta = notaVentaRepository.findByBuyer_IdAndEstadoVenta(id)
                .orElseThrow(() -> {
                    logsService.error("Sale note not found with ID: " + id);
                    return new NotDataFoundException("Sale note not found");
                });

        Address address = addressRepository.findById(notaVentaDto.getBuyerAddress())
                .orElse(null);

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
    public void deleteNotaVenta(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + id);
                    return new NotDataFoundException("User not found");
                });

        NotaVenta notaVenta = notaVentaRepository.findByBuyer_IdAndEstadoVenta(id)
                .orElseThrow(() -> {
                    logsService.error("Sale note not found with ID: " + id);
                    return new NotDataFoundException("Sale note not found");
                });

        log.info("Deleting Details affiliated with ID: " + id);
        logsService.info("Deleting Details affiliated with ID: " + id);

        orderDetailService.getOrderDetailsByNotaVenta(notaVenta.getId()).forEach(detail -> {
            log.info("cantidad del producto" + detail.getQuantity());
            productService.manageStock(new ManageProductDto(
                    detail.getProductId(),
                    detail.getQuantity(),
                    false));
            logsService.info("Augmented stock for product ID: " + detail.getProductId() +
                    " by quantity: " + detail.getQuantity());
        });


        notaVenta.setEstadoVenta(VentaEstado.DELETED);
        notaVentaRepository.save(notaVenta);
        logsService.info("Sale note deleted with ID: " + notaVenta.getId());
    }

    @Override
    public void completeNotaVenta(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: " + id);
                    logsService.error("User not found with ID: " + id);
                    return new NotDataFoundException("User not found");
                });

        NotaVenta notaVenta = notaVentaRepository.findByBuyer_IdAndEstadoVenta(id)
                .orElseThrow(() -> {
                    log.error("Sale note not found with ID: " + id);
                    logsService.error("Sale note not found with ID: " + id);
                    return new NotDataFoundException("Sale note not found");
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
                    log.error("Sale note not found with ID: " + id);
                    logsService.error("Sale note not found with ID: " + id);
                    return new NotDataFoundException("Sale note not found");
                });

        List<OrderDetailResponseDto> detalles = orderDetailService.getOrderDetailsByNotaVenta(id);
        for (OrderDetailResponseDto detalle : detalles) {
            productService.manageStock(new ManageProductDto(
                    detalle.getProductId(),
                    detalle.getQuantity(),
                    false));
            logsService.info("Augmented stock for product ID: " + detalle.getProductId() +
                    " by quantity: " + detalle.getQuantity());
        }

        notaVenta.setEstadoVenta(VentaEstado.DELETED);
        notaVentaRepository.save(notaVenta);
        logsService.info("Sale note completed with ID: " + id);
    }

    @Override
    public Page<NotaVentaResponseDto> getNotasVentaByEstado(VentaEstado estado, Pageable pageable) {
        if (estado == null) {
            log.error("VentaEstado is required.");
            logsService.error("VentaEstado is required.");
            throw new IllegalArgumentException("VentaEstado is required.");
        }

        logsService.info("Fetching sale notes with status: " + estado);
        Page<NotaVentaResponseDto> notaVentaPage = notaVentaRepository.findByEstadoVenta(estado, pageable);
        return notaVentaPage.map(this::enrichWithOrderDetails);
    }

    @Override
    public Page<NotaVentaResponseDto> getCompletedSalesByUser(Long userId, Pageable pageable) {
        if (userId == null || userId <= 0) {
            log.error("Valid User ID is required.");
            logsService.error("Valid User ID is required.");
            throw new IllegalArgumentException("Valid User ID is required.");
        }
        logsService.info("Fetching completed sales for user ID: " + userId);
        Page<NotaVentaResponseDto> notaVentaPage = notaVentaRepository.findAllNotaVentasByBuyer_Id(userId, pageable);
        return notaVentaPage.map(this::enrichWithOrderDetails);
    }

    @Override
    public void ingresarIdTransaccion(String idTransaccion, Long notaVentaId) {
        NotaVenta notaVenta = notaVentaRepository.findById(notaVentaId)
                .orElseThrow(() -> {
                    log.error("Sale note not found with ID: " + notaVentaId);
                    logsService.error("Sale note not found with ID: " + notaVentaId);
                    return new NotDataFoundException("Sale note not found");
                });

        notaVenta.setIdTransaccion(idTransaccion);
        notaVentaRepository.save(notaVenta);
        logsService.info("Sale note updated with ID: " + notaVentaId);
    }

    @Override
    public void obtenerRespuestaTransaccion(RespuestaVerificacionNotaVentaDto respuesta){
        NotaVenta notaVenta = notaVentaRepository.findNotaVentaByIdTransaccion(respuesta.getId());

        logsService.info("Obtaining transaction status for ID: " + notaVenta.getId());
        EstadoResponseDto estado = sterumPayService.obtenerEstadoCobro(notaVenta.getIdTransaccion());

        NotaVentaResponseDto notaVentaResponseDto = this.getActiveCartByUserId(notaVenta.getBuyer().getId());

        if (notaVentaResponseDto.getIdTransaccion() == null ||
                !notaVentaResponseDto.getIdTransaccion().equals(respuesta.getId())){
            log.error("Transaction ID does not match the cart");
            logsService.error("Transaction ID does not match the cart");
            throw new RuntimeException("Transaction ID does not match the cart");
        }

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
    public void assignAddressToNotaVenta(SetAddressDto setAddressDto) {
        NotaVenta notaVenta = notaVentaRepository.findByBuyer_IdAndEstadoVenta(setAddressDto.getUserId())
                .orElseThrow(() -> {
                    log.error("NotaVenta not found with User ID: " + setAddressDto.getUserId() + " " +
                            "In class NotaVentaServiceImpl.assignAddressToNotaVenta() method.");
                    logsService.error("NotaVenta not found with ID: " + setAddressDto.getUserId() + " " +
                            "In class NotaVentaServiceImpl.assignAddressToNotaVenta() method.");
                    return new EntityNotFoundException("NotaVenta not found with ID: " + setAddressDto.getUserId());
                });

        Address address = addressRepository.findAddressByIdAndUser_Id(setAddressDto.getAddressId(), setAddressDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Address not found with ID: " + setAddressDto));

        notaVenta.setBuyerAddress(address);

        notaVentaRepository.save(notaVenta);
    }

    @Override
    public NotaVentaResponseDto getActiveCartByUserId(Long userId) {
        Optional<NotaVenta> existingNotaVenta = notaVentaRepository.findByBuyer_IdAndEstadoVenta(userId);

        if (existingNotaVenta.isPresent()) {
            NotaVenta notaVenta = existingNotaVenta.get();
            Double recalculatedTotal = orderDetailRepository.calculateTotalByNotaVenta(notaVenta.getId());

            if (!Objects.equals(notaVenta.getTotalGlobal(), recalculatedTotal)) {
                logsService.info("Updating totalGlobal for active cart of user ID: " + userId);
                notaVenta.setTotalGlobal(recalculatedTotal != null ? recalculatedTotal : 0.0);
                notaVentaRepository.save(notaVenta);
            }

            return convertToDtoWithDetails(notaVenta);
        }

        logsService.info("No active cart found for user ID: " + userId + ", creating new one");

        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + userId);
                    return new NotDataFoundException("User not found");
                });

        NotaVenta newNotaVenta = NotaVenta.builder()
                .buyer(buyer)
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
                                return new NotDataFoundException("User not found");
                            });


                    NotaVenta newCart = NotaVenta.builder()
                            .buyer(buyer)
                            .estadoVenta(VentaEstado.ON_CART)
                            .date(LocalDateTime.now())
                            .totalGlobal(0.0)
                            .build();

                    return notaVentaRepository.save(newCart);
                });

        Product product = productRepository.findById(addToCartDto.getProductId())
                .orElseThrow(() -> {
                    logsService.error("Product not found with ID: " + addToCartDto.getProductId());
                    return new NotDataFoundException("Product not found");
                });

        Optional<OrderDetail> existingDetail = orderDetailRepository.findByGroupIdAndProductId(cart.getId(), product.getId());

        if (existingDetail.isPresent()) {
            OrderDetail detail = existingDetail.get();
            int newQuantity = detail.getQuantity() + addToCartDto.getQuantity();

            if (addToCartDto.getQuantity() > product.getStock()) {
                log.error("Not enough stock for product ID: " + product.getId() + ", quantity: " + newQuantity + ", stock: " + product.getStock());
                logsService.error("Not enough stock for product ID: " + product.getId());
                throw new NotDataFoundException("Not enough stock available");
            }

            detail.setQuantity(newQuantity);
            detail.setTotal(newQuantity * product.getPrice());
            productService.manageStock(new ManageProductDto(product.getId(), addToCartDto.getQuantity(), true));
            orderDetailRepository.save(detail);
        } else {
            if (addToCartDto.getQuantity() > product.getStock()) {
                logsService.error("Not enough stock for product ID: " + product.getId());
                throw new NotDataFoundException("Not enough stock available");
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
                    return new NotDataFoundException("Sale note not found");
                });

        if (notaVenta.getBuyerAddress() == null || notaVenta.getBuyerAddress().getStatus() == AddressStatus.DELETED) {
            log.error("Address not found for user ID: " + notaVenta.getBuyer().getId() +
                    notaVenta.getBuyer().getId() + " in class NotaVentaServiceImpl.getPaymentInfo() method.");
            logsService.error("Address not found for user ID: " +
                    notaVenta.getBuyer().getId() + " in class NotaVentaServiceImpl.getPaymentInfo() method.");
            throw new NotDataFoundException("Address not found");
        }

        return sterumPayService.crearCargoCobro(StereumPagaDto.builder()
                    .country(request.getCountry())
                    .amount(notaVenta.getTotalGlobal().toString())
//                    .network(request.getNetwork())
                    .currency(request.getCurrency())
                    .chargeReason(request.getChargeReason())
                    .build(),
                request.getUserId());
    }

    @Override
    @Transactional
    public NotaVentaResponseDto updateOrderDetailStock(UpdateOrderDetailDto updateOrderDetailDto) {
        Long userId = updateOrderDetailDto.getUserId();
        Long productId = updateOrderDetailDto.getProductId();
        int quantity = updateOrderDetailDto.getQuantity();

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.error("Usuario con id " + userId + " no encontrado.");
            logsService.error("Usuario con id " + userId + " no encontrado.");
            return new IllegalArgumentException("Usuario con id " + userId + " no encontrado.");
        });
        Product product = productRepository.findById(productId).orElseThrow(() -> {
            log.error("Producto con id " + productId + " no encontrado.");
            logsService.error("Producto con id " + productId + " no encontrado.");
            return new IllegalArgumentException("Producto con id " + productId + " no encontrado.");
        });
        NotaVenta activeCart = notaVentaRepository.findByBuyer_IdAndEstadoVenta(userId).orElseThrow(() -> {
            log.error("Carrito activo no encontrado para el usuario con id " + userId);
            logsService.error("Carrito activo no encontrado para el usuario con id " + userId);
            return new IllegalArgumentException("Carrito activo no encontrado para el usuario con id " + userId);
        });
        OrderDetail orderDetail = orderDetailRepository.findByGroupIdAndProductId(activeCart.getId(), product.getId())
                .orElseThrow(() -> {
                    log.error("El producto no se encuentra en el carrito.");
                    logsService.error("El producto no se encuentra en el carrito.");
                    return new IllegalArgumentException("El producto no se encuentra en el carrito.");
                });

        orderDetailService.updateQuantityOrderDetail(new UpdateQuantityDetailDto(orderDetail.getId(), quantity));

        Double recalculatedTotal = orderDetailRepository.calculateTotalByNotaVenta(activeCart.getId());

        if (!Objects.equals(activeCart.getTotalGlobal(), recalculatedTotal)) {
            logsService.info("Updating totalGlobal for active cart of user ID: " + userId);
            activeCart.setTotalGlobal(recalculatedTotal != null ? recalculatedTotal : 0.0);
            notaVentaRepository.save(activeCart);
        }

        log.info("La cantidad del producto con id {} en el carrito del usuario con id {} se ha actualizado a {}",
                productId, userId, quantity);
        return convertToDtoWithDetails(activeCart);
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
                .buyerAddress(notaVenta.getBuyerAddress() != null ? notaVenta.getBuyerAddress().getId() : null)
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