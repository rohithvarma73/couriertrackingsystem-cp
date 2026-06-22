package com.wip.controller;

import com.wip.service.CustomerService;
import com.wip.service.ParcelService;
import com.wip.service.ShipmentService;
import com.wip.service.TrackingUpdateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Spring MVC controller that handles the global search feature of the Courier Tracking System.
 *
 * <p>This controller exposes a single GET endpoint at {@code /search} that accepts a keyword
 * query parameter ({@code q}) and performs a concurrent search across all four major domain
 * entities: customers, parcels, shipments, and tracking updates. The aggregated results are
 * added to the model and rendered by the {@code search/results} Thymeleaf template.</p>
 *
 * <p>If the {@code q} parameter is absent or blank the search is performed with an empty
 * keyword string, which may return all records depending on the service implementation.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Controller
public class SearchController {

    private final CustomerService customerService;
    private final ParcelService parcelService;
    private final ShipmentService shipmentService;
    private final TrackingUpdateService trackingUpdateService;

    /**
     * Constructs a {@code SearchController} with all required service dependencies.
     *
     * <p>Spring automatically injects these services via constructor injection,
     * ensuring that all dependencies are non-null at runtime.</p>
     *
     * @param customerService        service for searching customer records
     * @param parcelService          service for searching parcel records
     * @param shipmentService        service for searching shipment records
     * @param trackingUpdateService  service for searching tracking update records
     */
    public SearchController(CustomerService customerService,
                            ParcelService parcelService,
                            ShipmentService shipmentService,
                            TrackingUpdateService trackingUpdateService) {
        this.customerService = customerService;
        this.parcelService = parcelService;
        this.shipmentService = shipmentService;
        this.trackingUpdateService = trackingUpdateService;
    }

    /**
     * Handles GET requests to {@code /search} and returns search results across all domain entities.
     *
     * <p>The following model attributes are populated and made available to the view:</p>
     * <ul>
     *   <li>{@code q} — the trimmed search keyword used for the query (empty string if none)</li>
     *   <li>{@code customers} — list of {@code CustomerDto} records matching the keyword</li>
     *   <li>{@code parcels} — list of {@code ParcelDto} records matching the keyword</li>
     *   <li>{@code shipments} — list of {@code ShipmentDto} records matching the keyword</li>
     *   <li>{@code trackingUpdates} — list of {@code TrackingUpdateDto} records matching
     *       the keyword</li>
     * </ul>
     *
     * @param q     the search keyword provided via the {@code q} request parameter;
     *              may be {@code null} or empty if the user submitted an empty query
     * @param model the Spring MVC {@link Model} used to pass search results to the view
     * @return the logical view name {@code "search/results"}
     */
    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false) String q, Model model) {
        String keyword = (q == null) ? "" : q.trim();

        model.addAttribute("q", keyword);
        model.addAttribute("customers", customerService.search(keyword));
        model.addAttribute("parcels", parcelService.search(keyword));
        model.addAttribute("shipments", shipmentService.search(keyword));
        model.addAttribute("trackingUpdates", trackingUpdateService.search(keyword));

        return "search/results";
    }
}
