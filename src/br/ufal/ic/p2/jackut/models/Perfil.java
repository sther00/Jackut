package br.ufal.ic.p2.jackut.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe que representa os atributos de perfil personalizáveis
 * de um usuário da rede Jackut.
 */
public class Perfil implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, String> atributos = new HashMap<>();

    /**
     * Adiciona ou atualiza um atributo do perfil do usuário.
     *
     * @param chave O nome do atributo.
     * @param valor O valor do atributo.
     * @throws IllegalArgumentException Se a chave ou o valor forem nulos ou vazios.
     */
    public void adicionarAtributo(String chave, String valor) {
        if (chave == null || chave.trim().isEmpty()) {
            throw new IllegalArgumentException("A chave do atributo não pode ser nula ou vazia.");
        }
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("O valor do atributo não pode ser nulo ou vazio.");
        }
        atributos.put(chave, valor);
    }

    /**
     * Recupera o valor de um atributo do perfil.
     *
     * @param chave O nome do atributo.
     * @return O valor do atributo ou null se não encontrado.
     */
    public String getAtributo(String chave) {
        return atributos.get(chave);
    }

    /**
     * Retorna todos os atributos do perfil.
     *
     * @return O mapa de atributos.
     */
    public Map<String, String> getAtributos() {
        return atributos;
    }
}
