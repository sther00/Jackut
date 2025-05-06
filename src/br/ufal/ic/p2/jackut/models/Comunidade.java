package br.ufal.ic.p2.jackut.models;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Representa uma comunidade dentro do sistema Jackut.
 * A comunidade possui um nome, descrição, dono e membros.
 */
public class Comunidade implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nome;
    private final String descricao;
    private final String dono;
    private final Set<String> membros = new HashSet<>();
    private final Facade facade;

    /**
     * Construtor que inicializa a comunidade com nome, descrição, dono e a instância da Facade.
     *
     * @param nome O nome da comunidade.
     * @param descricao A descrição da comunidade.
     * @param dono O login do usuário dono da comunidade.
     * @param facade Instância da Facade para acessar o sistema.
     */
    public Comunidade(String nome, String descricao, String dono, Facade facade) {
        this.nome = nome;
        this.descricao = descricao;
        this.dono = dono;
        this.facade = facade;
        this.membros.add(dono);
    }

    public String getNome() { return nome; }

    public String getDescricao() { return descricao; }

    public String getDono() { return dono; }

    public Set<String> getMembros() { return membros; }

    /**
     * Adiciona um novo membro à comunidade.
     *
     * @param login O login do membro a ser adicionado.
     * @param facade Instância da Facade para validar o usuário.
     * @throws IllegalArgumentException Se o login for inválido ou o usuário não existir.
     */
    public void adicionarMembro(String login, Facade facade) {
        if (login == null || login.isEmpty()) {
            throw new IllegalArgumentException("Login do membro não pode ser nulo ou vazio.");
        }
        if (!facade.getUsuarios().containsKey(login)) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        membros.add(login);
    }

    /**
     * Verifica se um usuário é membro da comunidade.
     *
     * @param login O login do usuário a ser verificado.
     * @return True se o usuário é membro, false caso contrário.
     */
    public boolean contemMembro(String login) {
        return membros.contains(login);
    }
}
