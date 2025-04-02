import easyaccept.EasyAccept;

public class Main {
    public static void main(String[] args) {

        for (int i = 1; i <= 4; i++)
        {
            String[] args2 = { "br.ufal.ic.p2.jackut.models.Facade", "tests/us" + i + "_1.txt" };
            String[] args3 = { "br.ufal.ic.p2.jackut.models.Facade", "tests/us" + i + "_2.txt" };
            EasyAccept.main(args2);
            EasyAccept.main(args3);
        }
    }
}

