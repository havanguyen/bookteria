package com.hanguyen.product_service.service;

import com.hanguyen.product_service.dto.event.TypeEvent;
import com.hanguyen.product_service.dto.request.CategoryRequest;
import com.hanguyen.product_service.dto.response.CategoryResponse;
import com.hanguyen.product_service.entity.Category;
import com.hanguyen.product_service.entity.Product;
import com.hanguyen.product_service.exception.AppException;
import com.hanguyen.product_service.exception.ErrorCode;
import com.hanguyen.product_service.mapper.CategoryMapper;
import com.hanguyen.product_service.repository.CategoryRepository;
import com.hanguyen.product_service.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class CategoryService {

     CategoryRepository categoryRepository;
     CategoryMapper categoryMapper;
     ProductEventProducerService productEventProducerService;
     ProductRepository productRepository;

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Category category = categoryMapper.toCategory(request);

        if (request.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.ID_CATEGORY_NOT_FOUND));
            category.setParentCategory(parent);
        }

        category = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

    @Transactional
    public CategoryResponse update(String id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ID_CATEGORY_NOT_FOUND));

        List<Product> affectedProducts = productRepository.findAllByCategories_Id(id);
        category.setName(request.getName());
        category.setSlug(request.getSlug());

        if (request.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.ID_CATEGORY_NOT_FOUND));
            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }

        category = categoryRepository.save(category);

        for (Product product : affectedProducts) {
            product = productRepository.findById(product.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.ID_PRODUCT_NOT_FOUND));
            productEventProducerService.sendProductEvent(product, TypeEvent.UPDATE);
        }
        return categoryMapper.toCategoryResponse(category);
    }

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getById(String id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toCategoryResponse)
                .orElseThrow(() -> new AppException(ErrorCode.ID_CATEGORY_NOT_FOUND));
    }

    @Transactional
    public void delete(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new AppException(ErrorCode.ID_CATEGORY_NOT_FOUND);
        }

        List<Product> affectedProducts = productRepository.findAllByCategories_Id(id);
        for (Product product : affectedProducts) {
            Set<Category> categories = product.getCategories();
            categories.removeIf(c -> c.getId().equals(id));
            product.setCategories(categories);
            productRepository.save(product);

            productEventProducerService.sendProductEvent(product, TypeEvent.UPDATE);
        }
        categoryRepository.deleteById(id);
    }
}