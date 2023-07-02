package it.sdcc.projectsonlinebackend.repositories;

import it.sdcc.projectsonlinebackend.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentoRepository extends JpaRepository<Commento, Integer> {
    List<Commento> findCommentoByProgettoCommentato(Progetto progetto);
    Commento findCommentoById(Long id);
}//CommentoRepository
