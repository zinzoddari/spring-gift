package gift.member.view;

import gift.member.service.AdminMemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/members")
class AdminMemberController {

    private final AdminMemberService adminMemberService;

    public AdminMemberController(final AdminMemberService adminMemberService) {
        this.adminMemberService = adminMemberService;
    }

    @GetMapping
    public String list(final Model model) {
        model.addAttribute("members", adminMemberService.getMembers());
        return "member/list";
    }

    @GetMapping("/new")
    public String newForm() {
        return "member/new";
    }

    @PostMapping
    public String create(
        @RequestParam final String email,
        @RequestParam final String password,
        final Model model
    ) {
        try {
            adminMemberService.createMember(email, password);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            return "member/new";
        }
        return "redirect:/admin/members";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable final Long id, final Model model) {
        model.addAttribute("member", adminMemberService.getMember(id));
        return "member/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(
        @PathVariable final Long id,
        @RequestParam final String email,
        @RequestParam final String password
    ) {
        adminMemberService.updateMember(id, email, password);
        return "redirect:/admin/members";
    }

    @PostMapping("/{id}/charge-point")
    public String chargePoint(
        @PathVariable final Long id,
        @RequestParam final int amount
    ) {
        adminMemberService.chargePoint(id, amount);
        return "redirect:/admin/members";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable final Long id) {
        adminMemberService.deleteMember(id);
        return "redirect:/admin/members";
    }
}
