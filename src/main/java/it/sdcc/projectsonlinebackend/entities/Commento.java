package it.sdcc.projectsonlinebackend.entities;

import lombok.*;
import java.io.*;
import java.util.Objects;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "commento")
public class Commento implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Basic
    @Column(name = "contenuto")
    private String contenuto;

    @ManyToOne
    @JoinColumn(name = "username_utente", nullable = false)
    private Utente utente;

    @ManyToOne
    @JoinColumn(name = "nome_progetto", nullable = false)
    private Progetto progettoCommentato;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Commento))
            return false;
        if (o == this)
            return true;
        return ((Commento)o).id == id;
    }//equals

    @Override
    public int hashCode() {
        int result = 1;
        result = (int) (result * 59 + id);
        result = result * 59 + contenuto.hashCode();
        result = result * 59 + utente.hashCode();
        result = result * 59 + progettoCommentato.hashCode();
        return result;
    }//hashCode
}//Commento
