package com.wip.controller;

import com.wip.dto.CustomerDto;
import com.wip.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String saveCustomer(@ModelAttribute("customerDto") @Valid CustomerDto customerDto) {
        CustomerDto saved = customerService.addCustomer(customerDto);
        return "redirect:/customers/" + saved.getCustomerId();
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
                                 @ModelAttribute("customerDto") @Valid CustomerDto customerDto) {
        customerService.updateCustomer(id, customerDto);
        return "redirect:/customers/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteCustomer(@PathVariable Long id, Model model) {
        try {
            customerService.deleteCustomer(id);
            return "redirect:/customers";
        } catch (IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "customer/delete";
        }
    }
}