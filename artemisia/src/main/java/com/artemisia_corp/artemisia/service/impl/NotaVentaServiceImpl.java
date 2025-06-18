package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.*;
import com.artemisia_corp.artemisia.entity.dto.address.AddressResponseDto;
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
import com.artemisia_corp.artemisia.repository.*;
import com.artemisia_corp.artemisia.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Autowired
    private AddressService addressService;

    @Override
    public List<NotaVentaResponseDto> getAllNotasVenta() {
        logsService.info("Fetching all sales notes");
        return notaVentaRepository.findAllNotaVentas();
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
        NotaVenta notaVenta = notaVentaRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("Sale note not found with ID: " + id);
                    throw new RuntimeException("Sale note not found");
                });

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
            detailDto.setGroupId(id);
            OrderDetailResponseDto existingDetail = existingDetails.stream()
                    .filter(d -> d.getProductId().equals(detailDto.getProductId()))
                    .findFirst()
                    .orElse(null);

            if (existingDetail != null) {
                detailDto.setGroupId(id);
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
        if (!notaVentaRepository.existsById(id)) {
            logsService.error("Sale note not found with ID: " + id);
            throw new RuntimeException("Sale note not found");
        }

        logsService.info("Deleting Details affiliated with ID: " + id);
        orderDetailService.getOrderDetailsByNotaVenta(id).forEach(detail -> {
            orderDetailService.deleteOrderDetail(detail.getId());
        });

        notaVentaRepository.deleteById(id);
        logsService.info("Sale note deleted with ID: " + id);
    }

    @Override
    @Transactional
    public void completeNotaVenta(Long id) {
        NotaVenta notaVenta = notaVentaRepository.findById(id)
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
    public List<NotaVentaResponseDto> getNotasVentaByEstado(VentaEstado estado) {
        logsService.info("Fetching sale notes with status: " + estado);
        return notaVentaRepository.findByEstadoVenta(estado).stream()
                .map(this::convertToDtoWithDetails)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotaVentaResponseDto> getCompletedSalesByUser(Long userId) {
        return notaVentaRepository.findAllNotaVentasByBuyer_Id(userId);
    }

    @Override
    public void ingresarIdTransaccion(String idTransaccion, Long notaVentaId) {
        NotaVenta notaVenta = notaVentaRepository.getReferenceById(notaVentaId);

        notaVenta.setIdTransaccion(idTransaccion);
        notaVentaRepository.save(notaVenta);
        logsService.info("Sale note updated with ID: " + notaVentaId);
    }

    @Override
    public void obtenerRespuestaTransaccion(RespuestaVerificacionNotaVentaDto respuesta){
        NotaVenta notaVenta = notaVentaRepository.findNotaVentaByIdTransaccion(respuesta.getId());

        EstadoResponseDto estado = sterumPayService.obtenerEstadoCobro(notaVenta.getIdTransaccion());

        if ("PAYED".equals(estado.getStatus())) {
            logsService.info("Transaction completed successfully for ID: " + notaVenta.getId());
            notaVenta.setEstadoVenta(VentaEstado.PAYED);
            notaVentaRepository.save(notaVenta);
            completeNotaVenta(notaVenta.getId());
        } else if ("CANCELED".equals(estado.getStatus())) {
            logsService.info("Transaction canceled for ID: " + notaVenta.getId());
            deleteNotaVenta(notaVenta.getId());
        } else {
            logsService.warning("Transaction has not been finalized for ID: " + notaVenta.getId());
        }
    }

    @Override
    public NotaVentaResponseDto getActiveCartByUserId(Long userId) {
        NotaVenta notaVenta = notaVentaRepository.findByBuyer_IdAndEstadoVenta(userId, VentaEstado.ON_CART)
                .orElseThrow(() -> {
                    logsService.error("No active cart found for user ID: " + userId);
                    throw new RuntimeException("No active cart found");
                });

        return convertToDtoWithDetails(notaVenta);
    }

    @Override
    @Transactional
    public NotaVentaResponseDto addProductToCart(AddToCartDto addToCartDto) {
        NotaVenta cart = notaVentaRepository.findByBuyer_IdAndEstadoVenta(addToCartDto.getUserId(), VentaEstado.ON_CART)
                .orElseGet(() -> {
                    User buyer = userRepository.findById(addToCartDto.getUserId())
                            .orElseThrow(() -> {
                                logsService.error("User not found with ID: " + addToCartDto.getUserId());
                                throw new RuntimeException("User not found");
                            });

                    List<AddressResponseDto> addresses = addressService.getAddressesByUser(addToCartDto.getUserId());
                    if (addresses.isEmpty()) {
                        logsService.error("User has no addresses");
                        throw new RuntimeException("User must have at least one address");
                    }

                    Address defaultAddress = addressRepository.
                            getById(addresses.getFirst().getAddressId());

                    log.info("Address: {}", defaultAddress);

                    NotaVenta newCart = NotaVenta.builder()
                            .buyer(buyer)
                            .buyerAddress(defaultAddress)
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

        Optional<OrderDetail> existingDetail = orderDetailRepository.findByGroup_IdAndProduct_ProductId(cart.getId(), product.getProductId());

        if (existingDetail.isPresent()) {
            OrderDetail detail = existingDetail.get();
            int newQuantity = detail.getQuantity() + addToCartDto.getQuantity();

            if (newQuantity > product.getStock()) {
                logsService.error("Not enough stock for product ID: " + product.getProductId());
                throw new RuntimeException("Not enough stock available");
            }

            detail.setQuantity(newQuantity);
            detail.setTotal(newQuantity * product.getPrice());
            orderDetailRepository.save(detail);
        } else {
            if (addToCartDto.getQuantity() > product.getStock()) {
                logsService.error("Not enough stock for product ID: " + product.getProductId());
                throw new RuntimeException("Not enough stock available");
            }

            OrderDetailRequestDto detailDto = OrderDetailRequestDto.builder()
                    .groupId(cart.getId())
                    .productId(product.getProductId())
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
                .buyerAddress(notaVenta.getBuyerAddress().getAddressId())
                .estadoVenta(notaVenta.getEstadoVenta().name())
                .totalGlobal(notaVenta.getTotalGlobal())
                .date(notaVenta.getDate())
                .detalles(detalles)
                .build();
    }

    private Product convertToProduct(Long productId, ProductResponseDto preProduct) {
        return Product.builder()
                .productId(productId)
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