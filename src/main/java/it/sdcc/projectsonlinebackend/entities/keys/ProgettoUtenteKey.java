package it.sdcc.projectsonlinebackend.entities.keys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgettoUtenteKey implements Serializable {
    @Column(name = "nome_progetto")
    private String nome_progetto;

    @Column(name = "username_utente")
    private String username_utente;
}//ProgettoUtenteKey
