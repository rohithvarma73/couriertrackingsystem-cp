package com.wip.controller;

import com.wip.dto.ParcelDto;
import com.wip.service.ParcelService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("parcelDto", new ParcelDto());
        return "parcel/form";
    }

    @PostMapping("/save")
    public String saveParcel(@ModelAttribute("parcelDto") @Valid ParcelDto parcelDto) {
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
        return "parcel/form";
    }

    @PostMapping("/{id}/update")
    public String updateParcel(@PathVariable Long id,
                               @ModelAttribute("parcelDto") @Valid ParcelDto parcelDto) {
        parcelService.updateParcel(id, parcelDto);
        return "redirect:/parcels/" + id;
    }

    @DeleteMapping("/{id}/delete")
    public String deleteParcel(@PathVariable Long id) {
        parcelService.deleteParcel(id);
        return "redirect:/parcels";
    }
}