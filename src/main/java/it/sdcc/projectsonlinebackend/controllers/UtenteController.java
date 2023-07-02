package it.sdcc.projectsonlinebackend.controllers;

import it.sdcc.projectsonlinebackend.entities.*;
import it.sdcc.projectsonlinebackend.services.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UtenteController {
    @Autowired
    private UtenteService utenteService;

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createUser(@RequestBody @Valid Utente utente){
        try {
            return new ResponseEntity(utenteService.salvaUtente(utente), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity("Error creating user: " + e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//createUser

    @GetMapping("/find")
    public ResponseEntity findUser(@RequestParam("username") @Valid String username){
        try {
            return new ResponseEntity(utenteService.getUtente(username), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity("Error getting user: " + e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//findUser
}//UtenteController

