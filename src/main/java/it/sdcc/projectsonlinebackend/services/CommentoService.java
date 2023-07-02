package it.sdcc.projectsonlinebackend.services;

import it.sdcc.projectsonlinebackend.entities.*;
import it.sdcc.projectsonlinebackend.repositories.CommentoRepository;
import it.sdcc.projectsonlinebackend.repositories.ProgettoRepository;
import it.sdcc.projectsonlinebackend.repositories.ProgettoUtenteRepository;
import it.sdcc.projectsonlinebackend.repositories.UtenteRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CommentoService {
    @Autowired
    private CommentoRepository commentoRepository;

    @Autowired
    private ProgettoRepository progettoRepository;

    @Autowired
    private BlobService blobService;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private ProgettoUtenteRepository progettoUtenteRepository;

    /**Aggiunge un nuovo commento
     * @param commento il commento da aggiungere
     * @return il commento appena caricato, se tutto è andato a buon fine
     * @throws IllegalArgumentException se il commento presenta delle inconsistenze: in particolare, se il progetto commentato non esiste, se l'utente che ha lasciato il commento non esiste o non è autorizzato ad accedere al progetto e, infine, se il commento non ha contenuto*/
    @Transactional(isolation = Isolation.DEFAULT)
    public Commento addComment(@NotNull Commento commento){
        Progetto progetto = progettoRepository.findProgettoByNome(commento.getProgettoCommentato().getNome());
        if(progetto == null)
            throw new IllegalArgumentException("Progetto inesistente");
        Utente utente = utenteRepository.findUtenteByUsername(commento.getUtente().getUsername());
        if(utente == null)
            throw new IllegalArgumentException("Utente inesistente");
        List<ProgettoUtente> lista = progettoUtenteRepository.findProgettoUtenteByUtente(utente);
        if(utenteNonAutorizzato(utente, progetto, lista))
            throw new IllegalArgumentException("Utente non autorizzato a modificare il progetto");
        if(commento.getContenuto() == null)
            throw new IllegalArgumentException("No content!");
        return commentoRepository.saveAndFlush(commento);
    }//addComment

    /**Cerca i commenti associati a un certo progetto
     * @param nomeProgetto il progetto da cui prendere i commenti
     * @param utente l'utente che ha effettuato la richiesta
     * @return la lista dei commenti associati al progetto
     * @throws IllegalArgumentException se la richiesta presenta delle inconsistenze: in particolare, se il progetto non esiste, se l'utente non esiste o non è autorizzato ad accedere al progetto e, infine, se il container associato al progetto non esiste*/
    @Transactional(readOnly = true)
    public List<Commento> getComments(@NotNull String nomeProgetto, @NotNull Utente utente){
        Progetto progetto = progettoRepository.findProgettoByNome(nomeProgetto);
        if(progetto == null)
            throw new IllegalArgumentException("Unexisting project");
        if(utente == null)
            throw new IllegalArgumentException("Unexisting user");
        List<ProgettoUtente> lista = progettoUtenteRepository.findProgettoUtenteByUtente(utente);
        if(utenteNonAutorizzato(utente, progetto, lista))
            throw new IllegalArgumentException("User not allowed to manage project");

        if(!blobService.containsContainer(nomeProgetto))
            throw new IllegalArgumentException("Unexisting project");

        List<Commento> ret = commentoRepository.findCommentoByProgettoCommentato(progetto);
        return ret;
    }//getComments

    /**Cancella un commento
     * @param id l'identificatore del commento
     * @param utente l'utente che ha effettuato la richiesta
     * @return il commento appena cancellato, se tutto è andato a buon fine
     * @throws IllegalArgumentException se ci sono delle inconsistenze: in particolare, se il commento non esiste o se l'utente che ha lasciato il commento non esiste o non è autorizzato ad accedere al progetto*/
    @Transactional(isolation = Isolation.DEFAULT)
    public Commento deleteComment(@NotNull Long id, @NotNull Utente utente){
        Commento commento = commentoRepository.findCommentoById(id);
        if(commento == null)
            throw new IllegalArgumentException("Unexisting comment");
        if(utente == null)
            throw new IllegalArgumentException("Unexisting user");
        List<ProgettoUtente> lista = progettoUtenteRepository.findProgettoUtenteByUtente(utente);
        if(utenteNonAutorizzato(utente, commento.getProgettoCommentato(), lista) || !utente.equals(commento.getUtente()))
            throw new IllegalArgumentException("User not allowed to manage project");

        commentoRepository.delete(commento);
        commentoRepository.flush();
        return commento;
    }//deleteComment


    /**Metodo privato che verifica se un utente può accedere a un progetto di cui non è proprietario
     * @param list l'elenco dei progetti a cui l'utente può accedere
     * @param progetto il progetto su cui l'utente desidera accedere
     * @return true se fra i progetti a cui l'utente può accedere c'è anche quello passato fra i parametri, false altrimenti*/
    private boolean contiene(List<ProgettoUtente> list, Progetto progetto){
        if(list == null)
            return false;
        for(ProgettoUtente pu : list){
            if(pu.getProgetto().equals(progetto))
                return true;
        }
        return false;
    }//contiene

    /**Metodo privato che verifica se un utente non è autorizzato ad accedere a un progetto, ovvero se contemporaneamente non è nè il proprietario nè il proprietario lo ha autorizzato ad accedere
     * @param utente l'utente che vuole accedere
     * @param progetto il progetto a cui l'utente vuole accedere
     * @param lista l'elenco dei progetti a cui l'utente può accedere
     * @return true se l'utente non è autorizzato, false altrimenti
     * */
    private boolean utenteNonAutorizzato(Utente utente, Progetto progetto, List<ProgettoUtente> lista){
        return !progetto.getProprietario().equals(utente) &&
                !contiene(lista, progetto);
    }//utenteNonAutorizzato
}//CommentoService
