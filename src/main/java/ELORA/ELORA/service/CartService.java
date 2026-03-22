package ELORA.ELORA.service;

import ELORA.ELORA.dto.request.CartRequest;
import ELORA.ELORA.entity.*;
import ELORA.ELORA.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CartService {

    @Autowired private CartRepository cartRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository variantRepository;

    // 1. Thêm sản phẩm vào giỏ hàng (Xử lý cả biến thể và cộng dồn)
    @Transactional
    public Cart addToCart(CartRequest request) {
        // Kiểm tra tồn kho trước khi thêm
        validateStock(request.getProductId(), request.getVariantId(), request.getQuantity());

        // Tìm xem sản phẩm này (cùng biến thể nếu có) đã tồn tại trong giỏ của user chưa
        Cart cart = cartRepository.findByUserIdAndProductIdAndVariantId(
                request.getUserId(), request.getProductId(), request.getVariantId()).orElse(new Cart());

        if (cart.getId() == null) {
            // Trường hợp thêm mới
            User u = new User(); u.setId(request.getUserId()); cart.setUser(u);
            Product p = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
            cart.setProduct(p);

            if (request.getVariantId() != null) {
                ProductVariant v = variantRepository.findById(request.getVariantId())
                        .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại"));
                cart.setVariant(v);
            }
            cart.setQuantity(request.getQuantity());
        } else {
            // Trường hợp đã có -> Cộng dồn số lượng và kiểm tra lại kho một lần nữa
            int newQuantity = cart.getQuantity() + request.getQuantity();
            validateStock(request.getProductId(), request.getVariantId(), newQuantity);
            cart.setQuantity(newQuantity);
        }
        return cartRepository.save(cart);
    }

    // 2. Cập nhật số lượng (Dùng cho nút +/- trên giao diện)
    @Transactional
    public Cart updateQuantity(Integer cartId, Integer newQuantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (newQuantity <= 0) {
            cartRepository.delete(cart);
            return null;
        }

        // Kiểm tra tồn kho thực tế
        validateStock(cart.getProduct().getId(),
                cart.getVariant() != null ? cart.getVariant().getId() : null,
                newQuantity);

        cart.setQuantity(newQuantity);
        return cartRepository.save(cart);
    }

    // 3. Lấy toàn bộ giỏ hàng của một người dùng
    public List<Cart> getMyCart(Integer userId) {
        return cartRepository.findByUserId(userId);
    }

    // 4. Xóa một sản phẩm khỏi giỏ hàng
    @Transactional
    public void removeFromCart(Integer cartId) {
        if (!cartRepository.existsById(cartId)) {
            throw new RuntimeException("Sản phẩm không tồn tại trong giỏ hàng");
        }
        cartRepository.deleteById(cartId);
    }

    // 5. Xóa sạch giỏ hàng (Dùng sau khi đặt hàng hoặc nút Xóa tất cả)
    @Transactional
    public void clearCart(Integer userId) {
        cartRepository.deleteByUserId(userId);
    }

    // Hàm bổ trợ kiểm tra kho
    private void validateStock(Integer productId, Integer variantId, Integer quantity) {
        int stockAvailable = 0;
        if (variantId != null) {
            ProductVariant v = variantRepository.findById(variantId).orElseThrow();
            stockAvailable = v.getStock();
        } else {
            Product p = productRepository.findById(productId).orElseThrow();
            stockAvailable = p.getStock();
        }

        if (quantity > stockAvailable) {
            throw new RuntimeException("Rất tiếc, kho chỉ còn " + stockAvailable + " sản phẩm.");
        }
    }
}