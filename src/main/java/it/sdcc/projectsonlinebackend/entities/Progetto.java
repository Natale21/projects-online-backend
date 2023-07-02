package it.sdcc.projectsonlinebackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import jakarta.persistence.*;
import java.io.*;
import java.util.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "progetto")
public class Progetto implements Serializable {
    @Id
    @Column(name = "nome")
    private String nome;

    @ManyToOne
    @JoinColumn(name = "proprietario", nullable = false)
    private Utente proprietario;

    @OneToMany(mappedBy = "progettoCommentato")
    @JsonIgnore
    private List<Commento> commenti = new ArrayList<>();

    @OneToMany(mappedBy = "progetto")
    @JsonIgnore
    private List<ManagedFile> files = new ArrayList<>();

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 59 + nome.hashCode();
        result = result * 59 + proprietario.hashCode();
        result = result * 59 + commenti.hashCode();
        result = result * 59 + files.hashCode();
        return result;
    }//hashCode

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Progetto))
            return false;
        if(o == this)
            return true;

        return ((Progetto)o).nome.equals(nome);
    }//equals
}//Progetto
