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

            if (esq instanceof ExpMult && dir instanceof ExpMult) {
                ExpMult m1 = (ExpMult) esq;
                ExpMult m2 = (ExpMult) dir;

                Integer c1 = null;
                Integer c2 = null;
                Id x1 = null;
                Id x2 = null;

                // Tenta extrair "coeficiente * variável" de m1
                if (m1.getEsq() instanceof ValorInteiro && m1.getDir() instanceof Id) {
                    c1 = ((ValorInteiro) m1.getEsq()).valor();
                    x1 = (Id) m1.getDir();
                } else if (m1.getDir() instanceof ValorInteiro && m1.getEsq() instanceof Id) {
                    c1 = ((ValorInteiro) m1.getDir()).valor();
                    x1 = (Id) m1.getEsq();
                }

                // Tenta extrair "coeficiente * variável" de m2
                if (m2.getEsq() instanceof ValorInteiro && m2.getDir() instanceof Id) {
                    c2 = ((ValorInteiro) m2.getEsq()).valor();
                    x2 = (Id) m2.getDir();
                } else if (m2.getDir() instanceof ValorInteiro && m2.getEsq() instanceof Id) {
                    c2 = ((ValorInteiro) m2.getDir()).valor();
                    x2 = (Id) m2.getEsq();
                }

                // Se ambos são "coef * x" com o MESMO identificador:
                if (c1 != null && c2 != null && x1 != null && x2 != null &&
                    x1.getIdName().equals(x2.getIdName())) {

                    int coef = c1 + c2;

                    // Tratar casos especiais do coeficiente
                    if (coef == 0) {
                        // 2*x + (-2)*x -> 0
                        return new ValorInteiro(0);
                    } else if (coef == 1) {
                        // 1*x + 0*x -> x 
                        return x1;
                    } else {
                        return new ExpMult(new ValorInteiro(coef), x1);
                    }
                }
            }

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

            // Simplificação adicional: x + x -> 2 * x
            if (esq instanceof Id && dir instanceof Id) {
                Id idEsq = (Id) esq;
                Id idDir = (Id) dir;
                if (idEsq.getIdName().equals(idDir.getIdName())) {
                    // Exemplo: x + x -> 2 * x
                    return new ExpMult(new ValorInteiro(2), esq);
                }
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
