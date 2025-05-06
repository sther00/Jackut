package br.ufal.ic.p2.jackut.models;

import java.nio.charset.StandardCharsets;

/**
 * Classe utilit�ria para manipula��o de codifica��o de caracteres.
 */
public class EncodingUtil {

    /**
     * Converte a codifica��o de um texto de ISO-8859-1 para UTF-8.
     *
     * @param text O texto a ser convertido.
     * @return O texto convertido ou o pr�prio texto se houver erro.
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
     * Cria uma exce��o com a mensagem convertida para UTF-8.
     *
     * @param message A mensagem de erro.
     * @return A exce��o gerada.
     */
    public static RuntimeException createException(String message) {
        return new RuntimeException(fixEncoding(message));
    }

    /**
     * Garante que o texto esteja no encoding ISO-8859-1.
     *
     * @param text O texto a ser for�ado para ISO-8859-1.
     * @return O texto com codifica��o for�ada.
     */
    public static String forceISO(String text) {
        return fixEncoding(text);
    }
}
