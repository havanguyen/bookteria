package com.hanguyen.product_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.hanguyen.product_service.dto.event.TypeEvent;
import com.hanguyen.product_service.dto.request.ProductRequest;
import com.hanguyen.product_service.dto.response.ProductPageResponse;
import com.hanguyen.product_service.dto.response.ProductResponse;
import com.hanguyen.product_service.entity.Author;
import com.hanguyen.product_service.entity.Category;
import com.hanguyen.product_service.entity.Product;
import com.hanguyen.product_service.entity.Publisher;
import com.hanguyen.product_service.exception.AppException;
import com.hanguyen.product_service.exception.ErrorCode;
import com.hanguyen.product_service.mapper.ProductMapper;
import com.hanguyen.product_service.repository.AuthorRepository;
import com.hanguyen.product_service.repository.CategoryRepository;
import com.hanguyen.product_service.repository.ProductRepository;
import com.hanguyen.product_service.repository.PublisherRepository;
import com.hanguyen.product_service.utils.TestUtils;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductEventProducerService productEventProducerService;

    @Mock
    private RedisTemplate<String, Object> stringObjectRedisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @InjectMocks
    private ProductService productService;

    private ProductRequest productRequest;
    private ProductResponse productResponse;
    private Product product;
    private Author author;
    private Publisher publisher;
    private Category category;

    @BeforeEach
    void initData() {
        productRequest = TestUtils.getObject("data/product/create_product_request.json", ProductRequest.class);
        productResponse = TestUtils.getObject("data/product/product_response.json", ProductResponse.class);

        author = new Author();
        author.setId("a1");

        publisher = new Publisher();
        publisher.setId("p1");

        category = new Category();
        category.setId("c1");

        product = Product.builder()
                .id("prod1")
                .title("Clean Code")
                .author(author)
                .publisher(publisher)
                .categories(Collections.singleton(category))
                .build();

        ReflectionTestUtils.setField(productService, "minTTL", 100);
        ReflectionTestUtils.setField(productService, "maxTTL", 200);
    }

    @Test
    void createProduct_validRequest_success() {
        when(productMapper.toProduct(any(ProductRequest.class))).thenReturn(product);
        when(authorRepository.findById(anyString())).thenReturn(Optional.of(author));
        when(publisherRepository.findById(anyString())).thenReturn(Optional.of(publisher));
        when(categoryRepository.findAllById(any())).thenReturn(Collections.singletonList(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

        // Mock Redis
        when(stringObjectRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members(anyString())).thenReturn(null);

        ProductResponse response = productService.createProduct(productRequest);

        assertNotNull(response);
        assertEquals("prod1", response.getId());
        verify(productEventProducerService, times(1)).sendProductEvent(any(Product.class), eq(TypeEvent.CREATE));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void update_validId_success() {
        when(productRepository.findById(anyString())).thenReturn(Optional.of(product));
        // Author and Publisher are not updated if IDs match, so no need to mock their
        // repositories here
        when(categoryRepository.findAllById(any())).thenReturn(Collections.singletonList(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

        // Mock Redis
        when(stringObjectRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members(anyString())).thenReturn(null);

        ProductResponse response = productService.update("prod1", productRequest);

        assertNotNull(response);
        verify(productEventProducerService, times(1)).sendProductEvent(any(Product.class), eq(TypeEvent.UPDATE));
        verify(stringObjectRedisTemplate, times(1)).delete(anyString());
    }

    @Test
    void getAll_cacheMiss_success() {
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        when(productRepository.findAllWithRelations(any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

        when(stringObjectRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null); // Cache miss
        when(stringObjectRedisTemplate.opsForSet()).thenReturn(setOperations);

        Page<ProductResponse> response = productService.getAll(PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(valueOperations, times(1)).set(anyString(), any(), any()); // Verify cache set
    }

    @Test
    void getById_cacheMiss_success() {
        when(stringObjectRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null); // Cache miss

        when(productRepository.findById(anyString())).thenReturn(Optional.of(product));
        when(productMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

        ProductResponse response = productService.getById("prod1");

        assertNotNull(response);
        assertEquals("prod1", response.getId());
        verify(valueOperations, times(1)).set(anyString(), any(), any()); // Verify cache set
    }

    @Test
    void delete_validId_success() {
        when(productRepository.findById(anyString())).thenReturn(Optional.of(product));
        // Mock Redis
        when(stringObjectRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members(anyString())).thenReturn(null);

        productService.delete("prod1");

        verify(productEventProducerService, times(1)).sendProductEvent(any(Product.class), eq(TypeEvent.DELETE));
        verify(productRepository, times(1)).deleteById("prod1");
        verify(stringObjectRedisTemplate, times(1)).delete(anyString());
    }
}
