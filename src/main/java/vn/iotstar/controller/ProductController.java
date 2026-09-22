package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ProductService;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        model.addAttribute("products", productService.findAll(keyword, page, size));
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);
        return "products/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("mode", "create");
        return "products/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute ProductDTO dto,
                         BindingResult result,
                         @RequestParam(required = false) MultipartFile image,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            return "products/form";
        }
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        dto.setUserId(user.getId());
        try {
            MultipartFile fileToUpload = (image != null && !image.isEmpty()) ? image : dto.getImage();
            productService.create(dto, fileToUpload);
            redirect.addFlashAttribute("success", "Tạo sản phẩm thành công.");
            return "redirect:/products";
        } catch (Exception e) {
            result.reject("product.error", e.getMessage() != null ? e.getMessage() : "Lỗi khi lưu sản phẩm.");
            model.addAttribute("mode", "create");
            return "products/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Authentication authentication, Model model, RedirectAttributes redirect) {
        ProductDTO product = productService.findById(id);
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && (authentication == null || !product.getUsername().equals(authentication.getName()))) {
            redirect.addFlashAttribute("error", "Bạn chỉ có thể chỉnh sửa sản phẩm do chính mình đăng!");
            return "redirect:/products";
        }
        model.addAttribute("productDTO", product);
        model.addAttribute("mode", "edit");
        return "products/form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute ProductDTO dto,
                       BindingResult result,
                       @RequestParam(required = false) MultipartFile image,
                       Authentication authentication,
                       Model model,
                       RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            return "products/form";
        }
        ProductDTO existing = productService.findById(id);
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && (authentication == null || !existing.getUsername().equals(authentication.getName()))) {
            redirect.addFlashAttribute("error", "Bạn không có quyền cập nhật sản phẩm của người khác!");
            return "redirect:/products";
        }
        try {
            MultipartFile fileToUpload = (image != null && !image.isEmpty()) ? image : dto.getImage();
            productService.update(id, dto, fileToUpload);
            redirect.addFlashAttribute("success", "Cập nhật sản phẩm thành công.");
            return "redirect:/products";
        } catch (Exception e) {
            result.reject("product.error", e.getMessage() != null ? e.getMessage() : "Lỗi khi cập nhật sản phẩm.");
            model.addAttribute("mode", "edit");
            return "products/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, Authentication authentication, RedirectAttributes redirect) {
        ProductDTO existing = productService.findById(id);
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && (authentication == null || !existing.getUsername().equals(authentication.getName()))) {
            redirect.addFlashAttribute("error", "Bạn không có quyền xóa sản phẩm của người khác!");
            return "redirect:/products";
        }
        productService.delete(id);
        redirect.addFlashAttribute("success", "Xóa sản phẩm thành công.");
        return "redirect:/products";
    }
}
