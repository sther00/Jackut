package br.ufal.ic.p2.jackut.models;

import java.nio.charset.StandardCharsets;

/**
 * Classe utilitária para manipulação de codificação de caracteres.
 */
public class EncodingUtil {

    /**
     * Converte a codificação de um texto de ISO-8859-1 para UTF-8.
     *
     * @param text O texto a ser convertido.
     * @return O texto convertido ou o próprio texto se houver erro.
     */
    public static String fixEncoding(String text) {
        if (text == null) return null;
        try {
            return new String(text.getBytes("ISO-8859-1"), "UTF-8");
        } catch (Exception e) {
            return text;
        }
    }

    /**
     * Cria uma exceção com a mensagem convertida para UTF-8.
     *
     * @param message A mensagem de erro.
     * @return A exceção gerada.
     */
    public static RuntimeException createException(String message) {
        return new RuntimeException(fixEncoding(message));
    }

    /**
     * Garante que o texto esteja no encoding ISO-8859-1.
     *
     * @param text O texto a ser forçado para ISO-8859-1.
     * @return O texto com codificação forçada.
     */
    public static String forceISO(String text) {
        return fixEncoding(text);
    }
}
