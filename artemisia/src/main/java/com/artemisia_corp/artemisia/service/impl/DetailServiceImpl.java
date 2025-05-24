package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.*;
import com.artemisia_corp.artemisia.entity.dto.datail.*;
import com.artemisia_corp.artemisia.repository.*;
import com.artemisia_corp.artemisia.service.DetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetailServiceImpl implements DetailService {

    @Autowired
    private DetailRepository detailRepository;

    @Autowired
    private NotaVentaRepository notaVentaRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public List<DetailResponseDto> listAll() {
        return detailRepository.findAllDetails();
    }

    @Override
    public List<SimpleDetailDto> getAllSimpleDetails() {
        return detailRepository.findAllSimpleDetails();
    }

    @Override
    public List<SimpleDetailDto> getDetailsByNotaVenta(Long notaVentaId) {
        return detailRepository.findByNotaVentaId(notaVentaId);
    }

    @Override
    public void save(DetailRequestDto detailDto) {
        // Validar y obtener entidades relacionadas
        NotaVenta notaVenta = notaVentaRepository.findById(detailDto.getGroupId())
                .orElseThrow(() -> new RuntimeException("Nota de venta no encontrada con ID: " + detailDto.getGroupId()));

        Product product = productRepository.findById(detailDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + detailDto.getProductId()));

        Users seller = usersRepository.findById(detailDto.getSellerId())
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado con ID: " + detailDto.getSellerId()));

        // Calcular total si no viene en el DTO
        Double total = detailDto.getTotal() != null ? detailDto.getTotal() :
                product.getPrice() * detailDto.getQuantity();

        // Construir y guardar el detalle
        Detail detail = Detail.builder()
                .group(notaVenta)
                .product(product)
                .seller(seller)
                .productName(product.getName())
                .quantity(detailDto.getQuantity())
                .total(total)
                .build();

        detailRepository.save(detail);
    }

    @Override
    public void delete(DetailDeleteDto detailDto) {
        if (!detailRepository.existsById(detailDto.getId())) {
            throw new RuntimeException("Detalle no encontrado con ID: " + detailDto.getId());
        }
        detailRepository.deleteById(detailDto.getId());
    }

    @Override
    public void update(DetailUpdateDto detailDto) {
        Detail detail = detailRepository.findById(detailDto.getId())
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado con ID: " + detailDto.getId()));

        // Actualizar campos
        if (detailDto.getQuantity() != null) {
            detail.setQuantity(detailDto.getQuantity());
            // Recalcular total si cambia la cantidad
            if (detailDto.getTotal() == null) {
                detail.setTotal(detail.getProduct().getPrice() * detailDto.getQuantity());
            }
        }

        if (detailDto.getTotal() != null) {
            detail.setTotal(detailDto.getTotal());
        }

        if (detailDto.getProductId() != null) {
            Product product = productRepository.findById(detailDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + detailDto.getProductId()));
            detail.setProduct(product);
            detail.setProductName(product.getName());
        }

        if (detailDto.getSellerId() != null) {
            Users seller = usersRepository.findById(detailDto.getSellerId())
                    .orElseThrow(() -> new RuntimeException("Vendedor no encontrado con ID: " + detailDto.getSellerId()));
            detail.setSeller(seller);
        }

        detailRepository.save(detail);
    }
}