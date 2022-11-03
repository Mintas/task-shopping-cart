package ru.kovalev.shopping.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kovalev.shopping.config.SwaggerConfig;
import ru.kovalev.shopping.mapper.ProductDtoMapper;
import ru.kovalev.shopping.repository.ProductRepository;
import ru.kovalev.shopping.rest.api.BookApi;
import ru.kovalev.shopping.rest.dto.CreateBookRequest;
import ru.kovalev.shopping.rest.dto.ProductListDto;
import ru.kovalev.shopping.rest.dto.ProductResponseDto;

@RestController
@RequestMapping("v0")
@SecurityRequirement(name = SwaggerConfig.SECURED)
@RequiredArgsConstructor
public class BookApiController implements BookApi {
    private final ProductRepository productRepository;
    private final ProductDtoMapper productDtoMapper;

    @Override
    public ResponseEntity<ProductResponseDto> createBook(CreateBookRequest createBookRequest) {
        var product = productDtoMapper.toEntity(createBookRequest);
        var created = productRepository.save(product);
        return ResponseEntity.ok(productDtoMapper.toDto(created));
    }

    @Override
    public ResponseEntity<ProductListDto> listBooks() {
        var allActive = productRepository.findAllActive();
        return ResponseEntity.ok(productDtoMapper.toListDto(allActive));
    }
}
