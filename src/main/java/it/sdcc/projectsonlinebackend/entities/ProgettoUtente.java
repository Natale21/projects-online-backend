package it.sdcc.projectsonlinebackend.entities;

import it.sdcc.projectsonlinebackend.entities.keys.ProgettoUtenteKey;
import lombok.*;
import jakarta.persistence.*;
import java.io.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "progetto_utente")
public class ProgettoUtente implements Serializable {
    @EmbeddedId
    private ProgettoUtenteKey progettoUtenteKey;

    @ManyToOne
    @MapsId("nome_progetto")
    @JoinColumn(name = "nome_progetto")
    private Progetto progetto;

    @ManyToOne
    @MapsId("username_utente")
    @JoinColumn(name = "username_utente")
    private Utente utente;
}//ProgettoUtente
