package com.wip.controller;

import com.wip.security.CurrentUserUtil;
import com.wip.service.CustomerService;
import com.wip.service.ParcelService;
import com.wip.service.ShipmentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CustomerService customerService;
    private final ParcelService parcelService;
    private final ShipmentService shipmentService;

    public HomeController(CustomerService customerService, ParcelService parcelService, ShipmentService shipmentService) {
        this.customerService = customerService;
        this.parcelService = parcelService;
        this.shipmentService = shipmentService;
    }

    @GetMapping("/")
    public String index() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "home";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        if (CurrentUserUtil.isAdmin()) {
            model.addAttribute("totalCustomers", customerService.getAllCustomers().size());
            model.addAttribute("totalParcels", parcelService.getAllParcels().size());
            model.addAttribute("totalShipments", shipmentService.getAllShipments().size());
        } else {
            model.addAttribute("totalParcels", parcelService.getAllParcels().size());
            model.addAttribute("activeShipments", shipmentService.getAllShipments().size());
        }
        return "index";
    }
}