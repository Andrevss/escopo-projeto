package lf2.plp.functional2.util;

import lf2.plp.expressions2.expression.Expressao;
import lf2.plp.expressions2.expression.ExpSoma;
import lf2.plp.expressions2.expression.ExpMult;
import lf2.plp.expressions2.expression.Id;
import lf2.plp.expressions2.expression.Valor;
import lf2.plp.expressions2.expression.ValorInteiro;

import lf2.plp.expressions2.memory.AmbienteExecucao;
import lf2.plp.expressions2.memory.ContextoExecucao;
import lf2.plp.expressions2.memory.VariavelJaDeclaradaException;
import lf2.plp.expressions2.memory.VariavelNaoDeclaradaException;

import lf2.plp.functional2.expression.ExpDeriv;

/**
 * Classe de teste para ExpDeriv.
 *
 * Objetivos:
 *  1) Montar expressões da linguagem (AST) diretamente em Java.
 *  2) Usar o Derivador + ExpDeriv para obter a derivada simbólica.
 *  3) Avaliar numericamente o valor da derivada em um ponto, usando
 *     o mesmo AmbienteExecucao (ContextoExecucao) que a Funcional2 usa.
 *
 * Sobre a ExpDeriv:
 *  - ExpDeriv implementa Expressao.
 *  - Internamente, ExpDeriv guarda:
 *        - uma Expressao 'funcao'   → f(x)
 *        - um Id 'variavel'        → "x" (variável em relação à qual derivamos)
 *  - No método avaliar(amb), ExpDeriv faz:
 *        1. Chama Derivador.derivarESimplificar(funcao, variavel.getIdName())
 *           → obtém uma nova Expressao representando f'(x), já simplificada.
 *        2. Chama avaliar(amb) sobre essa expressão derivada.
 *           → devolve um Valor (por exemplo, ValorInteiro) com f'(x) avaliado
 *             no ambiente corrente (onde x já está mapeado).
 *
 * Assim, do ponto de vista do usuário, ExpDeriv se comporta como uma expressão
 * qualquer, mas "embutindo" o processo de derivação simbólica antes de avaliar.
 */
public class TesteExpDeriv {

    public static void main(String[] args) {

        try {
            System.out.println("==== Teste de ExpDeriv ====\n");

            /*
             * -------------------------------------------------------------
             * 1) Definição de f(x) = x * x + 3
             *
             * Montamos a árvore de expressão (AST) usando as classes já
             * existentes em lf2.plp.expressions2.expression:
             *
             *   ExpSoma( ExpMult(Id("x"), Id("x")), ValorInteiro(3) )
             *
             * Esta é a forma "em Java" de representar a expressão da linguagem:
             *
             *   x * x + 3
             * -------------------------------------------------------------
             */
            Expressao f =
                new ExpSoma(
                    new ExpMult(new Id("x"), new Id("x")),
                    new ValorInteiro(3)
                );

            System.out.println("Função f(x):      " + f);

            /*
             * Derivada simbólica:
             *
             * Aqui usamos diretamente o Derivador para obter f'(x), já
             * simplificada. Isso nos permite verificar se a derivada
             * simbólica está coerente com o que esperamos:
             *
             *   f(x)   = x * x + 3
             *   f'(x)  = 2 * x
             */
            Expressao dfSimp = Derivador.derivarESimplificar(f, "x");
            System.out.println("Derivada f'(x):   " + dfSimp);

            /*
             * -------------------------------------------------------------
             * 2) Criação do AmbienteExecucao
             *
             * Usamos ContextoExecucao, que é a implementação concreta de
             * AmbienteExecucao para a Expressions2 na Funcional2.
             *
             * - incrementa() abre um novo escopo (novo "frame" de variáveis).
             * - map(Id, Valor) associa um identificador a um valor nesse escopo.
             *
             * Aqui vamos representar o ponto x = 2 no ambiente, para depois
             * avaliar a derivada nesse ponto.
             * -------------------------------------------------------------
             */
            AmbienteExecucao amb = new ContextoExecucao();
            amb.incrementa(); // abre um novo escopo

            // Define x = 2 no ambiente
            amb.map(new Id("x"), new ValorInteiro(2));

            /*
             * -------------------------------------------------------------
             * 3) ExpDeriv: expressão que representa "f'(x)"
             *
             * Criamos uma ExpDeriv passando:
             *    - a expressão f (x * x + 3)
             *    - o identificador da variável em relação à qual derivamos: "x"
             *
             * Internamente, no método avaliar(amb), ExpDeriv vai:
             *   1) Derivar simbolicamente 'f' em relação a "x"
             *      → Derivador.derivarESimplificar(f, "x")
             *   2) Avaliar a expressão derivada no próprio 'amb'
             *      → derivada.avaliar(amb)
             * -------------------------------------------------------------
             */
            Expressao df = new ExpDeriv(f, new Id("x"));

            /*
             * Ao chamar avaliar(amb), o que acontece na prática é:
             *
             *  - dentro de ExpDeriv:
             *       Expressao derivada = Derivador.derivarESimplificar(f, "x");
             *       return derivada.avaliar(amb);
             *
             *  - como no ambiente temos x = 2, o valor retornado será f'(2).
             */
            Valor v = df.avaliar(amb);
            System.out.println("f'(2) avaliado:   " + v + "\n");


            /*
             * -------------------------------------------------------------
             * 4) Segundo teste: g(x) = x * x + x * x + 3
             *
             * Aqui queremos exercitar um caso com repetição de termos
             * para observar:
             *   - a derivada simbólica antes/depois de simplificar
             *   - o valor numérico da derivada em x = 2
             *
             * g(x) = x * x + x * x + 3
             *      = 2 * x * x + 3
             *
             * g'(x) esperado (antes de simplificar tudo):
             *      = 2 * x + 2 * x
             *
             * Com simplificação:
             *      = 4 * x
             * -------------------------------------------------------------
             */
            Expressao g =
                new ExpSoma(
                    new ExpSoma(
                        new ExpMult(new Id("x"), new Id("x")),
                        new ExpMult(new Id("x"), new Id("x"))
                    ),
                    new ValorInteiro(3)
                );

            System.out.println("Função g(x):      " + g);

            // Derivada simbólica de g(x) em relação a x
            Expressao dgSimp = Derivador.derivarESimplificar(g, "x");
            System.out.println("Derivada g'(x):   " + dgSimp);

            /*
             * Criamos outra ExpDeriv, agora para g(x):
             *   ExpDeriv(g, x)
             *
             * Novamente, o avaliar(amb) vai:
             *   - derivar simbolicamente g em relação a x
             *   - avaliar g'(x) no mesmo ambiente (x = 2)
             */
            Expressao dg = new ExpDeriv(g, new Id("x"));
            Valor v2 = dg.avaliar(amb);
            System.out.println("g'(2) avaliado:   " + v2);

        } catch (VariavelNaoDeclaradaException | VariavelJaDeclaradaException e) {
            // Captura problemas de variáveis no ambiente (não declarada / duplicada)
            System.out.println("Erro de variável no ambiente: " + e);
            e.printStackTrace();
        } catch (Exception e) {
            // Captura qualquer outra exceção inesperada
            System.out.println("Erro inesperado: " + e);
            e.printStackTrace();
        }
    }
}
