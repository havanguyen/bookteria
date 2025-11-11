package com.hanguyen.product_service.service;

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
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE )
public class ProductService {

    final AuthorRepository authorRepository;
    final CategoryRepository categoryRepository;
    final ProductRepository productRepository;
    final PublisherRepository publisherRepository;
    final ProductMapper productMapper;

    final ProductEventProducerService productEventProducerService;

    final RedisTemplate<String , Object> stringObjectRedisTemplate;

    String productKeyPrefix = "product::";
    String productListKeyPrefix = "product_list::";
    String productListKeySet = "product_list_keys";

    int minTTL = 1200 ;
    int maxTTL = 1800 ;


    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest){
        Product product = productMapper.toProduct(productRequest);

        Author author = authorRepository.findById(productRequest.getAuthorId())
                .orElseThrow(() -> new AppException(ErrorCode.ID_AUTHOR_NOT_FOUND));

        Publisher publisher = publisherRepository.findById(productRequest.getPublisherId())
                .orElseThrow(() -> new AppException(ErrorCode.ID_PUBLISH_NOT_FOUND));

        product.setAuthor(author);
        product.setPublisher(publisher);

        if (productRequest.getCategoryIds() != null && !productRequest.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(productRequest.getCategoryIds()));
            product.setCategories(categories);
        }

        Product savedProduct = productRepository.save(product);

        productEventProducerService.sendProductEvent(savedProduct , TypeEvent.CREATE);

        clearProductListCaches();

        return productMapper.toProductResponse(savedProduct);
    }

    @Transactional
    public ProductResponse update(String id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ID_PRODUCT_NOT_FOUND));

        product.setTitle(request.getTitle());
        product.setSlug(request.getSlug());
        product.setIsbn(request.getIsbn());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setPublicationDate(request.getPublicationDate());
        product.setPageCount(request.getPageCount());
        product.setBasePrice(request.getBasePrice());
        product.setSalePrice(request.getSalePrice());
        product.setSaleStartDate(request.getSaleStartDate());
        product.setSaleEndDate(request.getSaleEndDate());
        product.setAttributes(request.getAttributes());

        if (product.getAuthor() == null || !product.getAuthor().getId().equals(request.getAuthorId())) {
            Author author = authorRepository.findById(request.getAuthorId())
                    .orElseThrow(() -> new AppException(ErrorCode.ID_AUTHOR_NOT_FOUND));
            product.setAuthor(author);
        }

        if (product.getPublisher() == null || !product.getPublisher().getId().equals(request.getPublisherId())) {
            Publisher publisher = publisherRepository.findById(request.getPublisherId())
                    .orElseThrow(() -> new AppException(ErrorCode.ID_AUTHOR_NOT_FOUND));
            product.setPublisher(publisher);
        }

        if (request.getCategoryIds() != null) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
            product.setCategories(categories);
        }

        Product savedProduct = productRepository.save(product);

        productEventProducerService.sendProductEvent(savedProduct , TypeEvent.UPDATE);

        String key = productKeyPrefix + id ;
        stringObjectRedisTemplate.delete(key);
        clearProductListCaches();
        log.info("Delete cache with id {}" , id);
        return productMapper.toProductResponse(savedProduct);
    }

    public Page<ProductResponse> getAll(Pageable pageable) {
        String key = productListKeyPrefix + generateProductCacheKey(pageable);

        ProductPageResponse cachedPage = null;
        try {
            cachedPage = (ProductPageResponse) stringObjectRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Error reading from Redis cache: {}", e.getMessage());
        }


        if (cachedPage != null) {
            log.info("Cache hit for getProducts list: {}", key);
            Pageable pageRequest = PageRequest.of(cachedPage.getPageNo(), cachedPage.getPageSize(), pageable.getSort());
            return new PageImpl<>(cachedPage.getContent(), pageRequest, cachedPage.getTotalElements());
        }

        log.info("Cache miss for getProducts list. Fetching from DB");

        Page<ProductResponse> productPage = productRepository.findAllWithRelations(pageable)
                .map(productMapper::toProductResponse);

        ProductPageResponse pageToCache = new ProductPageResponse(
                productPage.getContent(),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );

        int randomTTL = ThreadLocalRandom.current().nextInt(minTTL , maxTTL + 1);

        try {
            stringObjectRedisTemplate.opsForValue().set(key , pageToCache , Duration.ofSeconds(randomTTL));
            stringObjectRedisTemplate.opsForSet().add(productListKeySet , key);
        } catch (Exception e) {
            log.warn("Error writing to Redis cache: {}", e.getMessage());
        }

        return productPage;
    }

    public ProductResponse getById(String id) {

        String key = productKeyPrefix + id ;

        ProductResponse productResponse= (ProductResponse) stringObjectRedisTemplate.opsForValue().get(key);

        if(productResponse != null) {
            log.info("Cache hit for getProduct: {}", id);
            return productResponse;
        }

        productResponse = productRepository.findById(id)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new AppException(ErrorCode.ID_PRODUCT_NOT_FOUND));

        int randomTTL = ThreadLocalRandom.current().nextInt(minTTL , maxTTL + 1);

        stringObjectRedisTemplate.opsForValue().set(key , productResponse , Duration.ofSeconds(randomTTL));

        return productResponse;
    }

    @Transactional
    public void delete(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ID_PRODUCT_NOT_FOUND));

        productEventProducerService.sendProductEvent(product , TypeEvent.DELETE);
        productRepository.deleteById(id);
        String key = productKeyPrefix + id ;
        stringObjectRedisTemplate.delete(key);
        clearProductListCaches();
    }

    private String generateProductCacheKey(Pageable pageable) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("page=").append(pageable.getPageNumber())
                .append(":size=").append(pageable.getPageSize());
        if (pageable.getSort().isSorted()) {
            keyBuilder.append(":sort=");
            pageable.getSort().forEach(order ->
                    keyBuilder.append(order.getProperty())
                            .append("-")
                            .append(order.getDirection().name())
                            .append("_")
            );
        }
        return keyBuilder.toString();
    }

    private void clearProductListCaches() {
        log.info("Clearing all product list caches");
        Set<Object> keys = stringObjectRedisTemplate.opsForSet().members(productListKeySet);
        if (keys != null && !keys.isEmpty()) {
            keys.add(productListKeySet);
            Set<String> keyStrings = keys.stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
            stringObjectRedisTemplate.delete(keyStrings);
        }
    }

}
