package it.sdcc.projectsonlinebackend.services;

import it.sdcc.projectsonlinebackend.entities.Utente;
import it.sdcc.projectsonlinebackend.repositories.UtenteRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UtenteService {
    @Autowired
    private UtenteRepository utenteRepository;

    /**Salva un utente appena creato
     * @param utente l'utente da salvare
     * @return l'utente appena creato*/
    @Transactional(isolation = Isolation.DEFAULT)
    public Utente salvaUtente(@NotNull Utente utente){
        if(utenteRepository.existsUtenteByUsername(utente.getUsername()))
            return utente;
        return utenteRepository.saveAndFlush(utente);
    }//salvaUtente

    /**Cerca un utente
     * @param username l'username dell'utente da trovare
     * @return l'utente, se esiste
     * @throws IllegalArgumentException se l'utente non c'è*/
    @Transactional(readOnly = true)
    public Utente getUtente(@NotNull String username){
        Utente u = utenteRepository.findUtenteByUsername(username);
        if(u == null)
            throw new IllegalArgumentException("Utente non trovato");
        return u;
    }//getUtente
}//UtenteService
