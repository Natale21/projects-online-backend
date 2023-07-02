package it.sdcc.projectsonlinebackend.services;

import it.sdcc.projectsonlinebackend.entities.ManagedFile;
import it.sdcc.projectsonlinebackend.entities.Progetto;
import it.sdcc.projectsonlinebackend.entities.ProgettoUtente;
import it.sdcc.projectsonlinebackend.entities.Utente;
import it.sdcc.projectsonlinebackend.repositories.ManagedFileRepository;
import it.sdcc.projectsonlinebackend.repositories.ProgettoRepository;
import it.sdcc.projectsonlinebackend.repositories.ProgettoUtenteRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.List;

@Service
public class ManagedFileService {
    @Autowired
    private ManagedFileRepository managedFileRepository;

    @Autowired
    private BlobService blobService;

    @Autowired
    private ProgettoRepository progettoRepository;

    @Autowired
    private ProgettoUtenteRepository progettoUtenteRepository;

    /**Elimina un file da un progetto
     * @param nomeFile il nome del file da cancellare
     * @param nomeProgetto il nome del progetto che contiene il file
     * @param utente l'utente che ha effettuato la richiesta
     * @return il progetto aggiornato
     * @throws IllegalArgumentException se ci sono inconsistenze, ovvero se: il progetto non esiste, l'utente non esiste, il container associato al progetto non esiste, il file non esiste o l'utente non è autorizzato ad effettuare operazioni sul progetto*/
    @Transactional(isolation = Isolation.DEFAULT)
    public Progetto cancellaFile(@NotNull String nomeFile, @NotNull String nomeProgetto, @NotNull Utente utente){
        Progetto progetto = progettoRepository.findProgettoByNome(nomeProgetto);
        if(progetto == null)
            throw new IllegalArgumentException("Progetto inesistente");

        if(utente == null)
            throw new IllegalArgumentException("Utente inesistente");

        List<ProgettoUtente> lista = progettoUtenteRepository.findProgettoUtenteByUtente(utente);
        if(utenteNonAutorizzato(utente, progetto, lista))
            throw new IllegalArgumentException("Utente non autorizzato ad accedere al progetto");

        if(!blobService.containsContainer(nomeProgetto))
            throw new IllegalArgumentException("Container inesistente");

        ManagedFile managedFile = managedFileRepository.findManagedFileByNomeAndProgetto(nomeFile, progetto);
        if(managedFile == null)
            throw new IllegalArgumentException("File inesistente");

        blobService.deleteBlob(nomeProgetto, nomeFile);

        managedFileRepository.delete(managedFile);
        managedFileRepository.flush();

        progettoRepository.saveAndFlush(progetto);

        return progetto;
    }//cancellaFile

    /**Trova tutti i file di un progetto. Attenzione: non restituisce i file effettivi, ma le informazioni su di essi, conservate nel database
     * @param nomeProgetto il nome del progetto di cui si vogliono ottenere i file
     * @param utente l'utente che ha effettuato la richiesta
     * @return la lista delle informazioni sui file del progetto
     * @throws IllegalArgumentException se ci sono inconsistenze: il progetto non esiste, l'utente non esiste o l'utente non è autorizzato a effettuare operazioni sul progetto*/
    @Transactional(readOnly = true)
    public List<ManagedFile> ottieniFiles(@NotNull String nomeProgetto, @NotNull Utente utente){
        Progetto progetto = progettoRepository.findProgettoByNome(nomeProgetto);
        if(progetto == null)
            throw new IllegalArgumentException("Progetto inesistente");
        if(utente == null)
            throw new IllegalArgumentException("Utente inesistente");
        List<ProgettoUtente> lista = progettoUtenteRepository.findProgettoUtenteByUtente(utente);
        if(utenteNonAutorizzato(utente, progetto, lista))
            throw new IllegalArgumentException("Utente non autorizzato ad accedere al progetto");

        List<ManagedFile> files = managedFileRepository.findManagedFileByProgetto(progetto);
        return files;
    }//ottieniFiles

