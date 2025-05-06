package br.ufal.ic.p2.jackut.models;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Representa uma comunidade dentro do sistema Jackut.
 * A comunidade possui um nome, descri��o, dono e membros.
 */
public class Comunidade implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nome;
    private final String descricao;
    private final String dono;
    private final Set<String> membros = new HashSet<>();
    private final Facade facade;

    /**
     * Construtor que inicializa a comunidade com nome, descri��o, dono e a inst�ncia da Facade.
     *
     * @param nome O nome da comunidade.
     * @param descricao A descri��o da comunidade.
     * @param dono O login do usu�rio dono da comunidade.
     * @param facade Inst�ncia da Facade para acessar o sistema.
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
     * Adiciona um novo membro � comunidade.
     *
     * @param login O login do membro a ser adicionado.
     * @param facade Inst�ncia da Facade para validar o usu�rio.
     * @throws IllegalArgumentException Se o login for inv�lido ou o usu�rio n�o existir.
     */
    public void adicionarMembro(String login, Facade facade) {
        if (login == null || login.isEmpty()) {
            throw new IllegalArgumentException("Login do membro n�o pode ser nulo ou vazio.");
        }
        if (!facade.getUsuarios().containsKey(login)) {
            throw new IllegalArgumentException("Usu�rio n�o encontrado.");
        }
        membros.add(login);
    }

    /**
     * Verifica se um usu�rio � membro da comunidade.
     *
     * @param login O login do usu�rio a ser verificado.
     * @return True se o usu�rio � membro, false caso contr�rio.
     */
    public boolean contemMembro(String login) {
        return membros.contains(login);
    }
}
