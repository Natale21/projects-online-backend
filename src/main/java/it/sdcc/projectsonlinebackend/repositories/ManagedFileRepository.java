package it.sdcc.projectsonlinebackend.repositories;

import it.sdcc.projectsonlinebackend.entities.ManagedFile;
import it.sdcc.projectsonlinebackend.entities.Progetto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManagedFileRepository extends JpaRepository<ManagedFile, Integer> {
    List<ManagedFile> findManagedFileByProgetto(Progetto progetto);
    ManagedFile findManagedFileByNomeAndProgetto(String nome, Progetto progetto);
}//ManagedFileRepository
