package it.sdcc.projectsonlinebackend.repositories;

import it.sdcc.projectsonlinebackend.entities.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UtenteRepository extends JpaRepository<Utente, String> {
    boolean existsUtenteByUsername(String username);
    Utente findUtenteByUsername(String username);
}//UtenteRepository
