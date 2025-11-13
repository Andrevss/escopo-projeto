package lf2.plp.functional2.util;

import lf2.plp.expressions2.expression.Expressao;
import lf2.plp.expressions2.expression.ValorInteiro;
import lf2.plp.expressions2.expression.Id;
import lf2.plp.expressions2.expression.ExpSoma;
import lf2.plp.expressions2.expression.ExpSub;
import lf2.plp.expressions2.expression.ExpMenos;
// Incluir a funcionalidade da multiplicação
import lf2.plp.expressions2.expression.ExpMult;

/**
 * Classe utilitária para realizar derivação simbólica de expressões
 * da linguagem de Expressoes2 em relação a uma variável.
 *
 * Fragmento suportado:
 *  - constantes inteiras: ValorInteiro
 *  - identificadores: Id
 *  - soma: ExpSoma
 *  - subtração binária: ExpSub
 *  - menos unário: ExpMenos
 */
public class Derivador {

    /**
     * Nome da variável em relação à qual derivamos (por exemplo "x").
     */
    private final String var;

    public Derivador(String var) {
        this.var = var;
    }

    /**
     * Deriva uma expressão e retorna a expressão resultante (sem simplificar).
     */
    public Expressao derivar(Expressao e) {
        if (e instanceof ValorInteiro) {
            // d/dx(c) = 0
            return new ValorInteiro(0);
        }

        if (e instanceof Id) {
            Id id = (Id) e;
            // d/dx(x) = 1 ; d/dx(y!=x) = 0
            if (id.getIdName().equals(this.var)) {
                return new ValorInteiro(1);
            } else {
                return new ValorInteiro(0);
            }
        }

        if (e instanceof ExpSoma) {
            ExpSoma s = (ExpSoma) e;
            Expressao dEsq = derivar(s.getEsq());
            Expressao dDir = derivar(s.getDir());
            return new ExpSoma(dEsq, dDir);
        }

        if (e instanceof ExpSub) {
            ExpSub s = (ExpSub) e;
            Expressao dEsq = derivar(s.getEsq());
            Expressao dDir = derivar(s.getDir());
            return new ExpSub(dEsq, dDir);
        }

        if (e instanceof ExpMenos) {
            ExpMenos m = (ExpMenos) e;
            Expressao dExp = derivar(m.getExp());
            // d/dx(-e) = -(d/dx(e))
            return new ExpMenos(dExp);
        }
        // Adição da Funcionalidade de Multiplicação:
        if (e instanceof ExpMult) {
            ExpMult m = (ExpMult) e;
            Expressao u  = m.getEsq();
            Expressao v  = m.getDir();
            Expressao du = derivar(u);
            Expressao dv = derivar(v);

            // (u * v)' = u' * v + u * v'
            return new ExpSoma(
                new ExpMult(du, v),
                new ExpMult(u, dv)
            );
        }

        // Qualquer outra expressão ainda não é suportada
        throw new IllegalArgumentException(
            "Derivada nao definida para a classe: " + e.getClass().getName()
        );
    }

    /**
     * Deriva e em seguida simplifica a expressão obtida.
     */
    public Expressao derivarESimplificar(Expressao e) {
        return simplificar(derivar(e));
    }

    /**
     * Atalho estático: Derivador.derivar(expr, "x")
     */
    public static Expressao derivar(Expressao e, String var) {
        return new Derivador(var).derivar(e);
    }

    /**
     * Atalho estático: Derivador.derivarESimplificar(expr, "x")
     */
    public static Expressao derivarESimplificar(Expressao e, String var) {
        return new Derivador(var).derivarESimplificar(e);
    }

