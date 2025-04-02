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
     * @param chave o nome do atributo (ex: "descricao", "cidadeNatal")
     * @param valor o valor do atributo a ser definido
     */

    public void adicionarAtributo(String chave, String valor) {
        if (chave == null || chave.isEmpty()) {
            throw new RuntimeException("Atributo não preenchido.");
        }
        atributos.put(chave, valor);
    }

    public String getAtributo(String chave) {
        return atributos.get(chave);
    }

    // 🔧 Adicione este método para dar acesso aos atributos:
    public Map<String, String> getAtributos() {
        return atributos;
    }
}
