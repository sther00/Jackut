import br.ufal.ic.p2.jackut.models.Facade;
import easyaccept.EasyAccept;

public class Main {

    static {
        System.setProperty("file.encoding", "ISO-8859-1");
        try {
            java.nio.charset.Charset.availableCharsets().get("ISO-8859-1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        System.setProperty("file.encoding", "ISO-8859-1");
        java.nio.charset.Charset.availableCharsets().get("ISO-8859-1");

        Facade facade = new Facade();
        facade.zerarSistema();
        
        for (int i = 1; i <= 9; i++)
        {
            String[] args2 = { "br.ufal.ic.p2.jackut.models.Facade", "tests/us" + i + "_1.txt" };
            String[] args3 = { "br.ufal.ic.p2.jackut.models.Facade", "tests/us" + i + "_2.txt" };
            EasyAccept.main(args2);
            EasyAccept.main(args3);
        }
    }
}