    /**
     * Simplifica uma expressão do fragmento {ValorInteiro, Id, ExpSoma, ExpSub, ExpMenos}.
     * Aplica recursivamente simplificações como:
     *  - 0 + e = e
     *  - e + 0 = e
     *  - 0 - e = -e
     *  - e - 0 = e
     *  - soma/sub de constantes: 1 + 1 = 2
     *  - -0 = 0
     *  - -(-e) = e
     */
    private Expressao simplificar(Expressao e) {
        // Casos base: números e ids
        if (e instanceof ValorInteiro || e instanceof Id) {
            return e;
        }

        if (e instanceof ExpSoma) {
            ExpSoma s = (ExpSoma) e;
            Expressao esq = simplificar(s.getEsq());
            Expressao dir = simplificar(s.getDir());

            // 0 + e -> e
            if (esq instanceof ValorInteiro &&
                ((ValorInteiro) esq).valor() == 0) {
                return dir;
            }

            // e + 0 -> e
            if (dir instanceof ValorInteiro &&
                ((ValorInteiro) dir).valor() == 0) {
                return esq;
            }

            // Soma de constantes: c1 + c2 -> (c1+c2)
            if (esq instanceof ValorInteiro && dir instanceof ValorInteiro) {
                int v = ((ValorInteiro) esq).valor()
                      + ((ValorInteiro) dir).valor();
                return new ValorInteiro(v);
            }

            return new ExpSoma(esq, dir);
        }

        if (e instanceof ExpSub) {
            ExpSub s = (ExpSub) e;
            Expressao esq = simplificar(s.getEsq());
            Expressao dir = simplificar(s.getDir());

            // e - 0 -> e
            if (dir instanceof ValorInteiro &&
                ((ValorInteiro) dir).valor() == 0) {
                return esq;
            }

            // 0 - e -> -e
            if (esq instanceof ValorInteiro &&
                ((ValorInteiro) esq).valor() == 0) {
                return new ExpMenos(dir);
            }

            // Subtração de constantes: c1 - c2 -> (c1-c2)
            if (esq instanceof ValorInteiro && dir instanceof ValorInteiro) {
                int v = ((ValorInteiro) esq).valor()
                      - ((ValorInteiro) dir).valor();
                return new ValorInteiro(v);
            }

            return new ExpSub(esq, dir);
        }

        if (e instanceof ExpMenos) {
            ExpMenos m = (ExpMenos) e;
            Expressao exp = simplificar(m.getExp());

            // -0 -> 0
            if (exp instanceof ValorInteiro &&
                ((ValorInteiro) exp).valor() == 0) {
                return new ValorInteiro(0);
            }

            // -(-e) -> e
            if (exp instanceof ExpMenos) {
                ExpMenos inner = (ExpMenos) exp;
                return simplificar(inner.getExp());
            }

            // Caso geral: -(exp simplificado)
            return new ExpMenos(exp);
        }

        // Acrescentar a simplificação da multiplicaçao

        if (e instanceof ExpMult) {
            ExpMult m = (ExpMult) e;
            Expressao esq = simplificar(m.getEsq());
            Expressao dir = simplificar(m.getDir());

            // 0 * e -> 0
            if (esq instanceof ValorInteiro &&
                ((ValorInteiro) esq).valor() == 0) {
                return new ValorInteiro(0);
            }

            // e * 0 -> 0
            if (dir instanceof ValorInteiro &&
                ((ValorInteiro) dir).valor() == 0) {
                return new ValorInteiro(0);
            }

            // 1 * e -> e
            if (esq instanceof ValorInteiro &&
                ((ValorInteiro) esq).valor() == 1) {
                return dir;
            }

            // e * 1 -> e
            if (dir instanceof ValorInteiro &&
                ((ValorInteiro) dir).valor() == 1) {
                return esq;
            }

            // c1 * c2 -> (c1*c2)
            if (esq instanceof ValorInteiro && dir instanceof ValorInteiro) {
                int v = ((ValorInteiro) esq).valor()
                    * ((ValorInteiro) dir).valor();
                return new ValorInteiro(v);
            }

            return new ExpMult(esq, dir);
        }

        // Caso apareça algum outro tipo que não tratamos aqui
        return e;
    }
}
