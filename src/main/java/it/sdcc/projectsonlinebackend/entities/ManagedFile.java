package it.sdcc.projectsonlinebackend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "managed_file")
public class ManagedFile implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Basic
    @Column(name = "nome")
    private String nome;

    @Basic
    @Column(name = "dimensione")
    private Integer dimensione;

    @ManyToOne
    @JoinColumn(name = "creatore")
    private Utente creatore;

    @ManyToOne
    @JoinColumn(name = "progetto")
    private Progetto progetto;

    @Override
    public int hashCode() {
        int result = 1;
        result = (int) (result * 59 + id);
        result = result * 59 + nome.hashCode();
        result = result * 59 + creatore.hashCode();
        result = result * 59 + progetto.hashCode();
        result = result * 59 + dimensione;
        return result;
    }//hashCode

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ManagedFile))
            return false;
        if(o == this)
            return true;

        ManagedFile mf = (ManagedFile)o;
        return mf.nome.equals(nome) && mf.progetto.equals(progetto);
    }//equals
}//ManagedFile
