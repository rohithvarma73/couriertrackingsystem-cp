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
 * Thymeleaf UI controller for managing customer records in the Courier Tracking System.
 *
 * <p>Handles all browser-facing HTTP requests under the {@code /customers} path and
 * renders server-side HTML views using the Thymeleaf templating engine. This controller
 * covers the complete customer lifecycle — listing, creating, viewing details, editing,
 * and deleting customers — while providing user-friendly error messages and form
 * validation feedback through Spring MVC's {@link BindingResult} mechanism.</p>
 *
 * <p>Page flow overview:
 * <ul>
 *   <li>{@code GET  /customers}          → {@code customer/list}   — all customers</li>
 *   <li>{@code GET  /customers/new}      → {@code customer/form}   — blank creation form</li>
 *   <li>{@code POST /customers/save}     → redirect to details or re-render form on error</li>
 *   <li>{@code GET  /customers/{id}}     → {@code customer/details} — single customer view</li>
 *   <li>{@code GET  /customers/{id}/edit}→ {@code customer/form}   — pre-filled edit form</li>
 *   <li>{@code POST /customers/{id}/update} → redirect to details or re-render form on error</li>
 *   <li>{@code POST /customers/{id}/delete} → redirect to list or render delete error view</li>
 * </ul>
 *
 * @author Dharshan K S (dharshan.ks@wipro.com)
 * @version 1.0
 * @since 1.0
 */
@Controller
@RequestMapping("/customers")
public class CustomerUiController {

    /** Service layer delegate for all customer business operations. */
    private final CustomerService customerService;

    /**
     * Constructs a {@code CustomerUiController} with the required {@link CustomerService}.
     *
     * @param customerService the service bean responsible for customer business logic;
     *                        injected by Spring's dependency injection container
     */
    public CustomerUiController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Displays the paginated list of all customers.
     *
     * <p>Fetches all customer records and populates the model with a {@code customers}
     * attribute before rendering the {@code customer/list} view template.</p>
     *
     * @param model the Spring MVC {@link Model} used to pass attributes to the view;
     *              receives the {@code customers} attribute (a list of {@link CustomerDto})
     * @return the logical view name {@code "customer/list"}
     */
    @GetMapping
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        return "customer/list";
    }

    /**
     * Displays a blank form for creating a new customer.
     *
     * <p>Adds an empty {@link CustomerDto} to the model so that the Thymeleaf form can
     * bind to it. The view renders input fields for name, email, phone, and address.</p>
     *
     * @param model the Spring MVC {@link Model} used to pass the empty {@code customerDto}
     *              binding object to the view
     * @return the logical view name {@code "customer/form"}
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("customerDto", new CustomerDto());
        return "customer/form";
    }

    /**
     * Processes the submission of a new customer creation form.
     *
     * <p>Validates the submitted {@link CustomerDto} via Bean Validation. If validation
     * errors are present, the form is re-rendered with inline error messages. On
     * successful persistence, the user is redirected to the newly created customer's
     * detail page. If the service throws an exception (e.g., duplicate email), an
     * {@code errorMessage} attribute is added to the model and the form is re-rendered.</p>
     *
     * @param customerDto   the form-bound customer data submitted by the user;
     *                      must satisfy all Bean Validation constraints
     * @param bindingResult holds validation errors produced during binding and validation
     * @param model         the Spring MVC {@link Model} used to pass error messages back to the view
     * @return a redirect to {@code /customers/{id}} on success, or the logical view name
     *         {@code "customer/form"} when validation or persistence fails
     */
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

    /**
     * Displays the detail view for a single customer identified by their ID.
     *
     * <p>Looks up the customer by {@code id} and adds the result as a {@code customer}
     * model attribute. If the customer does not exist, the service layer propagates a
     * {@code ResourceNotFoundException}.</p>
     *
     * @param id    the unique identifier of the customer whose details are to be displayed
     * @param model the Spring MVC {@link Model} used to pass the {@code customer} attribute
     *              (a {@link CustomerDto}) to the view
     * @return the logical view name {@code "customer/details"}
     * @throws com.wip.exception.ResourceNotFoundException if no customer exists with the given ID
     */
    @GetMapping("/{id}")
    public String customerDetails(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.getCustomerById(id));
        return "customer/details";
    }

    /**
     * Displays a pre-populated form for editing an existing customer's details.
     *
     * <p>Loads the current customer data by {@code id} and binds it to the {@code customerDto}
     * model attribute so that all existing field values are pre-filled in the Thymeleaf form.</p>
     *
     * @param id    the unique identifier of the customer to be edited
     * @param model the Spring MVC {@link Model} used to pass the pre-filled {@code customerDto}
     *              to the view
     * @return the logical view name {@code "customer/form"}
     * @throws com.wip.exception.ResourceNotFoundException if no customer exists with the given ID
     */
    @GetMapping("/{id}/edit")
    public String editCustomerForm(@PathVariable Long id, Model model) {
        model.addAttribute("customerDto", customerService.getCustomerById(id));
        return "customer/form";
    }

    /**
     * Processes the submission of the customer edit form and persists the updated details.
     *
     * <p>Validates the submitted {@link CustomerDto}. If validation fails, the form is
     * re-rendered with inline error messages. On success, the user is redirected to the
     * customer's detail page. Handles {@link ResourceNotFoundException} when the target
     * customer no longer exists, and generic exceptions for any other unexpected error.</p>
     *
     * @param id            the unique identifier of the customer being updated
     * @param customerDto   the form-bound updated customer data; must satisfy all Bean Validation constraints
     * @param bindingResult holds validation errors produced during binding and validation
     * @param model         the Spring MVC {@link Model} used to pass error messages back to the view
     * @return a redirect to {@code /customers/{id}} on success, or the logical view name
     *         {@code "customer/form"} when validation or persistence fails
     * @throws com.wip.exception.ResourceNotFoundException if no customer exists with the given ID
     */
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

    /**
     * Handles the deletion of a customer and redirects appropriately.
     *
     * <p>Attempts to delete the customer identified by {@code id}. On success, a flash
     * attribute {@code successMessage} is added and the user is redirected to the customer
     * list. If the customer has linked parcels, an {@link IllegalStateException} is caught
     * and the {@code customer/delete} error view is rendered with an informative message.
     * Any other unexpected exception redirects the user back to the customer list with an
     * error flash message.</p>
     *
     * @param id                 the unique identifier of the customer to delete
     * @param model              the Spring MVC {@link Model} used to pass error messages to the error view
     * @param redirectAttributes Spring MVC redirect attribute container used to carry flash messages
     *                           across the redirect boundary
     * @return a redirect to {@code /customers} on success or generic error; or the logical
     *         view name {@code "customer/delete"} when the customer has linked parcels
     */
    @PostMapping("/{id}/delete")
    public String deleteCustomer(@PathVariable Long id,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Customer deleted.");
            return "redirect:/customers";
        } catch (IllegalStateException ex) {
            // Customer has linked parcels — show informative error
            model.addAttribute("errorMessage", ex.getMessage());
            return "customer/delete";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not delete this customer. Please try again.");
            return "redirect:/customers";
        }
    }
}
