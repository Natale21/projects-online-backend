package it.sdcc.projectsonlinebackend.controllers;

import it.sdcc.projectsonlinebackend.entities.DTO.Authorization;
import it.sdcc.projectsonlinebackend.services.ProgettoService;
import it.sdcc.projectsonlinebackend.services.UtenteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project")
public class ProgettoController {
    @Autowired
    private ProgettoService progettoService;

    @Autowired
    private UtenteService utenteService;

    @GetMapping(value = "/getProjects")
    public ResponseEntity getProject(@RequestParam("username") String username){
        try {
            return new ResponseEntity(progettoService.ottieniProgetti(utenteService.getUtente(username)), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity("Error getting projects: " + e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//getProject

    @PostMapping(value = "/createProject")
    public ResponseEntity createProject(@RequestParam("nomeProgetto") String nomeProgetto, @RequestParam("username_proprietario") String username_proprietario) {
        try {
            return new ResponseEntity(progettoService.creaNuovoProgetto(nomeProgetto.toLowerCase(), utenteService.getUtente(username_proprietario)), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity("Error creating project: " + e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//createProject

    @DeleteMapping("/deleteProject")
    public ResponseEntity deleteProject(@RequestParam("nomeProgetto") @Valid String nomeProgetto, @RequestParam("username") @Valid String username) {
        try {
            return new ResponseEntity(progettoService.cancellaProgetto(nomeProgetto, utenteService.getUtente(username)), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity("Error deleting project: " + e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//deleteProject

    @GetMapping("/getAuthorizedUsers")
    public ResponseEntity getAuthorizedUsers(@RequestParam("nomeProgetto") @Valid String nomeProgetto){
        try {
            return new ResponseEntity(progettoService.ottieniUtentiAutorizzati(nomeProgetto), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity("Error getting users: " + e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//getAuthorizedUsers

    @GetMapping("/getAllUnauthorizedUsers")
    public ResponseEntity getAllUnauthorizedUsers(@RequestParam("nomeProgetto") @Valid String nomeProgetto){
        try {
            return new ResponseEntity(progettoService.trovaUtentiNonAutorizzati(nomeProgetto), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity("Error getting users: " + e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//getAllUnauthorizedUsers

    @PostMapping(value = "/authorize", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity authorize(@RequestBody @Valid Authorization authorization){
        try {
            return new ResponseEntity(progettoService.autorizzaUtente(authorization.getNomeProgetto(), utenteService.getUtente(authorization.getUsername())), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity("Error authorizing user: " + e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//authorize

    @DeleteMapping("/deauthorize")
    public ResponseEntity deauthorize(@RequestParam("nomeProgetto") @Valid String nomeProgetto, @RequestParam("username") @Valid String username){
        try {
            return new ResponseEntity(progettoService.disautorizzaUtente(nomeProgetto, utenteService.getUtente(username)), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity("Error deauthorizing user: " + e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//deauthorize
}//ProgettoController
