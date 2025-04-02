package br.ufal.ic.p2.jackut.models;

import java.io.Serializable;
import java.util.*;

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

    public Usuario(String login, String senha, String nome) {
        this.login = login;
        this.senha = senha;
        this.nome = nome;
        this.perfil = new Perfil();
    }

    public String getLogin() { return login; }
    public String getSenha() { return senha; }
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
        recadosRecebidos.add(recado);
    }

    /**
     * Lê o próximo recado da fila de recados recebidos.
     *
     * @return o texto do recado ou null se não houver mais recados
     */

    public String lerRecado() {
        return recadosRecebidos.poll();
    }

    public boolean temRecados() {
        return !recadosRecebidos.isEmpty();
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
        for (String r : recadosRecebidos) sb.append(r).append("|");
        if (!recadosRecebidos.isEmpty()) sb.setLength(sb.length() - 1);
        sb.append("\n");

        sb.append("amigos=").append(String.join("|", amigos)).append("\n");
        sb.append("convitesEnviados=").append(String.join("|", convitesEnviados)).append("\n");
        sb.append("convitesRecebidos=").append(String.join("|", convitesRecebidos)).append("\n");
        sb.append("FIM\n");
        return sb.toString();
    }

    public static Usuario fromText(List<String> bloco) {
        String login = "", senha = "", nome = "";
        Perfil perfil = new Perfil();
        Queue<String> recados = new LinkedList<>();
        Set<String> amigos = new LinkedHashSet<>();
        Set<String> enviados = new LinkedHashSet<>();
        Set<String> recebidos = new LinkedHashSet<>();

        for (String linha : bloco) {
            if (linha.startsWith("login=")) {
                login = linha.length() > 6 ? linha.substring(6) : "";
            } else if (linha.startsWith("senha=")) {
                senha = linha.length() > 6 ? linha.substring(6) : "";
            } else if (linha.startsWith("nome=")) {
                nome = linha.length() > 5 ? linha.substring(5) : "";
            } else if (linha.startsWith("atributos=")) {
                if (linha.length() > 10) {
                    String dados = linha.substring(10);
                    if (!dados.isBlank()) {
                        String[] pares = dados.split("\\|");
                        for (String par : pares) {
                            if (!par.isBlank() && par.contains(":")) {
                                String[] kv = par.split(":", 2);
                                perfil.adicionarAtributo(kv[0], kv[1]);
                            }
                        }
                    }
                }
            } else if (linha.startsWith("recados=")) {
                if (linha.length() > 8) {
                    String dados = linha.substring(8);
                    if (!dados.isBlank()) {
                        recados.addAll(Arrays.asList(dados.split("\\|")));
                    }
                }
            } else if (linha.startsWith("amigos=")) {
                if (linha.length() > 7) {
                    String dados = linha.substring(7);
                    if (!dados.isBlank()) {
                        amigos.addAll(Arrays.asList(dados.split("\\|")));
                    }
                }
            } else if (linha.startsWith("convitesEnviados=")) {
                if (linha.length() > 17) {
                    String dados = linha.substring(17);
                    if (!dados.isBlank()) {
                        enviados.addAll(Arrays.asList(dados.split("\\|")));
                    }
                }
            } else if (linha.startsWith("convitesRecebidos=")) {
                if (linha.length() > 19) {
                    String dados = linha.substring(19);
                    if (!dados.isBlank()) {
                        recebidos.addAll(Arrays.asList(dados.split("\\|")));
                    }
                }
            }
        }

        Usuario u = new Usuario(login, senha, nome);
        u.getPerfil().getAtributos().putAll(perfil.getAtributos());
        u.getAmigos().addAll(amigos);
        u.getConvitesEnviados().addAll(enviados);
        u.getConvitesRecebidos().addAll(recebidos);
        for (String r : recados) {
            u.receberRecado(r);
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
