package com.wip.controller;

import com.wip.dto.CustomerDto;
import com.wip.exception.ResourceNotFoundException;
import com.wip.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CustomerUiController Component.
 * 
 * Handles operations and data related to CustomerUiController.
 */
@Controller
@RequestMapping("/customers")
public class CustomerUiController {

    private final CustomerService customerService;

    public CustomerUiController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        return "customer/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("customerDto", new CustomerDto());
        return "customer/form";
    }

    @PostMapping("/save")
    public String saveCustomer(@Valid @ModelAttribute("customerDto") CustomerDto customerDto,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "customer/form";
        }
        try {
            CustomerDto saved = customerService.addCustomer(customerDto);
            return "redirect:/customers/" + saved.getCustomerId();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Could not save customer: " + ex.getMessage());
            return "customer/form";
        }
    }

    @GetMapping("/{id}")
    public String customerDetails(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.getCustomerById(id));
        return "customer/details";
    }

    @GetMapping("/{id}/edit")
    public String editCustomerForm(@PathVariable Long id, Model model) {
        model.addAttribute("customerDto", customerService.getCustomerById(id));
        return "customer/form";
    }

    @PostMapping("/{id}/update")
    public String updateCustomer(@PathVariable Long id,
                                 @Valid @ModelAttribute("customerDto") CustomerDto customerDto,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "customer/form";
        }
        try {
            customerService.updateCustomer(id, customerDto);
            return "redirect:/customers/" + id;
        } catch (ResourceNotFoundException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "customer/form";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Could not update customer. Please try again.");
            return "customer/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCustomer(@PathVariable Long id,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Customer deleted.");
            return "redirect:/customers";
        } catch (IllegalStateException ex) {
            // Customer has linked parcels â€” show informative error
            model.addAttribute("errorMessage", ex.getMessage());
            return "customer/delete";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not delete this customer. Please try again.");
            return "redirect:/customers";
        }
    }
}
