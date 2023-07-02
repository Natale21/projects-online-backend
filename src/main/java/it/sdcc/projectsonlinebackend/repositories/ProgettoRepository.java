package it.sdcc.projectsonlinebackend.repositories;

import it.sdcc.projectsonlinebackend.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgettoRepository extends JpaRepository<Progetto, String> {
    List<Progetto> findProgettoByProprietario(Utente proprietario);
    Progetto findProgettoByNome(String nome);
}//ProgettoRepository
