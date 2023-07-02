package it.sdcc.projectsonlinebackend.controllers;

import it.sdcc.projectsonlinebackend.services.ManagedFileService;
import it.sdcc.projectsonlinebackend.services.ProgettoService;
import it.sdcc.projectsonlinebackend.services.UtenteService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
@RequestMapping("/file")
public class ManagedFileController {
    @Autowired
    private ManagedFileService managedFileService;

    @Autowired
    private UtenteService utenteService;

    @GetMapping("/getFiles")
    public ResponseEntity getFiles(@RequestParam("nomeProgetto") @Valid String nomeProgetto, @RequestParam("username") @Valid String username){
        try {
            return new ResponseEntity(managedFileService.ottieniFiles(nomeProgetto, utenteService.getUtente(username)), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity("Error getting files: "+e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//getFiles

    @PostMapping(value = "/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity uploadFile(@RequestParam("nomeProgetto") @Valid String nomeProgetto, @RequestParam("username_utente") @Valid String username_utente, @RequestParam("file") @Valid MultipartFile multipartFile) {
        try {
            return new ResponseEntity(managedFileService.caricaNuovoFile(nomeProgetto, utenteService.getUtente(username_utente), multipartFile), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity("Error uploading file: "+e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//uploadFile

    @DeleteMapping("/deleteFile")
    public ResponseEntity deleteFile(@RequestParam("nomeFile") @Valid String nomeFile, @RequestParam("nomeProgetto") @Valid String nomeProgetto, @RequestParam("username") @Valid String username) {
        try {
            return new ResponseEntity(managedFileService.cancellaFile(nomeFile, nomeProgetto, utenteService.getUtente(username)), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity("Error deleting file: "+e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//deleteFile

    @GetMapping(value = "/downloadFile")
    public ResponseEntity downloadFile(@RequestParam("nomeFile") @Valid String nomeFile, @RequestParam("nomeProgetto") @Valid String nomeProgetto, @RequestParam("username") @Valid String username, HttpServletResponse response) {
        try {
            managedFileService.downloadFile(nomeFile, nomeProgetto, utenteService.getUtente(username), response);
            return new ResponseEntity("File downloaded correctly", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity("Error downloading file: "+e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }//downloadFile
}//ManagedFileController
