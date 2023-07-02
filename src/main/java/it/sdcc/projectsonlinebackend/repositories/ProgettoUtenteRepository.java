package it.sdcc.projectsonlinebackend.repositories;

import it.sdcc.projectsonlinebackend.entities.*;
import it.sdcc.projectsonlinebackend.entities.keys.ProgettoUtenteKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgettoUtenteRepository extends JpaRepository<ProgettoUtente, ProgettoUtenteKey> {
    List<ProgettoUtente> findProgettoUtenteByUtente(Utente utente);
    List<ProgettoUtente> findProgettoUtenteByProgetto(Progetto progetto);
}//ProgettoUtenteRepository
