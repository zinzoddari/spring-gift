package gift.product.view;

import gift.product.ProductNameValidator;
import gift.product.domain.Product;
import gift.product.service.AdminProductService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/products")
class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(final AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping
    public String list(final Model model) {
        model.addAttribute("products", adminProductService.getProducts());
        return "product/list";
    }

    @GetMapping("/new")
    public String newForm(final Model model) {
        model.addAttribute("categories", adminProductService.getCategories());
        return "product/new";
    }

    @PostMapping
    public String create(
        @RequestParam final String name,
        @RequestParam final int price,
        @RequestParam final String imageUrl,
        @RequestParam final Long categoryId,
        final Model model
    ) {
        final List<String> errors = ProductNameValidator.validate(name, true);
        if (!errors.isEmpty()) {
            populateNewForm(model, errors, name, price, imageUrl, categoryId);
            return "product/new";
        }

        adminProductService.createProduct(name, price, imageUrl, categoryId);
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable final Long id, final Model model) {
        model.addAttribute("product", adminProductService.getProduct(id));
        model.addAttribute("categories", adminProductService.getCategories());
        return "product/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(
        @PathVariable final Long id,
        @RequestParam final String name,
        @RequestParam final int price,
        @RequestParam final String imageUrl,
        @RequestParam final Long categoryId,
        final Model model
    ) {
        final Product product = adminProductService.getProduct(id);

        final List<String> errors = ProductNameValidator.validate(name, true);
        if (!errors.isEmpty()) {
            populateEditForm(model, product, errors, name, price, imageUrl, categoryId);
            return "product/edit";
        }

        adminProductService.updateProduct(id, name, price, imageUrl, categoryId);
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable final Long id) {
        adminProductService.deleteProduct(id);
        return "redirect:/admin/products";
    }

    private void populateNewForm(
        final Model model,
        final List<String> errors,
        final String name,
        final int price,
        final String imageUrl,
        final Long categoryId
    ) {
        model.addAttribute("errors", errors);
        model.addAttribute("name", name);
        model.addAttribute("price", price);
        model.addAttribute("imageUrl", imageUrl);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", adminProductService.getCategories());
    }

    private void populateEditForm(
        final Model model,
        final Product product,
        final List<String> errors,
        final String name,
        final int price,
        final String imageUrl,
        final Long categoryId
    ) {
        model.addAttribute("errors", errors);
        model.addAttribute("product", product);
        model.addAttribute("name", name);
        model.addAttribute("price", price);
        model.addAttribute("imageUrl", imageUrl);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", adminProductService.getCategories());
    }
}
