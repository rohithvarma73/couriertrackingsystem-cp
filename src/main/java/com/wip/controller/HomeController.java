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

/**
 * Spring MVC controller that handles the home page, root redirect, and dashboard views.
 *
 * <p>This controller is responsible for the top-level navigation of the Courier Tracking
 * System's web UI. It serves the public-facing landing page at {@code /} and {@code /home},
 * and provides a role-aware dashboard at {@code /dashboard}. When an authenticated user
 * visits the root URL they are automatically redirected to the dashboard. The dashboard
 * populates the Thymeleaf model with high-level statistics: administrators see totals for
 * customers, parcels, and shipments, while regular users see totals for parcels and their
 * active shipments.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Controller
public class HomeController {

    private final CustomerService customerService;
    private final ParcelService parcelService;
    private final ShipmentService shipmentService;

    /**
     * Constructs a {@code HomeController} with the required service dependencies.
     *
     * <p>Spring automatically injects these services via constructor injection,
     * ensuring that all dependencies are non-null at runtime.</p>
     *
     * @param customerService  service for customer-related data retrieval
     * @param parcelService    service for parcel-related data retrieval
     * @param shipmentService  service for shipment-related data retrieval
     */
    public HomeController(CustomerService customerService, ParcelService parcelService, ShipmentService shipmentService) {
        this.customerService = customerService;
        this.parcelService = parcelService;
        this.shipmentService = shipmentService;
    }

    /**
     * Handles GET requests to the root URL ({@code /}).
     *
     * <p>If the requesting user is fully authenticated (i.e., not the anonymous principal),
     * they are redirected to the dashboard. Unauthenticated visitors are served the
     * public home page ({@code home} view).</p>
     *
     * @return {@code "redirect:/dashboard"} for authenticated users, or {@code "home"}
     *         for unauthenticated visitors
     */
    @GetMapping("/")
    public String index() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "home";
    }

    /**
     * Handles GET requests to {@code /home}.
     *
     * <p>Renders the public-facing landing page of the application. This endpoint
     * is accessible to both authenticated and unauthenticated users and does not
     * add any model attributes.</p>
     *
     * @return the logical view name {@code "home"}
     */
    @GetMapping("/home")
    public String home() {
        return "home";
    }

    /**
     * Handles GET requests to {@code /dashboard} and populates the model with summary statistics.
     *
     * <p>The data added to the model is role-dependent:</p>
     * <ul>
     *   <li><strong>Admin users</strong> receive {@code totalCustomers}, {@code totalParcels},
     *       and {@code totalShipments} counts for the global system overview.</li>
     *   <li><strong>Regular users</strong> receive {@code totalParcels} and
     *       {@code activeShipments} counts relevant to their context.</li>
     * </ul>
     * <p>The rendered view is the {@code index} Thymeleaf template.</p>
     *
     * @param model the Spring MVC {@link Model} used to pass statistics to the view
     * @return the logical view name {@code "index"}
     */
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
