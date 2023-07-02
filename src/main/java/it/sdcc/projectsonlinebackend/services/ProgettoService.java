package it.sdcc.projectsonlinebackend.services;

import it.sdcc.projectsonlinebackend.entities.*;
import it.sdcc.projectsonlinebackend.entities.keys.ProgettoUtenteKey;
import it.sdcc.projectsonlinebackend.repositories.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ProgettoService {
    @Autowired
    private ProgettoRepository progettoRepository;

    @Autowired
    private ProgettoUtenteRepository progettoUtenteRepository;

    @Autowired
    private ManagedFileRepository managedFileRepository;

    @Autowired
    private BlobService blobService;

    @Autowired
    private CommentoRepository commentoRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    /**Metodo che autorizza un utente ad accedere a un progetto
     * @param nomeProgetto il nome del progetto
     * @param utente il nome dell'utente da autorizzare
     * @return l'utente appena autorizzato
     * @throws IllegalArgumentException se ci sono inconsistenze: il progetto non esiste o l'utente non esiste*/
    @Transactional(isolation = Isolation.DEFAULT)
    public Utente autorizzaUtente(@NotNull String nomeProgetto, @NotNull Utente utente){
        if (utente == null)
            throw new IllegalArgumentException("Utente inesistente");
        Progetto progetto = progettoRepository.findProgettoByNome(nomeProgetto);
        if (progetto == null)
            throw new IllegalArgumentException("Progetto inesistente");
        if (progetto.getProprietario().equals(utente))
            return utente;
        List<ProgettoUtente> progettiUtente = progettoUtenteRepository.findProgettoUtenteByUtente(utente);
        if(contiene(progettiUtente, progetto))
            return utente;

        ProgettoUtenteKey progettoUtenteKey = new ProgettoUtenteKey(nomeProgetto, utente.getUsername());
        ProgettoUtente progettoUtente = new ProgettoUtente();
        progettoUtente.setProgettoUtenteKey(progettoUtenteKey);
        progettoUtente.setUtente(utente);
        progettoUtente.setProgetto(progetto);
        progettoUtenteRepository.saveAndFlush(progettoUtente);

        return utente;
    }//autorizzaUtente

    /**Metodo che disautorizza un utente ad accedere a un progetto. Nel momento in cui questo accade, i commenti che tale utente ha lasciato vengono rimossi, mentre i file che ha caricato vengono associati al proprietario del progetto
     * @param nomeProgetto il nome del progetto
     * @param utente il nome dell'utente da autorizzare
     * @return l'utente appena disautorizzato
     * @throws IllegalArgumentException se ci sono inconsistenze: il progetto non esiste, l'utente non esiste o l'utente che si cerca di disautorizzare è il proprietario*/
    @Transactional(isolation = Isolation.DEFAULT)
    public Utente disautorizzaUtente(@NotNull String nomeProgetto, @NotNull Utente utente){
        if (utente == null)
            throw new IllegalArgumentException("Utente inesistente");
        Progetto progetto = progettoRepository.findProgettoByNome(nomeProgetto);
        if (progetto == null)
            throw new IllegalArgumentException("Progetto inesistente");
        if (progetto.getProprietario().equals(utente))
            throw new IllegalArgumentException("Impossibile disautorizzare il proprietario");
        List<ProgettoUtente> progettiUtente = progettoUtenteRepository.findProgettoUtenteByUtente(utente);
        if(!contiene(progettiUtente, progetto))
            return utente;

        ProgettoUtenteKey progettoUtenteKey = new ProgettoUtenteKey(nomeProgetto, utente.getUsername());

        progettoUtenteRepository.deleteById(progettoUtenteKey);
        progettoUtenteRepository.flush();

        List<ManagedFile> listmf = managedFileRepository.findManagedFileByProgetto(progetto);
        for(ManagedFile mf : listmf)
            if(mf.getCreatore().equals(utente))
                mf.setCreatore(progetto.getProprietario());

        managedFileRepository.saveAllAndFlush(listmf);

        List<Commento> listcm = commentoRepository.findCommentoByProgettoCommentato(progetto);
        commentoRepository.deleteAll(listcm);
        commentoRepository.flush();

        return utente;
    }//deautorizzaUtente

    /**Trova i progetti a cui l'utente può accedere, sia quelli che possiede sia quelli ai quali è stato autorizzato ad accedere
     * @param utente l'utente che ha effettuato la richiesta
     * @return la lista dei progetti a cui l'utente può accedere. Può essere vuota
     * @throws IllegalArgumentException se l'utente non esiste*/
    @Transactional(readOnly = true)
    public List<Progetto> ottieniProgetti(@NotNull Utente utente) {
        if (utente == null)
            throw new IllegalArgumentException("Utente inesistente");

        List<Progetto> list = new LinkedList<>();
        list.addAll(progettoRepository.findProgettoByProprietario(utente));
        List<ProgettoUtente> listpu = progettoUtenteRepository.findProgettoUtenteByUtente(utente);
        for(ProgettoUtente pu : listpu)
            list.add(pu.getProgetto());
        return list;
    }//ottieniProgetti

    /**Trova gli utenti che possono accedere al progetto, escluso il proprietario.
     * @param nomeProgetto il progetto
     * @return la lista degli utenti
     * @throws IllegalArgumentException se il progetto non esiste o non esiste il container associato al progetto*/
    @Transactional(readOnly = true)
    public List<Utente> ottieniUtentiAutorizzati(String nomeProgetto){
        Progetto progetto = progettoRepository.findProgettoByNome(nomeProgetto);
        if(progetto == null || !blobService.containsContainer(nomeProgetto))
            throw new IllegalArgumentException("Progetto inesistente");

        List<ProgettoUtente> list = progettoUtenteRepository.findProgettoUtenteByProgetto(progetto);
        List<Utente> utenti = new LinkedList<>();
        if(list != null && !list.isEmpty())
            for(ProgettoUtente pu : list)
                utenti.add(pu.getUtente());
        return utenti;
    }//getUtenti

    /**Trova gli utenti che non possono accedere al progetto.
     * @param nomeProgetto il progetto
     * @return la lista degli utenti
     * @throws IllegalArgumentException se il progetto non esiste o non esiste il container associato al progetto*/
    @Transactional(readOnly = true)
    public List<Utente> trovaUtentiNonAutorizzati(String nomeProgetto){
        Progetto progetto = progettoRepository.findProgettoByNome(nomeProgetto);
        if(progetto == null || !blobService.containsContainer(nomeProgetto))
            throw new IllegalArgumentException("Progetto inesistente");

        List<ProgettoUtente> list = new LinkedList<>(progettoUtenteRepository.findProgettoUtenteByProgetto(progetto));
        List<Utente> utenti = new LinkedList<>(utenteRepository.findAll());
        utenti.remove(progetto.getProprietario());

        for(ProgettoUtente pu : list)
            utenti.remove(pu.getUtente());

        return utenti;
    }//trovaUtentiNonAutorizzati

    /**Crea un nuovo progetto
     * @param nomeProgetto il nome del progetto
     * @param proprietario l'utente che ha effettuato la richiesta e che diventerà il proprietario del progetto
     * @return il progetto appena creato
     * @throws IllegalArgumentException se ci sono inconsistenze: l'utente non esiste o il progetto esiste già*/
    @Transactional(isolation = Isolation.DEFAULT)
    public Progetto creaNuovoProgetto(@NotNull String nomeProgetto, @NotNull Utente proprietario){
        if(proprietario == null)
            throw new IllegalArgumentException("Utente inesistente");

        if(progettoRepository.findProgettoByNome(nomeProgetto) != null)
            throw new IllegalArgumentException("Progetto esistente");

        Progetto progetto = new Progetto();
        progetto.setNome(nomeProgetto);
        progetto.setProprietario(proprietario);
        progettoRepository.saveAndFlush(progetto);

        blobService.createContainer(nomeProgetto);

        return progetto;
    }//creaNuovoProgetto

    /**Cancella un progetto
     * @param nomeProgetto il nome del progetto
     * @param proprietario l'utente che ha effettuato la richiesta. In quanto proprietario, è l'unico che può cancellare il progetto
     * @return il progetto appena cancellato
     * @throws IllegalArgumentException se ci sono inconsistenze: l'utente non esiste, il progetto non esiste, il container associato non esiste o l'utente non è il proprietario del progetto*/
    @Transactional(isolation = Isolation.DEFAULT)
    public Progetto cancellaProgetto(@NotNull String nomeProgetto, @NotNull Utente proprietario){
        Progetto progetto = progettoRepository.findProgettoByNome(nomeProgetto);
        if(progetto == null || !blobService.containsContainer(nomeProgetto))
            throw new IllegalArgumentException("Progetto inesistente");
        if(proprietario == null)
            throw new IllegalArgumentException("Utente inesistente");
        if(!progetto.getProprietario().equals(proprietario))
            throw new IllegalArgumentException("Utente non autorizzato a cancellare il progetto");

        blobService.deleteContainer(nomeProgetto);

        List<ProgettoUtente> pu = progettoUtenteRepository.findProgettoUtenteByProgetto(progetto);
        progettoUtenteRepository.deleteAll(pu);
        progettoUtenteRepository.flush();

        List<Commento> commenti = commentoRepository.findCommentoByProgettoCommentato(progetto);
        commentoRepository.deleteAll(commenti);
        commentoRepository.flush();

        List<ManagedFile> mf = managedFileRepository.findManagedFileByProgetto(progetto);
        managedFileRepository.deleteAll(mf);
        managedFileRepository.flush();

        progettoRepository.delete(progetto);
        progettoRepository.flush();

        return progetto;
    }//cancellaProgetto

    /**Metodo privato che verifica se un utente può accedere a un progetto di cui non è proprietario
     * @param list l'elenco dei progetti a cui l'utente può accedere
     * @param progetto il progetto su cui l'utente desidera accedere
     * @return true se fra i progetti a cui l'utente può accedere c'è anche quello passato fra i parametri, false altrimenti*/
    private boolean contiene(List<ProgettoUtente> list, Progetto progetto){
        for(ProgettoUtente pu : list){
            if(pu.getProgetto().equals(progetto))
                return true;
        }
        return false;
    }//contiene
}//ProgettoService
