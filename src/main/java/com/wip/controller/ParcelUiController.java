package com.wip.controller;

import com.wip.dto.ParcelDto;
import com.wip.service.ParcelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/parcels")
public class ParcelUiController {

    private final ParcelService parcelService;

    public ParcelUiController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @GetMapping
    public String listParcels(Model model) {
        model.addAttribute("parcels", parcelService.getAllParcels());
        return "parcel/list";
    }

    @GetMapping("/by-customer/{customerId}")
    public String listParcelsByCustomer(@PathVariable Long customerId, Model model) {
        model.addAttribute("parcels", parcelService.getParcelsByCustomerId(customerId));
        model.addAttribute("customerId", customerId);
        return "parcel/by-customer";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("parcelDto", new ParcelDto());
        model.addAttribute("today", LocalDate.now());
        return "parcel/form";
    }

    @PostMapping("/save")
    public String saveParcel(@ModelAttribute("parcelDto") ParcelDto parcelDto) {
        ParcelDto saved = parcelService.addParcel(parcelDto);
        return "redirect:/parcels/" + saved.getParcelId();
    }

    @GetMapping("/{id}")
    public String parcelDetails(@PathVariable Long id, Model model) {
        model.addAttribute("parcel", parcelService.getParcelById(id));
        return "parcel/details";
    }

    @GetMapping("/{id}/edit")
    public String editParcelForm(@PathVariable Long id, Model model) {
        model.addAttribute("parcelDto", parcelService.getParcelById(id));
        model.addAttribute("today", LocalDate.now());
        return "parcel/form";
    }

    @PostMapping("/{id}/update")
    public String updateParcel(@PathVariable Long id,
                               @ModelAttribute("parcelDto") ParcelDto parcelDto) {
        parcelService.updateParcel(id, parcelDto);
        return "redirect:/parcels/" + id;
    }

    @DeleteMapping("/{id}/delete")
    public String deleteParcel(@PathVariable Long id) {
        parcelService.deleteParcel(id);
        return "redirect:/parcels";
    }
}