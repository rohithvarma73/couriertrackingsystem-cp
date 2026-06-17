package com.wip.controller;

import com.wip.service.CustomerService;
import com.wip.service.ParcelService;
import com.wip.service.ShipmentService;
import com.wip.service.TrackingUpdateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

    private final CustomerService customerService;
    private final ParcelService parcelService;
    private final ShipmentService shipmentService;
    private final TrackingUpdateService trackingUpdateService;

    public SearchController(CustomerService customerService,
                            ParcelService parcelService,
                            ShipmentService shipmentService,
                            TrackingUpdateService trackingUpdateService) {
        this.customerService = customerService;
        this.parcelService = parcelService;
        this.shipmentService = shipmentService;
        this.trackingUpdateService = trackingUpdateService;
    }

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