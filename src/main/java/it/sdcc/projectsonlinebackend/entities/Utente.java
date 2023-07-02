package it.sdcc.projectsonlinebackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Table;
import lombok.*;
import jakarta.persistence.*;
import java.io.*;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name = "utente")
public class Utente implements Serializable {
    @Id
    @Column(name = "username")
    private String username;

    @OneToMany(mappedBy = "proprietario")
    @JsonIgnore
    private List<Progetto> progettiInPossesso = new LinkedList<>();

    @OneToMany(mappedBy = "utente")
    @JsonIgnore
    private List<Commento> commentiPubblicati = new LinkedList<>();

    @OneToMany(mappedBy = "creatore")
    @JsonIgnore
    private List<ManagedFile> fileCreati = new LinkedList<>();

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Utente))
            return false;
        if(o == this)
            return true;

        return username.equals(((Utente)o).username);
    }//equals

    @Override
    public int hashCode(){
        int result = 1;
        result = result * 59 + this.username.hashCode();
        result = result * 59 + this.progettiInPossesso.hashCode();
        result = result * 59 + this.commentiPubblicati.hashCode();
        result = result * 59 + this.fileCreati.hashCode();
        return result;
    }//hashcode
}//Utente
