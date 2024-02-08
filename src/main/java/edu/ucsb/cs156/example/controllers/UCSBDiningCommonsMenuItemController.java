package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "UCSBDiningCommonsMenuItems")
@RequestMapping("/api/ucsbdiningcommonsmenuitem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

    @Autowired
    UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

    @Operation(summary= "List all ucsb menu items")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("all")
    public Iterable<UCSBDiningCommonsMenuItem> allMenuItems() {
        Iterable<UCSBDiningCommonsMenuItem> items = ucsbDiningCommonsMenuItemRepository.findAll();
        return items;
    }

    @Operation(summary= "Create a new item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBDiningCommonsMenuItem postItemsCommonsMenuItem(
        // @Parameter(name="id") @RequestParam long id,
        @Parameter(name="diningCommonsCode") @RequestParam String diningCommonsCode,
        @Parameter(name="name") @RequestParam String name,
        @Parameter(name="station") @RequestParam String station
        )
        {

        UCSBDiningCommonsMenuItem items = new UCSBDiningCommonsMenuItem();
        // items.setId(id);
        items.setDiningCommonsCode(diningCommonsCode);
        items.setName(name);
        items.setStation(station);

        UCSBDiningCommonsMenuItem savedItems = ucsbDiningCommonsMenuItemRepository.save(items);

        return savedItems;
    }

    @Operation(summary= "Get a single menu item")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBDiningCommonsMenuItem getById(
            @Parameter(name="diningCommonsCode") @RequestParam String diningCommonsCode) {
        UCSBDiningCommonsMenuItem items = ucsbDiningCommonsMenuItemRepository.findById(diningCommonsCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, diningCommonsCode));

        return items;
    }

    @Operation(summary= "Delete a menu item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteCommons(
            @Parameter(name="diningCommonCode") @RequestParam String diningCommonsCode) {
        UCSBDiningCommonsMenuItem items = ucsbDiningCommonsMenuItemRepository.findById(diningCommonsCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, diningCommonsCode));

        ucsbDiningCommonsMenuItemRepository.delete(items);
        return genericMessage("UCSBDiningCommons menu item with id %s deleted".formatted(diningCommonsCode));
    }

    @Operation(summary= "Update a single menu item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBDiningCommonsMenuItem updateCommons(
            @Parameter(name="diningCommonCode") @RequestParam String diningCommonsCode,
            @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

        UCSBDiningCommonsMenuItem items = ucsbDiningCommonsMenuItemRepository.findById(diningCommonsCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, diningCommonsCode));

        items.setId(incoming.getId());  
        items.setDiningCommonsCode(incoming.getDiningCommonsCode());
        items.setName(incoming.getName());
        items.setStation(incoming.getStation());


        ucsbDiningCommonsMenuItemRepository.save(items);

        return items;
    }
}