    /**Carica un file nel progetto.
     * @param nomeProgetto il nome del progetto in cui si vuole caricare il file
     * @param utente l'utente che ha effettuato la richiesta
     * @param multipartFile il file da caricare, nella forma di una rappresentazione di un file ottenuto mediante una multipart request
     * @return il progetto aggiornato
     * @throws IllegalArgumentException se ci sono inconsistenze: il progetto non esiste, l'utente non esiste, l'utente non è autorizzato a effettuare operazioni sul progetto o c'è stato un errore nel caricamento sullo storage*/
    @Transactional(isolation = Isolation.DEFAULT)
    public Progetto caricaNuovoFile(@NotNull String nomeProgetto, @NotNull Utente utente, @NotNull MultipartFile multipartFile){
        Progetto progetto = progettoRepository.findProgettoByNome(nomeProgetto);
        if(progetto == null)
            throw new IllegalArgumentException("Progetto inesistente");

        if(!blobService.containsContainer(nomeProgetto))
            blobService.createContainer(nomeProgetto);

        if(utente == null)
            throw new IllegalArgumentException("Utente inesistente");

        List<ProgettoUtente> lista = progettoUtenteRepository.findProgettoUtenteByUtente(utente);
        if(utenteNonAutorizzato(utente, progetto, lista))
            throw new IllegalArgumentException("Utente non autorizzato ad accedere al progetto");

        try{
            blobService.uploadBlob(nomeProgetto, multipartFile);
        }catch(IOException io){
            throw new IllegalArgumentException("Errore nel caricamento sul blob storage");
        }

        ManagedFile file = new ManagedFile();
        file.setNome(multipartFile.getOriginalFilename());
        file.setCreatore(utente);
        file.setProgetto(progetto);
        file.setDimensione((int)multipartFile.getSize());

        managedFileRepository.saveAndFlush(file);
        progettoRepository.saveAndFlush(progetto);

        Progetto ret = progettoRepository.findProgettoByNome(nomeProgetto);
        return ret;
    }//caricaNuovoFile

    /**Scarica un file.
     * @param nomeFile il nome del file da scaricare
     * @param nomeProgetto il nome del progetto in cui si trova il file
     * @param utente l'utente che ha effettuato la richiesta
     * @param response la risposta su cui verrà caricato il file appena scaricato
     * @throws IllegalArgumentException se ci sono inconsistenze: il progetto non esiste, il container ad esso associato non esiste, l'utente non esiste, l'utente non è autorizzato a effettuare operazioni sul progetto o il file non esiste*/
    @Transactional(isolation = Isolation.DEFAULT)
    public void downloadFile(@NotNull String nomeFile, @NotNull String nomeProgetto, @NotNull Utente utente, @NotNull HttpServletResponse response) throws IOException{
        Progetto progetto = progettoRepository.findProgettoByNome(nomeProgetto);
        if(progetto == null)
            throw new IllegalArgumentException("Progetto inesistente");

        if(!blobService.containsContainer(nomeProgetto))
            throw new IllegalArgumentException("Container inesistente");

        ManagedFile mf = managedFileRepository.findManagedFileByNomeAndProgetto(nomeFile, progetto);
        if(mf == null || !blobService.containsFile(nomeProgetto, nomeFile))
            throw new IllegalArgumentException("File inesistente");

        if(utente == null)
            throw new IllegalArgumentException("Utente inesistente");

        List<ProgettoUtente> lista = progettoUtenteRepository.findProgettoUtenteByUtente(utente);
        if(utenteNonAutorizzato(utente, progetto, lista))
            throw new IllegalArgumentException("Utente non autorizzato ad accedere al progetto");

        ByteArrayOutputStream bufOutputStream = blobService.downloadBlob(nomeProgetto, nomeFile);

        response.setContentType("application/*");
        response.addHeader("Content-Disposition", "inline; filename=" + nomeFile);

        OutputStream responseOutputStream = response.getOutputStream();
        bufOutputStream.writeTo(responseOutputStream);
        responseOutputStream.flush();
        responseOutputStream.close();
    }//downloadFile

    /**Metodo privato che converte un file in un array di byte
     * @param file il file da convertire
     * @return l'array di byte
     * @throws IOException se c'è un'eccezione di tipo I/O*/
    private static byte[] toByteArray(File file) throws IOException {
        byte[] buf = new byte[(int)file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(buf);
        fileInputStream.close();
        return buf;
    }//toByteArray

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
}//ManagedFileService
