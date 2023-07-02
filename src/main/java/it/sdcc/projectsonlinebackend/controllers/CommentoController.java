package it.sdcc.projectsonlinebackend.controllers;

import it.sdcc.projectsonlinebackend.entities.Commento;
import it.sdcc.projectsonlinebackend.services.CommentoService;
import it.sdcc.projectsonlinebackend.services.UtenteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentoController {

    @Autowired
    private CommentoService commentoService;

    @Autowired
    private UtenteService utenteService;

    @GetMapping("/getComments")
    public ResponseEntity getComments(@RequestParam("nomeProgetto") @Valid String nomeProgetto, @RequestParam("username") @Valid String username){
        try{
            return new ResponseEntity(commentoService.getComments(nomeProgetto, utenteService.getUtente(username)), HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity("Error getting comments: "+e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//getComments

    @DeleteMapping("/deleteComment")
    public ResponseEntity deleteComment(@RequestParam("commento_id") @Valid Long id, @RequestParam("username") @Valid String username){
        try{
            return new ResponseEntity(commentoService.deleteComment(id, utenteService.getUtente(username)), HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity("Error getting comments: "+e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//deleteComment

    @PostMapping(value = "/addComment", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addComment(@RequestBody @Valid Commento commento){
        try{
            return new ResponseEntity(commentoService.addComment(commento), HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity("Error getting comments: "+e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//getComments
}//CommentoController
