package com.wip.controller;

import com.wip.dto.ParcelDto;
import com.wip.exception.ResourceNotFoundException;
import com.wip.service.ParcelService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String showCreateForm(@RequestParam(value = "customerId", required = false) Long customerId,
                                 Model model) {
        ParcelDto parcelDto = new ParcelDto();

        // Auto-set customer from auth if regular user
        if (!com.wip.security.CurrentUserUtil.isAdmin()) {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder
                            .getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.wip.security.CustomUserDetails details) {
                parcelDto.setCustomerId(details.getCustomerId());
            }
        } else if (customerId != null) {
            parcelDto.setCustomerId(customerId);
        }

        model.addAttribute("parcelDto", parcelDto);
        model.addAttribute("today", LocalDate.now());
        return "parcel/form";
    }

    @PostMapping("/save")
    public String saveParcel(@Valid @ModelAttribute("parcelDto") ParcelDto parcelDto,
                             BindingResult bindingResult,
                             Model model) {
        // Re-enforce customer ID from auth for non-admin users
        if (!com.wip.security.CurrentUserUtil.isAdmin()) {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder
                            .getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.wip.security.CustomUserDetails details) {
                parcelDto.setCustomerId(details.getCustomerId());
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("today", LocalDate.now());
            return "parcel/form";
        }

        try {
            ParcelDto saved = parcelService.addParcel(parcelDto);
            return "redirect:/parcels/" + saved.getParcelId();
        } catch (ResourceNotFoundException ex) {
            // e.g. customer not found or user has no profile yet
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("today", LocalDate.now());
            return "parcel/form";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Could not save the parcel. Please check your details and try again.");
            model.addAttribute("today", LocalDate.now());
            return "parcel/form";
        }
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
                               @Valid @ModelAttribute("parcelDto") ParcelDto parcelDto,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("today", LocalDate.now());
            return "parcel/form";
        }

        try {
            parcelService.updateParcel(id, parcelDto);
            return "redirect:/parcels/" + id;
        } catch (ResourceNotFoundException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("today", LocalDate.now());
            return "parcel/form";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Could not update the parcel. Please try again.");
            model.addAttribute("today", LocalDate.now());
            return "parcel/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteParcel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            parcelService.deleteParcel(id);
            redirectAttributes.addFlashAttribute("successMessage", "Parcel deleted successfully.");
            return "redirect:/parcels";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/parcels";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not delete this parcel. Please try again.");
            return "redirect:/parcels";
        }
    }
}