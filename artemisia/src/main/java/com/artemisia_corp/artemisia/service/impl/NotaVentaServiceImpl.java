package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.*;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.*;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailRequestDto;
import com.artemisia_corp.artemisia.entity.dto.order_detail.OrderDetailResponseDto;
import com.artemisia_corp.artemisia.entity.dto.product.ProductResponseDto;
import com.artemisia_corp.artemisia.entity.enums.PaintingCategory;
import com.artemisia_corp.artemisia.entity.enums.PaintingTechnique;
import com.artemisia_corp.artemisia.entity.enums.ProductStatus;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import com.artemisia_corp.artemisia.repository.*;
import com.artemisia_corp.artemisia.service.LogsService;
import com.artemisia_corp.artemisia.service.NotaVentaService;
import com.artemisia_corp.artemisia.service.OrderDetailService;
import com.artemisia_corp.artemisia.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotaVentaServiceImpl implements NotaVentaService {
    @Autowired
    private NotaVentaRepository notaVentaRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ProductService productService;
    @Autowired
    private LogsService logsService;

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
                .image(preProduct.getImage())
                .category(PaintingCategory.valueOf(preProduct.getCategory()))
                .seller(userRepository.getReferenceById(preProduct.getSellerId()))
                .build();
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

        List<OrderDetailResponseDto> detalles = orderDetailService.getOrderDetailsByNotaVenta(id);

        // Reduce stock for each product
        for (OrderDetailResponseDto detalle : detalles) {
            productService.reduceStock(detalle.getProductId(), detalle.getQuantity());
            logsService.info("Reduced stock for product ID: " + detalle.getProductId() +
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
}