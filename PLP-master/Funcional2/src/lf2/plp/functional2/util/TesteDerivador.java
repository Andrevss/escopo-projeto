package lf2.plp.functional2.util;

import lf2.plp.expressions2.expression.Expressao;
import lf2.plp.expressions2.expression.ExpSoma;
import lf2.plp.expressions2.expression.ExpSub;
import lf2.plp.expressions2.expression.Id;
import lf2.plp.expressions2.expression.ValorInteiro;
// Adicionando a funcionalidade da multiplicação
import lf2.plp.expressions2.expression.ExpMult;

public class TesteDerivador {

    public static void main(String[] args) {
        // f(x) = 2*x + 3
        Expressao f =
            // new ExpSoma(
            //     new ExpMult(new Id("x"), new Id("x")),
            //     new ValorInteiro(3)s
            // );
            // Encontrei essa simplificação não feita, o resultado abaixo fica f'(x) = 2*x + 2*x
            new ExpSoma(
                new ExpSoma(
                    new ExpMult(new Id("x"), new Id("x")),
                    new ExpMult(new Id("x"), new Id("x"))
                ),
                new ValorInteiro(3)
            );
        Expressao df = Derivador.derivarESimplificar(f, "x");

        System.out.println("f(x)  = " + f);
        System.out.println("f'(x) = " + df);
    }
}
