package com.campusdeal.service.impl;

import com.campusdeal.common.BusinessException;
import com.campusdeal.common.PageResult;
import com.campusdeal.dto.request.AppealRequest;
import com.campusdeal.dto.request.ProductRequest;
import com.campusdeal.entity.Product;
import com.campusdeal.entity.ProductImage;
import com.campusdeal.entity.CreditRule;
import com.campusdeal.entity.SensitiveWord;
import com.campusdeal.repository.CartItemRepository;
import com.campusdeal.repository.CategoryRepository;
import com.campusdeal.repository.CreditRuleRepository;
import com.campusdeal.repository.FavoriteRepository;
import com.campusdeal.repository.OrderItemRepository;
import com.campusdeal.repository.ProductImageRepository;
import com.campusdeal.repository.ProductRepository;
import com.campusdeal.repository.SensitiveWordRepository;
import com.campusdeal.repository.UserRepository;
import com.campusdeal.service.MessageService;
import com.campusdeal.service.ProductService;
import com.campusdeal.service.SensitiveWordService;
import com.campusdeal.service.SystemLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final CartItemRepository cartItemRepository;
    private final FavoriteRepository favoriteRepository;
    private final OrderItemRepository orderItemRepository;
    private final MessageService messageService;
    private final SystemLogService systemLogService;
    private final SensitiveWordRepository sensitiveWordRepository;
    private final SensitiveWordService sensitiveWordService;
    private final CreditRuleRepository creditRuleRepository;
    private final UserRepository userRepository;
    private final HttpServletRequest request;

    public ProductServiceImpl(ProductRepository productRepository, ProductImageRepository productImageRepository,
                              CategoryRepository categoryRepository,
                              CartItemRepository cartItemRepository,
                              FavoriteRepository favoriteRepository,
                              OrderItemRepository orderItemRepository,
                              MessageService messageService,
                              SystemLogService systemLogService,
                              SensitiveWordRepository sensitiveWordRepository,
                              SensitiveWordService sensitiveWordService,
                              CreditRuleRepository creditRuleRepository,
                              UserRepository userRepository,
                              HttpServletRequest request) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
        this.cartItemRepository = cartItemRepository;
        this.favoriteRepository = favoriteRepository;
        this.orderItemRepository = orderItemRepository;
        this.messageService = messageService;
        this.systemLogService = systemLogService;
        this.sensitiveWordRepository = sensitiveWordRepository;
        this.sensitiveWordService = sensitiveWordService;
        this.creditRuleRepository = creditRuleRepository;
        this.userRepository = userRepository;
        this.request = request;
    }

    /**
     * 批量从 product_images 表加载图片，填充到商品的 images 与 coverImage 字段。
     * 解决种子数据未设置 cover_image、列表/详情接口未关联图片表导致前端图片不显示的问题。
     */
    private void enrichProductsWithImages(List<Product> products) {
        if (products == null || products.isEmpty()) return;
        List<Long> ids = products.stream().map(Product::getId).collect(Collectors.toList());
        Map<Long, List<ProductImage>> imageMap = productImageRepository.findByProductIdIn(ids).stream()
                .collect(Collectors.groupingBy(ProductImage::getProductId,
                        Collectors.mapping(Function.identity(), Collectors.toList())));
        for (Product p : products) {
            List<ProductImage> imgs = imageMap.getOrDefault(p.getId(), List.of());
            p.setImages(imgs);
            if ((p.getCoverImage() == null || p.getCoverImage().isBlank()) && !imgs.isEmpty()) {
                p.setCoverImage(imgs.get(0).getUrl());
            }
        }
    }

    @Override
    public PageResult<Product> listProducts(Integer categoryId, String keyword, int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<Product> productPage;

        if (keyword != null && !keyword.isBlank()) {
            productPage = productRepository.searchByKeyword(keyword, 1, pageable);
        } else if (categoryId != null && categoryId > 0) {
            productPage = productRepository.findByCategoryIdAndStatus(categoryId, 1, pageable);
        } else {
            productPage = productRepository.findByStatus(1, pageable);
        }

        List<Product> products = productPage.getContent();
        enrichProductsWithImages(products);

        return PageResult.of(products, productPage.getTotalElements(), page, size);
    }

    @Override
    @Transactional
    public Product getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        if (product.getStatus() != 1) {
            throw new BusinessException("商品已下架或未通过审核");
        }
        // 增加浏览量
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);
        // 触发 seller 懒加载
        if (product.getSeller() != null) {
            product.getSeller().getUsername();
        }
        if (product.getCategory() != null) {
            product.getCategory().getName();
        }
        // 加载商品图片
        enrichProductsWithImages(List.of(product));
        return product;
    }

    @Override
    @Transactional
    public Product createProduct(Long userId, ProductRequest request) {
        // 敏感词检测
        sensitiveWordService.checkContent(request.getTitle());
        sensitiveWordService.checkContent(request.getDescription());

        // 信誉分限制检查：从 credit_rules 表动态读取限制类规则
        List<CreditRule> appliedRules = creditRuleRepository.findByAppliedTrueOrderBySortOrderAsc();
        for (CreditRule rule : appliedRules) {
            if ("restriction".equals(rule.getCategory())) {
                int threshold = Math.abs(rule.getScore());
                com.campusdeal.entity.User user = userRepository.findById(userId).orElse(null);
                int currentScore = (user != null && user.getCreditScore() != null)
                        ? user.getCreditScore() : 100;
                if (currentScore < threshold) {
                    throw new BusinessException("您的信誉分（" + currentScore + "）低于" + threshold
                            + "分，暂无法发布商品。请通过成功交易提升信誉分。");
                }
                break;
            }
        }

        Product product = Product.builder()
                .sellerId(userId)
                .categoryId(request.getCategoryId())
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .conditionLevel(request.getConditionLevel())
                .campus(request.getCampus())
                .status(0) // 待审核
                .build();

        product = productRepository.save(product);

        // 保存图片
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (int i = 0; i < request.getImages().size(); i++) {
                ProductImage image = ProductImage.builder()
                        .productId(product.getId())
                        .url(request.getImages().get(i))
                        .sortOrder(i)
                        .build();
                productImageRepository.save(image);
                if (i == 0) {
                    product.setCoverImage(request.getImages().get(0));
                }
            }
            productRepository.save(product);
        }

        return product;
    }

    @Override
    @Transactional
    public Product updateProduct(Long userId, Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException("无权操作该商品");
        }

        // 敏感词检测（仅检测有变更的字段）
        if (request.getTitle() != null) {
            sensitiveWordService.checkContent(request.getTitle());
            product.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            sensitiveWordService.checkContent(request.getDescription());
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getOriginalPrice() != null) product.setOriginalPrice(request.getOriginalPrice());
        if (request.getConditionLevel() != null) product.setConditionLevel(request.getConditionLevel());
        if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());
        if (request.getCampus() != null) product.setCampus(request.getCampus());

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            productImageRepository.deleteByProductId(productId);
            for (int i = 0; i < request.getImages().size(); i++) {
                ProductImage image = ProductImage.builder()
                        .productId(productId)
                        .url(request.getImages().get(i))
                        .sortOrder(i)
                        .build();
                productImageRepository.save(image);
            }
            product.setCoverImage(request.getImages().get(0));
        }

        // 编辑后重新进入待审核状态
        product.setStatus(0);
        product.setAuditRemark(null);

        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long userId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException("无权操作该商品");
        }

        product.setStatus(2); // 下架
        productRepository.save(product);
    }

    @Override
    public PageResult<Product> getHotProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "favoriteCount"));
        Page<Product> productPage = productRepository.findByStatus(1, pageable);
        List<Product> products = productPage.getContent();
        enrichProductsWithImages(products);
        return PageResult.of(products, productPage.getTotalElements(), page, size);
    }

    @Override
    public PageResult<Product> getLatestProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> productPage = productRepository.findByStatus(1, pageable);
        List<Product> products = productPage.getContent();
        enrichProductsWithImages(products);
        return PageResult.of(products, productPage.getTotalElements(), page, size);
    }

    @Override
    public PageResult<Product> getSimilarProducts(Long productId, int size) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        Pageable pageable = PageRequest.of(0, size);
        List<Product> similar = productRepository.findSimilarProducts(product.getCategoryId(), 1, productId, pageable);
        enrichProductsWithImages(similar);
        return PageResult.of(similar, similar.size(), 1, size);
    }

    @Override
    public PageResult<Product> getMyProducts(Long userId, Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> productPage;
        if (status != null) {
            productPage = productRepository.findBySellerIdAndStatus(userId, status, pageable);
        } else {
            productPage = productRepository.findBySellerId(userId, pageable);
        }
        List<Product> products = productPage.getContent();
        enrichProductsWithImages(products);
        return PageResult.of(products, productPage.getTotalElements(), page, size);
    }

    @Override
    public Product getMyProductDetail(Long userId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException("无权查看该商品");
        }
        // 触发懒加载
        if (product.getCategory() != null) {
            product.getCategory().getName();
        }
        // 加载商品图片
        enrichProductsWithImages(List.of(product));
        return product;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Product> getAdminProducts(Integer status, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Product> productPage;

        if (keyword != null && !keyword.isBlank()) {
            productPage = productRepository.searchByKeyword(keyword, status, pageable);
        } else if (status != null) {
            productPage = productRepository.findByStatus(status, pageable);
        } else {
            // 默认排除已删除（status=4）的商品
            productPage = productRepository.findByStatusNot(4, pageable);
        }

        // 触发懒加载关联
        for (Product p : productPage.getContent()) {
            if (p.getSeller() != null) p.getSeller().getUsername();
            if (p.getCategory() != null) p.getCategory().getName();
        }

        List<Product> products = productPage.getContent();
        enrichProductsWithImages(products);
        return PageResult.of(products, productPage.getTotalElements(), page, size);
    }

    @Override
    public void auditProduct(Long productId, Integer status, String remark) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 审核通过时进行规则校验
        if (status != null && status == 1) {
            String validationError = validateProductForApproval(product);
            if (validationError != null) {
                // 自动驳回
                product.setStatus(2);
                product.setAuditRemark("自动驳回：" + validationError);
                productRepository.save(product);
                String ip = getClientIp();
                systemLogService.saveLog("商品审核",
                    "自动驳回商品\"" + product.getTitle() + "\"(ID:" + productId + ")，原因：" + validationError,
                    "管理员", ip);
                throw new BusinessException(validationError);
            }
        }

        product.setStatus(status);
        product.setAuditRemark(remark);
        productRepository.save(product);

        // 发送系统通知给发布者
        if (status != null && status != 1) {
            String rejectReason = (remark != null && !remark.isBlank()) ? "，原因：" + remark : "";
            messageService.sendSystemMessage(product.getSellerId(),
                "您的商品「" + product.getTitle() + "」已被驳回" + rejectReason + "。", "audit", productId);
        } else if (status != null && status == 1) {
            messageService.sendSystemMessage(product.getSellerId(),
                "您的商品「" + product.getTitle() + "」已通过审核，现已上架。", "audit", productId);
        }

        String actionLabel = status == 1 ? "通过" : "驳回";
        String ip = getClientIp();
        systemLogService.saveLog("商品审核",
            "审核" + actionLabel + "商品\"" + product.getTitle() + "\"(ID:" + productId + ")，备注：" + (remark != null ? remark : "无"),
            "管理员", ip);
    }

    @Override
    public void approveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 规则校验
        String validationError = validateProductForApproval(product);
        if (validationError != null) {
            throw new BusinessException(validationError);
        }

        product.setStatus(1);
        product.setAuditRemark(null);
        productRepository.save(product);

        // 发送系统消息通知卖家
        messageService.sendSystemMessage(product.getSellerId(),
            "您的商品「" + product.getTitle() + "」已通过审核，现已上架。", "audit", productId);

        String ip = getClientIp();
        systemLogService.saveLog("商品审核",
            "审核通过商品\"" + product.getTitle() + "\"(ID:" + productId + ")",
            "管理员", ip);
    }

    @Override
    public void rejectProduct(Long productId, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        product.setStatus(2);
        product.setAuditRemark(reason);
        productRepository.save(product);

        // 发送系统消息通知卖家
        String reasonText = (reason != null && !reason.isBlank()) ? "，原因：" + reason : "";
        messageService.sendSystemMessage(product.getSellerId(),
            "您的商品「" + product.getTitle() + "」已被驳回" + reasonText + "。", "audit", productId);

        String ip = getClientIp();
        systemLogService.saveLog("商品审核",
            "驳回商品\"" + product.getTitle() + "\"(ID:" + productId + ")，原因：" + (reason != null ? reason : "无"),
            "管理员", ip);
    }

    /**
     * 审核规则校验：返回 null 表示通过，返回非 null 字符串为错误原因
     */
    private String validateProductForApproval(Product product) {
        // 1. 标题不能为空
        if (product.getTitle() == null || product.getTitle().isBlank()) {
            return "商品标题不能为空";
        }
        // 2. 从数据库查询已应用的敏感词，进行敏感词匹配
        List<SensitiveWord> appliedWords = sensitiveWordRepository.findByAppliedTrue();
        if (!appliedWords.isEmpty()) {
            String title = product.getTitle();
            String description = product.getDescription();
            for (SensitiveWord sw : appliedWords) {
                String word = sw.getWord();
                if (title != null && title.contains(word)) {
                    return "商品包含敏感词，自动驳回";
                }
                if (description != null && description.contains(word)) {
                    return "商品包含敏感词，自动驳回";
                }
            }
        }
        // 3. 描述不能为空
        if (product.getDescription() == null || product.getDescription().isBlank()) {
            return "商品描述不能为空";
        }
        // 4. 售价必须大于0
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return "售价必须大于0";
        }
        // 5. 图片不能为空
        if (product.getCoverImage() == null || product.getCoverImage().isBlank()) {
            return "商品图片不能为空";
        }
        // 6. 分类必须合法（存在于数据库）
        if (product.getCategoryId() == null ||
            !categoryRepository.existsById(product.getCategoryId())) {
            return "商品分类不合法";
        }
        return null; // 通过
    }

    @Override
    public void offShelfProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        product.setStatus(3); // 下架（与已驳回 status=2 分离）
        productRepository.save(product);

        String ip = getClientIp();
        systemLogService.saveLog("商品下架", "下架商品\"" + product.getTitle() + "\"(ID:" + productId + ")", "管理员", ip);
    }

    @Override
    public void onShelfProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        // 允许以下状态的商品重新上架：已驳回(2)、已下架(3)、已收回(5)
        if (product.getStatus() != 2 && product.getStatus() != 3 && product.getStatus() != 5) {
            throw new BusinessException("该商品状态不允许上架");
        }
        product.setStatus(1); // 恢复在售
        product.setAuditRemark(null);
        productRepository.save(product);

        String ip = getClientIp();
        systemLogService.saveLog("商品上架", "重新上架商品\"" + product.getTitle() + "\"(ID:" + productId + ")", "管理员", ip);
    }

    @Override
    @Transactional
    public Product adminUpdateProduct(Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (request.getTitle() != null) product.setTitle(request.getTitle());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getOriginalPrice() != null) product.setOriginalPrice(request.getOriginalPrice());
        if (request.getConditionLevel() != null) product.setConditionLevel(request.getConditionLevel());
        if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());
        if (request.getCampus() != null) product.setCampus(request.getCampus());

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            productImageRepository.deleteByProductId(productId);
            for (int i = 0; i < request.getImages().size(); i++) {
                ProductImage image = ProductImage.builder()
                        .productId(productId)
                        .url(request.getImages().get(i))
                        .sortOrder(i)
                        .build();
                productImageRepository.save(image);
            }
            product.setCoverImage(request.getImages().get(0));
        }

        // 管理员编辑后保持当前状态，不重置审核
        Product saved = productRepository.save(product);

        String ip = getClientIp();
        systemLogService.saveLog("商品编辑",
            "管理员编辑商品\"" + saved.getTitle() + "\"(ID:" + productId + ")", "管理员", ip);

        return saved;
    }

    @Override
    @Transactional
    public void adminDeleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (product.getStatus() == 4) {
            throw new BusinessException("商品已被删除");
        }

        // 软删除：将状态置为 4（已删除），不清理关联数据
        product.setStatus(4);
        productRepository.save(product);

        String ip = getClientIp();
        systemLogService.saveLog("商品删除",
            "管理员软删除商品\"" + product.getTitle() + "\"(ID:" + productId + ")", "管理员", ip);
    }

    private Pageable buildPageable(int page, int size, String sort) {
        Sort sortObj;
        if ("price_asc".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price_desc".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "price");
        } else if ("hot".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "favoriteCount");
        } else {
            sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return PageRequest.of(page - 1, size, sortObj);
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    // ==================== 申诉相关 ====================

    @Override
    @Transactional
    public void submitAppeal(Long userId, Long productId, AppealRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException("无权操作该商品");
        }

        // 只有已驳回(status=2)且未申诉(appeal_status=0)才能申诉
        if (product.getStatus() != 2) {
            throw new BusinessException("只有已驳回的商品才能申诉");
        }
        if (product.getAppealStatus() != null && product.getAppealStatus() != 0) {
            throw new BusinessException("该商品已提交过申诉，不可重复申诉");
        }

        // 敏感词检测
        if (request.getTitle() != null) {
            sensitiveWordService.checkContent(request.getTitle());
            product.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            sensitiveWordService.checkContent(request.getDescription());
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getOriginalPrice() != null) product.setOriginalPrice(request.getOriginalPrice());
        if (request.getConditionLevel() != null) product.setConditionLevel(request.getConditionLevel());
        if (request.getCampus() != null) product.setCampus(request.getCampus());

        // 更新图片
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            productImageRepository.deleteByProductId(productId);
            for (int i = 0; i < request.getImages().size(); i++) {
                ProductImage image = ProductImage.builder()
                        .productId(productId)
                        .url(request.getImages().get(i))
                        .sortOrder(i)
                        .build();
                productImageRepository.save(image);
            }
            product.setCoverImage(request.getImages().get(0));
        }

        product.setAppealReason(request.getAppealReason());
        product.setAppealStatus(1); // 申诉中
        product.setAppealTime(LocalDateTime.now());
        product.setAppealRemark(null);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void approveAppeal(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (product.getAppealStatus() == null || product.getAppealStatus() != 1) {
            throw new BusinessException("该商品当前未处于申诉中状态");
        }

        // 申诉通过：商品状态恢复为在售(1)，申诉状态设为通过(2)
        product.setStatus(1);
        product.setAppealStatus(2);
        product.setAuditRemark(null);
        productRepository.save(product);

        // 通知卖家
        messageService.sendSystemMessage(product.getSellerId(),
                "您的申诉已通过，商品「" + product.getTitle() + "」已重新上架。", "appeal", productId);

        String ip = getClientIp();
        systemLogService.saveLog("申诉处理",
                "申诉通过，商品\"" + product.getTitle() + "\"(ID:" + productId + ") 重新上架",
                "管理员", ip);
    }

    @Override
    @Transactional
    public void rejectAppeal(Long productId, String remark) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (product.getAppealStatus() == null || product.getAppealStatus() != 1) {
            throw new BusinessException("该商品当前未处于申诉中状态");
        }

        // 申诉驳回：保持 status=2（已驳回），申诉状态设为驳回(3)
        product.setAppealStatus(3);
        product.setAppealRemark(remark);
        productRepository.save(product);

        // 通知卖家
        String reasonText = (remark != null && !remark.isBlank()) ? "，原因：" + remark : "";
        messageService.sendSystemMessage(product.getSellerId(),
                "您的申诉已被驳回" + reasonText + "。", "appeal", productId);

        String ip = getClientIp();
        systemLogService.saveLog("申诉处理",
                "申诉驳回，商品\"" + product.getTitle() + "\"(ID:" + productId + ")，原因：" + (remark != null ? remark : "无"),
                "管理员", ip);
    }
}
