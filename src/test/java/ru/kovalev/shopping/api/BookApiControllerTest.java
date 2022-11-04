package ru.kovalev.shopping.api;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import ru.kovalev.shopping.BaseIntegrationTest;
import ru.kovalev.shopping.rest.dto.CreateBookRequest;
import ru.kovalev.shopping.rest.dto.ProductListDto;
import ru.kovalev.shopping.rest.dto.ProductResponseDto;

class BookApiControllerTest extends BaseIntegrationTest {
    public static final String BOOK_PATH = "/v0/book/";

    @Test
    void createBook() {
        var request = new CreateBookRequest()
                .name("Happy Rotter")
                .description("By Roan Joling")
                .quantity(132);

        var response =
                restTemplate.postForEntity(BOOK_PATH + "create", request, ProductResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .extracting(ProductResponseDto::getName, ProductResponseDto::getDescription,
                        ProductResponseDto::getAvailableQuantity)
                .containsExactly(request.getName(), request.getDescription(), request.getQuantity());

        var oneBook = restTemplate.getForEntity(BOOK_PATH + "list", ProductListDto.class);
        assertThat(oneBook.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(oneBook.getBody()).extracting(ProductListDto::getBooks).asList()
                .hasSize(1).first().isEqualTo(response.getBody());
    }

    @Test
    void listBooks() {
        var noBooks = restTemplate.getForEntity(BOOK_PATH + "list", ProductListDto.class);
        assertThat(noBooks.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(noBooks.getBody()).extracting(ProductListDto::getBooks).asList().isEmpty();
    }
}