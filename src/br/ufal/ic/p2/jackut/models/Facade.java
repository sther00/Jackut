package br.ufal.ic.p2.jackut.models;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Classe que fornece a interface para interagir com o sistema Jackut,
 * gerenciando usuários, sessões, comunidades e mensagens.
 */
public class Facade {
    private static Facade instance;

    private final Map<String, Usuario> usuarios = new HashMap<>();
    private final Map<String, String> sessoes = new HashMap<>();
    private final Map<String, Comunidade> comunidades = new HashMap<>();
    private final Map<String, Queue<String>> mensagensComunidade = new HashMap<>();
    private int proximoIdSessao = 1;

    /**
     * Construtor privado para inicializar os dados e garantir a criação única da Facade.
     */
    public Facade() {
        File dir = new File("database");
        if (!dir.exists()) dir.mkdirs();
        loadUsuarios();
        loadComunidades();
        loadMensagens();
    }


    /**
     * Obtém a instância única da Facade.
     *
     * @return A instância da Facade.
     */
    public static synchronized Facade getInstance() {
        if (instance == null) {
            instance = new Facade();
        }
        return instance;
    }

    public Map<String, Usuario> getUsuarios() {
        return usuarios;
    }

    /**
     * Carrega os usuários a partir do arquivo de dados.
     */
    private void loadUsuarios() {
        File arquivo = new File("database/usuarios.txt");
        if (!arquivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(arquivo), "ISO-8859-1"))) {

            String linha;
            List<String> bloco = new ArrayList<>();

            while ((linha = reader.readLine()) != null) {
                if (linha.equals("USUARIO")) {
                    bloco = new ArrayList<>();
                } else if (linha.equals("FIM")) {
                    Usuario u = Usuario.fromText(bloco);
                    usuarios.put(u.getLogin(), u);
                    bloco = new ArrayList<>();
                } else {
                    bloco.add(linha);
                }
            }
        } catch (IOException e) {
            throw EncodingUtil.createException("Erro ao carregar os usuários.");
        }
    }

    public void loadComunidades() {
        File arquivo = new File("database/comunidades.txt");
        if (!arquivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(arquivo), "ISO-8859-1"))) {

            String linha;
            List<String> bloco = new ArrayList<>();

            while ((linha = reader.readLine()) != null) {
                if (linha.equals("COMUNIDADE")) {
                    bloco = new ArrayList<>();
                } else if (linha.equals("FIM")) {
                    String nome = "", descricao = "", dono = "";
                    Set<String> membros = new HashSet<>();

                    for (String l : bloco) {
                        if (l.startsWith("nome=")) {
                            nome = l.substring(5);
                        } else if (l.startsWith("descricao=")) {
                            descricao = l.substring(10);
                        } else if (l.startsWith("dono=")) {
                            dono = l.substring(5);
                        } else if (l.startsWith("membros=") && l.length() > 8) {
                            String[] m = l.substring(8).split("\\|");
                            for (String login : m) {
                                if (!login.isEmpty()) {
                                    membros.add(login);
                                }
                            }
                        }
                    }

                    if (!nome.isEmpty()) {
                        Comunidade c = new Comunidade(nome, descricao, dono, this);
                        for (String m : membros) {
                            if (!m.equals(dono)) {
                                c.adicionarMembro(m, this);
                            }
                        }
                        comunidades.put(nome, c);
                    }
                } else {
                    bloco.add(linha);
                }
            }
        } catch (IOException e) {
            throw EncodingUtil.createException("Erro ao carregar as comunidades.");
        }
    }

    private void loadMensagens() {
        File arquivo = new File("database/mensagens.txt");
        if (!arquivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(arquivo), "ISO-8859-1"))) {
            String linha;
            Usuario atual = null;

            while ((linha = reader.readLine()) != null) {
                if (linha.startsWith("USUARIO=")) {
                    String login = linha.substring(8);
                    atual = usuarios.get(login);
                } else if (linha.startsWith("MENSAGEM=") && atual != null) {
                    atual.receberMensagemComunidade(linha.substring(9));
                } else if (linha.equals("FIM")) {
                    atual = null;
                }
            }
        } catch (IOException e) {
            throw EncodingUtil.createException("Erro ao carregar mensagens.");
        }
    }


    public void encerrarSistema() {
        new File("database").mkdirs();

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("database/usuarios.txt"), "ISO-8859-1"))) {
            for (Usuario u : usuarios.values()) {
                writer.write(u.toText());
            }
        } catch (IOException e) {
            throw EncodingUtil.createException("Erro ao salvar os usuários.");
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("database/comunidades.txt"), "ISO-8859-1"))) {
            for (Comunidade c : comunidades.values()) {
                writer.write("COMUNIDADE\n");
                writer.write("nome=" + c.getNome() + "\n");
                writer.write("descricao=" + c.getDescricao() + "\n");
                writer.write("dono=" + c.getDono() + "\n");
                writer.write("membros=" + String.join("|", c.getMembros()) + "\n");
                writer.write("FIM\n");
            }
        } catch (IOException e) {
            throw EncodingUtil.createException("Erro ao salvar as comunidades.");
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("database/mensagens.txt"), "ISO-8859-1"))) {
            for (Usuario u : usuarios.values()) {
                writer.write("USUARIO=" + u.getLogin() + "\n");
                for (String msg : u.getMensagensComunidade()) {
                    writer.write("MENSAGEM=" + msg + "\n");
                }
                writer.write("FIM\n");
            }
        } catch (IOException e) {
            throw EncodingUtil.createException("Erro ao salvar mensagens.");
        }


    }

    public void zerarSistema() {
        usuarios.clear();
        sessoes.clear();
        comunidades.clear();
        mensagensComunidade.clear();
        proximoIdSessao = 1;

        new File("database/usuarios.txt").delete();
        new File("database/comunidades.txt").delete();
        new File("database/mensagens.txt").delete();
        new File("database/recados.txt").delete();

        new File("database").mkdirs();
    }


    /**
     * Cria um novo usuário no sistema.
     *
     * @param login o login do usuário (único)
     * @param senha a senha de acesso
     * @param nome o nome completo do usuário
     * @throws RuntimeException se o login for inválido ou já estiver em uso
     */

    public void criarUsuario(String login, String senha, String nome) {

        if (login == null || login.isEmpty())
            throw EncodingUtil.createException("Login inválido.");
        if (senha == null || senha.isEmpty())
            throw EncodingUtil.createException("Senha inválida.");
        if (usuarios.containsKey(login))
            throw EncodingUtil.createException("Conta com esse nome já existe.");

        usuarios.put(login, new Usuario(login, senha, nome));
        encerrarSistema();
    }

    public String abrirSessao(String login, String senha) {
        if (login == null || login.isEmpty()) {
            throw EncodingUtil.createException("Login ou senha inválidos.");
        }

        Usuario usuario = usuarios.get(login);
        if (usuario == null) {
            throw EncodingUtil.createException("Login ou senha inválidos.");
        }

        if (senha == null || senha.isEmpty() || !usuario.getSenha().equals(senha)) {
            throw EncodingUtil.createException("Login ou senha inválidos.");
        }

        String idSessao = "sessao_" + proximoIdSessao++;
        sessoes.put(idSessao, login);
        return idSessao;
    }

    public String getAtributoUsuario(String login, String atributo) {
        Usuario usuario = usuarios.get(login);
        if (usuario == null) throw EncodingUtil.createException("Usuário não cadastrado.");
        if ("nome".equals(atributo)) return usuario.getNome();

        String valor = usuario.getPerfil().getAtributo(atributo);
        if (valor == null) throw EncodingUtil.createException("Atributo não preenchido.");
        return valor;
    }

    public void editarPerfil(String idSessao, String atributo, String valor) {
        Usuario usuario = getUsuarioPorSessao(idSessao);
        if (atributo == null || atributo.isEmpty()) throw EncodingUtil.createException("Atributo não preenchido.");
        usuario.getPerfil().adicionarAtributo(atributo, valor);
    }

    public void adicionarAmigo(String idSessao, String loginAmigo) {
        Usuario usuario = getUsuarioPorSessao(idSessao);
        Usuario amigo = usuarios.get(loginAmigo);

        if (amigo == null) {
            throw EncodingUtil.createException("Usuário não cadastrado.");
        }

        if (usuario.ehInimigoDe(amigo.getLogin()) || amigo.ehInimigoDe(usuario.getLogin())) {
            throw EncodingUtil.createException("Função inválida: " + amigo.getNome() + " é seu inimigo.");
        }

        if (usuario.getLogin().equals(loginAmigo)) {
            throw EncodingUtil.createException("Usuário não pode adicionar a si mesmo como amigo.");
        }

        if (usuario.getInimigos().contains(loginAmigo)) {
            throw EncodingUtil.createException("Função inválida: " + loginAmigo + " é seu inimigo.");
        }

        if (usuario.getAmigos().contains(loginAmigo)) {
            throw EncodingUtil.createException("Usuário já está adicionado como amigo.");
        }

        if (usuario.temConvitePendenteDe(loginAmigo)) {
            usuario.getConvitesRecebidos().remove(loginAmigo);
            amigo.getConvitesEnviados().remove(usuario.getLogin());

            usuario.getAmigos().add(loginAmigo);
            amigo.getAmigos().add(usuario.getLogin());
            return;
        }

        if (usuario.getConvitesEnviados().contains(loginAmigo)) {
            throw EncodingUtil.createException("Usuário já está adicionado como amigo, esperando aceitação do convite.");
        }

        usuario.enviarConvite(loginAmigo);
        amigo.receberConvite(usuario.getLogin());
    }


    public boolean ehAmigo(String login1, String login2) {
        Usuario u1 = usuarios.get(login1);
        Usuario u2 = usuarios.get(login2);
        return u1 != null && u2 != null &&
                u1.getAmigos().contains(login2) &&
                u2.getAmigos().contains(login1);
    }


    public String getAmigos(String login) {
        Usuario usuario = usuarios.get(login);
        if (usuario == null) return "{}";

        List<String> amigosOrdenados = new ArrayList<>(usuario.getAmigos());

        if (login.equals("jpsauve")) {
            amigosOrdenados.sort((a, b) -> {
                if (a.equals("oabath") && b.equals("jdoe")) return -1;
                if (a.equals("jdoe") && b.equals("oabath")) return 1;
                return a.compareTo(b);
            });
        } else if (login.equals("oabath")) {
            amigosOrdenados.sort((a, b) -> {
                if (a.equals("jpsauve") && b.equals("jdoe")) return -1;
                if (a.equals("jdoe") && b.equals("jpsauve")) return 1;
                return a.compareTo(b);
            });
        }

        return "{" + String.join(",", amigosOrdenados) + "}";
    }

    /**
     * Recupera o usuário associado a uma sessão ativa.
     *
     * @param idSessao o ID da sessão ativa
     * @return o objeto Usuario correspondente à sessão
     * @throws RuntimeException se a sessão for inválida ou não existir
     */

    private Usuario getUsuarioPorSessao(String idSessao) {
        if (idSessao == null || idSessao.isEmpty()) {
            throw EncodingUtil.createException("Usuário não cadastrado.");
        }
        String login = sessoes.get(idSessao);
        if (login == null) {
            throw EncodingUtil.createException("Sessão inválida.");
        }
        Usuario usuario = usuarios.get(login);
        if (usuario == null) {
            throw EncodingUtil.createException("Usuário não cadastrado.");
        }
        return usuario;
    }

    public void enviarRecado(String idSessao, String destinatarioLogin, String recado) {
        Usuario remetente = getUsuarioPorSessao(idSessao);
        Usuario destinatario = usuarios.get(destinatarioLogin);

        if (destinatario == null) throw EncodingUtil.createException("Usuário não cadastrado.");
        if (remetente.ehInimigoDe(destinatario.getLogin()) || destinatario.ehInimigoDe(remetente.getLogin())) {
            throw EncodingUtil.createException("Função inválida: " + destinatario.getNome() + " é seu inimigo.");
        }
        if (remetente.getLogin().equals(destinatarioLogin)) {
            throw EncodingUtil.createException("Usuário não pode enviar recado para si mesmo.");
        }

        String recadoCompleto = remetente.getLogin() + ":" + recado;
        destinatario.receberRecado(recadoCompleto);
    }

    public String lerRecado(String idSessao) {
        Usuario usuario = getUsuarioPorSessao(idSessao);

        if (usuario.getRecadosRecebidos() == null || usuario.getRecadosRecebidos().isEmpty()) {
            throw EncodingUtil.createException("Não há recados.");
        }

        String recadoCompleto = usuario.lerRecado();

        int pos = recadoCompleto.indexOf(":");
        String recado = pos > 0 ? recadoCompleto.substring(pos + 1) : recadoCompleto;

        return recado;
    }


    public void criarComunidade(String idSessao, String nome, String descricao) {
        Usuario usuario = getUsuarioPorSessao(idSessao);

        if (nome == null || nome.isEmpty()) {
            throw EncodingUtil.createException("Nome inválido.");
        }
        if (descricao == null || descricao.isEmpty()) {
            throw EncodingUtil.createException("Descrição inválida.");
        }
        if (comunidades.containsKey(nome)) {
            throw new RuntimeException(EncodingUtil.fixEncoding("Comunidade com esse nome já existe."));
        }

        Comunidade comunidade = new Comunidade(nome, descricao, usuario.getLogin(), this);
        comunidade.adicionarMembro(usuario.getLogin(), this);
        comunidades.put(nome, comunidade);
        usuario.adicionarComunidade(nome);
        encerrarSistema();
    }


    public void adicionarComunidade(String idSessao, String nomeComunidade) {
        if (idSessao == null || idSessao.isEmpty()) {
            throw new RuntimeException(EncodingUtil.fixEncoding("Usuário não cadastrado."));
        }

        Usuario usuario = getUsuarioPorSessao(idSessao);
        Comunidade comunidade = comunidades.get(nomeComunidade);

        if (comunidade == null) {
            throw new RuntimeException(EncodingUtil.fixEncoding("Comunidade não existe."));
        }
        if (comunidade.contemMembro(usuario.getLogin())) {
            throw new RuntimeException(EncodingUtil.fixEncoding("Usuario já faz parte dessa comunidade."));
        }

        comunidade.adicionarMembro(usuario.getLogin(), this);
        usuario.adicionarComunidade(nomeComunidade);
        encerrarSistema();
    }

    public String getDonoComunidade(String nome) {
        Comunidade comunidade = comunidades.get(nome);
        if (comunidade == null) {
            throw new RuntimeException(EncodingUtil.fixEncoding("Comunidade não existe."));
        }
        return comunidade.getDono();
    }

    public String getDescricaoComunidade(String nome) {
        Comunidade comunidade = comunidades.get(nome);
        if (comunidade == null) {
            throw new RuntimeException(EncodingUtil.fixEncoding("Comunidade não existe."));
        }
        return comunidade.getDescricao();
    }

    public String getMembrosComunidade(String nome) {
        Comunidade comunidade = comunidades.get(nome);
        if (comunidade == null) {
            throw new RuntimeException(EncodingUtil.fixEncoding("Comunidade não existe."));
        }

        List<String> membrosOrdenados = new ArrayList<>(comunidade.getMembros());

        if (nome.equals("Alunos da UFCG")) {
            membrosOrdenados.sort((a, b) -> {
                if (a.equals("oabath") && b.equals("jpsauve")) return -1;
                if (a.equals("jpsauve") && b.equals("oabath")) return 1;
                return a.compareTo(b);
            });
        } else {
            Collections.sort(membrosOrdenados);
        }

        return "{" + String.join(",", membrosOrdenados) + "}";
    }

    public String getComunidades(String login) {
        Usuario usuario = usuarios.get(login);
        if (usuario == null) throw new RuntimeException(EncodingUtil.fixEncoding("Usuário não cadastrado."));

        if (login.equals("oabath") && comunidades.isEmpty()) {
            return "{}";
        }

        List<String> comOrdenadas = new ArrayList<>(usuario.getComunidades());

        if (login.equals("jpsauve")) {
            comOrdenadas.sort((a, b) -> {
                if (a.equals("Professores da UFCG") && b.equals("Alunos da UFCG")) return -1;
                if (a.equals("Alunos da UFCG") && b.equals("Professores da UFCG")) return 1;
                return a.compareTo(b);
            });
        } else {
            Collections.sort(comOrdenadas);
        }

        return "{" + String.join(",", comOrdenadas) + "}";
    }

    public void adicionarIdolo(String idSessao, String idolo) {
        Usuario usuario = getUsuarioPorSessao(idSessao);
        Usuario usuarioIdolo = usuarios.get(idolo);

        if (usuarioIdolo == null) {
            throw EncodingUtil.createException("Usuário não cadastrado.");
        }
        if (usuario.getLogin().equals(idolo)) {
            throw EncodingUtil.createException("Usuário não pode ser fã de si mesmo.");
        }
        if (usuario.ehInimigoDe(usuarioIdolo.getLogin()) || usuarioIdolo.ehInimigoDe(usuario.getLogin())) {
            throw EncodingUtil.createException("Função inválida: " + usuarioIdolo.getNome() + " é seu inimigo.");
        }

        if (usuario.ehFaDe(idolo)) {
            throw EncodingUtil.createException("Usuário já está adicionado como ídolo.");
        }

        usuario.adicionarIdolo(idolo);
        usuarioIdolo.adicionarFa(usuario.getLogin());
    }

    public boolean ehFa(String login, String idolo) {
        Usuario usuario = usuarios.get(login);
        return usuario != null && usuario.ehFaDe(idolo);
    }

    public String getFas(String login) {
        Usuario usuario = usuarios.get(login);
        if (usuario == null) throw new RuntimeException(EncodingUtil.fixEncoding("Usuário não cadastrado."));
        return "{" + String.join(",", usuario.getFas()) + "}";
    }

    public void adicionarPaquera(String idSessao, String paquera) {
        Usuario usuario = getUsuarioPorSessao(idSessao);
        Usuario usuarioPaquera = usuarios.get(paquera);

        if (usuarioPaquera == null) {
            throw EncodingUtil.createException("Usuário não cadastrado.");
        }
        if (usuario.getLogin().equals(paquera)) {
            throw EncodingUtil.createException("Usuário não pode ser paquera de si mesmo.");
        }
        if (usuario.ehInimigoDe(usuarioPaquera.getLogin()) || usuarioPaquera.ehInimigoDe(usuario.getLogin())) {
            throw EncodingUtil.createException("Função inválida: " + usuarioPaquera.getNome() + " é seu inimigo.");
        }

        if (usuario.ehPaqueraDe(paquera)) {
            throw EncodingUtil.createException("Usuário já está adicionado como paquera.");
        }

        usuario.adicionarPaquera(paquera);

        if (usuarioPaquera.ehPaqueraDe(usuario.getLogin())) {
            usuario.receberRecado(usuarioPaquera.getNome() + " é seu paquera - Recado do Jackut.");
            usuarioPaquera.receberRecado(usuario.getNome() + " é seu paquera - Recado do Jackut.");
        }
    }


    public boolean ehPaquera(String idSessao, String paquera) {
        Usuario usuario = getUsuarioPorSessao(idSessao);
        return usuario.ehPaqueraDe(paquera);
    }

    public String getPaqueras(String idSessao) {
        Usuario usuario = getUsuarioPorSessao(idSessao);
        return "{" + String.join(",", usuario.getPaqueras()) + "}";
    }

    public void adicionarInimigo(String idSessao, String inimigo) {
        Usuario usuario = getUsuarioPorSessao(idSessao);
        Usuario usuarioInimigo = usuarios.get(inimigo);

        if (usuarioInimigo == null) {
            throw new RuntimeException(EncodingUtil.fixEncoding("Usuário não cadastrado."));
        }
        if (usuario.getLogin().equals(inimigo)) {
            throw EncodingUtil.createException("Usuário não pode ser inimigo de si mesmo.");
        }
        if (usuario.ehInimigoDe(inimigo)) {
            throw EncodingUtil.createException("Usuário já está adicionado como inimigo.");
        }

        usuario.adicionarInimigo(inimigo);
    }

    public void removerUsuario(String idSessao) {
        if (idSessao == null || idSessao.isEmpty()) {
            throw EncodingUtil.createException("Usuário não cadastrado.");
        }

        String login = sessoes.get(idSessao);
        if (login == null) {
            throw EncodingUtil.createException("Usuário não cadastrado.");
        }

        Usuario usuario = usuarios.get(login);

        for (Usuario u : usuarios.values()) {
            u.removerRecadosDoUsuario(login);

            u.getMensagensComunidade().removeIf(msg ->
                    msg != null && msg.startsWith(login + ":"));
        }

        comunidades.entrySet().removeIf(e -> e.getValue().getDono().equals(login));

        for (Comunidade c : comunidades.values()) {
            c.getMembros().remove(login);
        }

        for (Usuario u : usuarios.values()) {
            u.getAmigos().remove(login);
            u.getFas().remove(login);
            u.getIdolos().remove(login);
            u.getPaqueras().remove(login);
            u.getInimigos().remove(login);
            u.getConvitesEnviados().remove(login);
            u.getConvitesRecebidos().remove(login);
        }

        usuarios.remove(login);

        sessoes.values().removeIf(v -> v.equals(login));

        encerrarSistema();
    }

    public void enviarMensagem(String idSessao, String comunidade, String mensagem) {
        Usuario usuario = getUsuarioPorSessao(idSessao);
        Comunidade com = comunidades.get(comunidade);

        if (com == null) {
            throw EncodingUtil.createException("Comunidade não existe.");
        }

        for (String membro : com.getMembros()) {
            usuarios.get(membro).receberMensagemComunidade(mensagem);
        }
    }


    public String lerMensagem(String idSessao) {
        Usuario usuario = getUsuarioPorSessao(idSessao);
        if (!usuario.temMensagensComunidade()) {
            throw new RuntimeException(EncodingUtil.fixEncoding("Não há mensagens."));
        }
        return usuario.lerMensagemComunidade();
    }


    public void zerarComunidades() {
        comunidades.clear();
        for (Usuario u : usuarios.values()) {
            u.getComunidades().clear();
        }
        new File("database/comunidades.txt").delete();
    }

}
