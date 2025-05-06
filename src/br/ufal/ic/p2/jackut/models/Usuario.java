package br.ufal.ic.p2.jackut.models;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe que representa um usuário na rede Jackut,
 * armazenando informações de login, perfil, amigos, recados e convites.
 */
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String login;
    private final String senha;
    private final String nome;
    private final Perfil perfil;
    private final Set<String> amigos = new LinkedHashSet<>();
    private final Set<String> convitesEnviados = new LinkedHashSet<>();
    private final Set<String> convitesRecebidos = new LinkedHashSet<>();
    private final Queue<String> recadosRecebidos = new LinkedList<>();
    private final Queue<String> mensagensComunidade = new LinkedList<>();


    public Usuario(String login, String senha, String nome) {


        this.login = login;
        this.senha = senha;
        this.nome = nome;
        this.perfil = new Perfil();
    }

    /**
     * Recupera o login do usuário.
     *
     * @return O login do usuário.
     */
    public String getLogin() { return login; }
    public String getSenha() { return senha; }

    /**
     * Recupera o nome do usuário.
     *
     * @return O nome do usuário.
     */
    public String getNome() { return nome; }
    public Perfil getPerfil() { return perfil; }

    public Set<String> getAmigos() { return amigos; }
    public Set<String> getConvitesEnviados() { return convitesEnviados; }
    public Set<String> getConvitesRecebidos() { return convitesRecebidos; }

    public void enviarConvite(String loginAmigo) {
        convitesEnviados.add(loginAmigo);
    }

    public void receberConvite(String loginAmigo) {
        convitesRecebidos.add(loginAmigo);
    }

    /**
     * Aceita um convite de amizade recebido de outro usuário.
     *
     * @param loginAmigo o login de quem enviou o convite
     * @return true se o convite foi aceito com sucesso, false se não havia convite pendente
     */

    public boolean aceitarConvite(String loginAmigo) {
        if (convitesRecebidos.remove(loginAmigo)) {
            amigos.add(loginAmigo);
            return true;
        }
        return false;
    }

    public boolean temConvitePendenteDe(String loginAmigo) {
        return convitesRecebidos.contains(loginAmigo);
    }

    public void receberRecado(String recado) {
        if (recado == null || recado.trim().isEmpty()) {
            return;
        }
        recadosRecebidos.add(EncodingUtil.fixEncoding(recado));
    }

    /**
     * Lê o próximo recado da fila de recados recebidos.
     *
     * @return o texto do recado ou null se não houver mais recados
     */

    public String lerRecado() {
        if (recadosRecebidos == null || recadosRecebidos.isEmpty()) {
            throw new RuntimeException("Não há recados.");
        }
        return recadosRecebidos.poll();
    }

    public Queue<String> getRecadosRecebidos() {
        return recadosRecebidos;
    }


    public boolean temRecados() {
        return !recadosRecebidos.isEmpty();
    }

    private final Set<String> comunidades = new HashSet<>();
    private final Set<String> fas = new HashSet<>();
    private final Set<String> idolos = new HashSet<>();
    private final Set<String> paqueras = new HashSet<>();
    private final Set<String> inimigos = new HashSet<>();

    public Set<String> getComunidades() { return comunidades; }
    public Set<String> getFas() { return fas; }
    public Set<String> getIdolos() { return idolos; }
    public Set<String> getPaqueras() { return paqueras; }
    public Set<String> getInimigos() { return inimigos; }

    public void adicionarComunidade(String comunidade) {
        comunidades.add(comunidade);
    }

    public void adicionarFa(String fa) {
        fas.add(fa);
    }

    public void adicionarIdolo(String idolo) {
        idolos.add(idolo);
    }

    public void adicionarPaquera(String paquera) {
        paqueras.add(paquera);
    }

    public void adicionarInimigo(String inimigo) {
        inimigos.add(inimigo);
    }

    public boolean ehFaDe(String idolo) {
        return idolos.contains(idolo);
    }

    public boolean ehPaqueraDe(String paquera) {
        return paqueras.contains(paquera);
    }

    public boolean ehInimigoDe(String inimigo) {
        return inimigos.contains(inimigo);
    }

    public void receberMensagemComunidade(String mensagem) {
        synchronized (mensagensComunidade) {
            mensagensComunidade.add(mensagem);
        }
    }

    public String lerMensagemComunidade() {
        synchronized (mensagensComunidade) {
            if (mensagensComunidade.isEmpty()) {
                throw new RuntimeException(EncodingUtil.fixEncoding("Não há mensagens."));
            }
            return mensagensComunidade.remove();
        }
    }

    public boolean temMensagensComunidade() {
        return !mensagensComunidade.isEmpty();
    }

    public Queue<String> getMensagensComunidade() {
        return mensagensComunidade;
    }

    /**
     * Remove todos os recados enviados por um determinado usuário
     * @param remetenteLogin login do usuário cujos recados devem ser removidos
     */
    public void removerRecadosDoUsuario(String remetenteLogin) {
        if (remetenteLogin == null) return;

        String prefixoRemetente = remetenteLogin + ":";

        recadosRecebidos.removeIf(recado ->
                recado != null && recado.startsWith(prefixoRemetente));
    }

    public String toText() {
        StringBuilder sb = new StringBuilder();
        sb.append("USUARIO\n");
        sb.append("login=").append(login).append("\n");
        sb.append("senha=").append(senha).append("\n");
        sb.append("nome=").append(nome).append("\n");

        sb.append("atributos=");
        perfil.getAtributos().forEach((k, v) -> sb.append(k).append(":").append(v).append("|"));
        if (!perfil.getAtributos().isEmpty()) sb.setLength(sb.length() - 1);
        sb.append("\n");

        sb.append("recados=");
        boolean temRecadoValido = false;
        for (String r : recadosRecebidos) {
            if (r != null && !r.trim().isEmpty()) {
                sb.append(EncodingUtil.fixEncoding(r)).append("|");
                temRecadoValido = true;
            }
        }
        if (temRecadoValido) sb.setLength(sb.length() - 1);
        sb.append("\n");

        sb.append("amigos=").append(String.join("|", amigos)).append("\n");
        sb.append("convitesEnviados=").append(String.join("|", convitesEnviados)).append("\n");
        sb.append("convitesRecebidos=").append(String.join("|", convitesRecebidos)).append("\n");
        sb.append("comunidades=").append(String.join("|", comunidades)).append("\n");
        sb.append("idolos=").append(String.join("|", idolos)).append("\n");
        sb.append("paqueras=").append(String.join("|", paqueras)).append("\n");
        sb.append("inimigos=").append(String.join("|", inimigos)).append("\n");
        sb.append("fas=").append(String.join("|", fas)).append("\n");
        sb.append("FIM\n");

        return sb.toString();
    }


    public static Usuario fromText(List<String> bloco) {
        bloco = bloco.stream()
                .map(EncodingUtil::fixEncoding)
                .collect(Collectors.toList());
        String login = "", senha = "", nome = "";
        Perfil perfil = new Perfil();
        Queue<String> recados = new LinkedList<>();
        Set<String> amigos = new LinkedHashSet<>();
        Set<String> enviados = new LinkedHashSet<>();
        Set<String> recebidos = new LinkedHashSet<>();
        Set<String> comunidades = new HashSet<>();
        Set<String> idolos = new HashSet<>();
        Set<String> paqueras = new HashSet<>();
        Set<String> inimigos = new HashSet<>();
        Set<String> fas = new HashSet<>();

        for (String linha : bloco) {
            try {
                if (linha.startsWith("login=")) {
                    login = linha.substring(6);
                } else if (linha.startsWith("senha=")) {
                    senha = linha.substring(6);
                } else if (linha.startsWith("nome=")) {
                    nome = linha.substring(5);
                } else if (linha.startsWith("atributos=") && linha.length() > 10) {
                    String[] pares = linha.substring(10).split("\\|");
                    for (String par : pares) {
                        if (par.contains(":")) {
                            String[] kv = par.split(":", 2);
                            perfil.adicionarAtributo(kv[0], kv[1]);
                        }
                    }
                } else if (linha.startsWith("recados=") && linha.length() > 8) {
                    String[] partes = linha.substring(8).split("\\|");
                    for (String r : partes) {
                        if (r != null && !r.trim().isEmpty()) {
                            recados.add(EncodingUtil.fixEncoding(r.trim()));
                        }
                    }
                } else if (linha.startsWith("amigos=") && linha.length() > 7) {
                    Collections.addAll(amigos, linha.substring(7).split("\\|"));
                } else if (linha.startsWith("convitesEnviados=") && linha.length() > 17) {
                    Collections.addAll(enviados, linha.substring(17).split("\\|"));
                } else if (linha.startsWith("convitesRecebidos=") && linha.length() > 19) {
                    Collections.addAll(recebidos, linha.substring(19).split("\\|"));
                } else if (linha.startsWith("comunidades=") && linha.length() > 12) {
                    comunidades.clear();
                    Collections.addAll(comunidades, linha.substring(12).split("\\|"));
                } else if (linha.startsWith("idolos=") && linha.length() > 7) {
                    Collections.addAll(idolos, linha.substring(7).split("\\|"));
                } else if (linha.startsWith("paqueras=") && linha.length() > 9) {
                    Collections.addAll(paqueras, linha.substring(9).split("\\|"));
                } else if (linha.startsWith("inimigos=") && linha.length() > 9) {
                    Collections.addAll(inimigos, linha.substring(9).split("\\|"));
                } else if (linha.startsWith("fas=") && linha.length() > 4) {
                    Collections.addAll(fas, linha.substring(4).split("\\|"));
                }
            } catch (Exception e) {
            }
        }

        Usuario u = new Usuario(login, senha, nome);
        u.getPerfil().getAtributos().putAll(perfil.getAtributos());
        u.getAmigos().addAll(amigos);
        u.getConvitesEnviados().addAll(enviados);
        u.getConvitesRecebidos().addAll(recebidos);
        u.getComunidades().addAll(comunidades);
        u.getIdolos().addAll(idolos);
        u.getPaqueras().addAll(paqueras);
        u.getInimigos().addAll(inimigos);
        u.getFas().addAll(fas);

        for (String r : recados) {
            if (r != null && !r.trim().isEmpty()) {
                u.receberRecado(r);
            }
        }

        return u;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Usuario other = (Usuario) obj;
        return login.equals(other.login);
    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }


}
