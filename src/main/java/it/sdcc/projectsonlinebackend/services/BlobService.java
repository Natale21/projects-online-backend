package it.sdcc.projectsonlinebackend.services;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import it.sdcc.projectsonlinebackend.entities.ManagedFile;
import it.sdcc.projectsonlinebackend.repositories.ManagedFileRepository;
import it.sdcc.projectsonlinebackend.repositories.ProgettoRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Service
public class BlobService {
    @Autowired
    private BlobServiceClient blobServiceClient;

    @Autowired
    private ManagedFileRepository managedFileRepository;

    @Autowired
    private ProgettoRepository progettoRepository;

    /**Crea un nuovo Blob Container, se non esiste già
     * @param nomeContainer il nome del container da creare*/
    public void createContainer(@NotNull String nomeContainer){
        if(!containsContainer(nomeContainer))
            blobServiceClient.createBlobContainer(nomeContainer);
    }//createContainer

    /**Elimina il Blob Container, se esiste
     * @param nomeContainer il nome del container da eliminare*/
    public void deleteContainer(@NotNull String nomeContainer){
        if(containsContainer(nomeContainer))
            blobServiceClient.deleteBlobContainer(nomeContainer);
    }//deleteContainer

    /**Verifica se esiste un container avente il nome specificato.
     * @param nomeContainer il nome del container da cercare
     * @return true se il container è presente, false altrimenti*/
    public boolean containsContainer(@NotNull String nomeContainer){
        for (BlobContainerItem container : blobServiceClient.listBlobContainers()) {
            if (container.getName().equals(nomeContainer))
                return true;
        }
        return false;
    }//containsContainer

    /**Verifica l'esistenza di un file nel container specificato
     * @param nomeContainer il nome del container in cui cercare il file
     * @param fileName il nome del file
     * @return true se il file è presente, false altrimenti*/
    public boolean containsFile(@NotNull String nomeContainer, @NotNull String fileName){
        return blobServiceClient.getBlobContainerClient(nomeContainer).getBlobClient(fileName).exists();
    }//containsContainer

    /**Effettua il caricamento di un file nel container specificato. Se il file esiste già, viene sovrascritto.
     * @param nomeContainer il nome del container in cui caricare il file
     * @param multipartFile il file da caricare, nella forma di una rappresentazione di un file ottenuto mediante una multipart request
     * */
    public void uploadBlob(@NotNull String nomeContainer, @NotNull MultipartFile multipartFile) throws IOException {
        if(!containsContainer(nomeContainer))
            throw new IllegalArgumentException("Container inesistente");
        BlobClient blobClient = blobServiceClient.getBlobContainerClient(nomeContainer)
                .getBlobClient(multipartFile.getOriginalFilename());
        blobClient.upload(multipartFile.getInputStream(), multipartFile.getSize(), true);
    }//uploadBlob

    /**Effettua lo scaricamento del file dallo storage.
     * @param nomeContainer il nome del container da cui scaricare il file
     * @param blobname il nome del blob da scaricare
     * @return lo stream per la scrittura nella risposta*/
    public ByteArrayOutputStream downloadBlob(@NotNull String nomeContainer, @NotNull String blobname) throws IOException{
        ManagedFile mf = managedFileRepository.findManagedFileByNomeAndProgetto(blobname, progettoRepository.findProgettoByNome(nomeContainer));
        if(mf == null)
            throw new IllegalArgumentException("File inesistente");
        BlobClient blobClient = blobServiceClient.getBlobContainerClient(nomeContainer)
                .getBlobClient(blobname);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(mf.getDimensione());
        blobClient.downloadStream(byteArrayOutputStream);
        return byteArrayOutputStream;
    }//downloadBlob

    /**Cancella un file, se esiste.
     * @param containerName il nome del container in cui si trova il file
     * @param blobname il nome del file da eliminare
     * @return true se il file è stato cancellato, false altrimenti*/
    public boolean deleteBlob(@NotNull String containerName, @NotNull String blobname){
        if(!containsContainer(containerName))
            throw new IllegalArgumentException("Progetto inesistente");
        BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName)
                .getBlobClient(blobname);
        return blobClient.deleteIfExists();
    }//deleteBlob
}//BlobService

