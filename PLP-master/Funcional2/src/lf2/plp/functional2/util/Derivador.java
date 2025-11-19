package lf2.plp.functional2.util;

import lf2.plp.expressions2.expression.Expressao;
import lf2.plp.expressions2.expression.ValorInteiro;
import lf2.plp.expressions2.expression.Id;
import lf2.plp.expressions2.expression.ExpSoma;
import lf2.plp.expressions2.expression.ExpSub;
import lf2.plp.expressions2.expression.ExpMenos;
import lf2.plp.expressions2.expression.ExpMult;

/**
 * Classe utilitária para realizar derivação simbólica e simplificação de expressões.
 * Todos os métodos são estáticos.
 */
public class Derivador {

    // Construtor privado para evitar instanciação
    private Derivador() {
    }

    // --- Métodos de Entrada Estáticos ---

    /**
     * Ponto de entrada estático: Deriva uma expressão (AST) em relação a 'var'.
     */
    public static Expressao derivar(Expressao e, String var) {
        return derivarRecursivo(e, var);
    }

    /**
     * Ponto de entrada estático: Deriva e simplifica a expressão.
     */
    public static Expressao derivarESimplificar(Expressao e, String var) {
        return simplificarRecursivo(derivar(e, var));
    }

    // --- Lógica Recursiva da Derivação ---

    /**
     * Método interno recursivo que aplica as regras de derivação.
     */
    private static Expressao derivarRecursivo(Expressao e, String var) {
        if (e instanceof ValorInteiro) {
            // d/dx(c) = 0
            return new ValorInteiro(0);
        }

        if (e instanceof Id) {
            Id id = (Id) e;
            // d/dx(x) = 1 ; d/dx(y!=x) = 0
            if (id.getIdName().equals(var)) {
                return new ValorInteiro(1);
            } else {
                return new ValorInteiro(0);
            }
        }

        if (e instanceof ExpSoma) {
            ExpSoma s = (ExpSoma) e;
            // Regra da Soma: Deriva recursivamente
            Expressao dEsq = derivarRecursivo(s.getEsq(), var);
            Expressao dDir = derivarRecursivo(s.getDir(), var);
            return new ExpSoma(dEsq, dDir);
        }

        if (e instanceof ExpSub) {
            ExpSub s = (ExpSub) e;
            // Regra da Subtração
            Expressao dEsq = derivarRecursivo(s.getEsq(), var);
            Expressao dDir = derivarRecursivo(s.getDir(), var);
            return new ExpSub(dEsq, dDir);
        }

        if (e instanceof ExpMenos) {
            ExpMenos m = (ExpMenos) e;
            // Regra do Negativo
            Expressao dExp = derivarRecursivo(m.getExp(), var);
            return new ExpMenos(dExp);
        }

        if (e instanceof ExpMult) {
            ExpMult m = (ExpMult) e;
            Expressao u = m.getEsq();
            Expressao v = m.getDir();

            // Regra do Produto: u' * v + u * v'
            Expressao du = derivarRecursivo(u, var);
            Expressao dv = derivarRecursivo(v, var);

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

    // --- Lógica Recursiva da Simplificação ---

    /**
     * Método interno recursivo que aplica as regras de simplificação.
     */
    private static Expressao simplificarRecursivo(Expressao e) {
        // Casos base: números e ids
        if (e instanceof ValorInteiro || e instanceof Id) {
            return e;
        }

        if (e instanceof ExpSoma) {
            ExpSoma s = (ExpSoma) e;
            // Simplifica subexpressões recursivamente
            Expressao esq = simplificarRecursivo(s.getEsq());
            Expressao dir = simplificarRecursivo(s.getDir());

            // 1. Simplificação de combinação de termos (ex: x + x -> 2*x, 2*x + 3*x -> 5*x)
            if (esq instanceof ExpMult && dir instanceof ExpMult) {
                ExpMult m1 = (ExpMult) esq;
                ExpMult m2 = (ExpMult) dir;

                Integer c1 = null; Integer c2 = null;
                Id x1 = null; Id x2 = null;

                // Extrai coeficientes e IDs
                if (m1.getEsq() instanceof ValorInteiro && m1.getDir() instanceof Id) { c1 = ((ValorInteiro) m1.getEsq()).valor(); x1 = (Id) m1.getDir(); } 
                else if (m1.getDir() instanceof ValorInteiro && m1.getEsq() instanceof Id) { c1 = ((ValorInteiro) m1.getDir()).valor(); x1 = (Id) m1.getEsq(); }
                
                if (m2.getEsq() instanceof ValorInteiro && m2.getDir() instanceof Id) { c2 = ((ValorInteiro) m2.getEsq()).valor(); x2 = (Id) m2.getDir(); } 
                else if (m2.getDir() instanceof ValorInteiro && m2.getEsq() instanceof Id) { c2 = ((ValorInteiro) m2.getDir()).valor(); x2 = (Id) m2.getEsq(); }

                if (c1 != null && c2 != null && x1 != null && x2 != null && x1.getIdName().equals(x2.getIdName())) {
                    int coef = c1 + c2;
                    if (coef == 0) return new ValorInteiro(0);
                    if (coef == 1) return x1;
                    return new ExpMult(new ValorInteiro(coef), x1);
                }
            }

            // 2. Regras de Adição com Zero
            if (esq instanceof ValorInteiro && ((ValorInteiro) esq).valor() == 0) return dir;
            if (dir instanceof ValorInteiro && ((ValorInteiro) dir).valor() == 0) return esq;

            // 3. Soma de Constantes
            if (esq instanceof ValorInteiro && dir instanceof ValorInteiro) {
                int v = ((ValorInteiro) esq).valor() + ((ValorInteiro) dir).valor();
                return new ValorInteiro(v);
            }

            // 4. x + x -> 2 * x (IDs simples)
            if (esq instanceof Id && dir instanceof Id && ((Id) esq).getIdName().equals(((Id) dir).getIdName())) {
                return new ExpMult(new ValorInteiro(2), esq);
            }

            return new ExpSoma(esq, dir);
        }

        if (e instanceof ExpSub) {
            ExpSub s = (ExpSub) e;
            Expressao esq = simplificarRecursivo(s.getEsq());
            Expressao dir = simplificarRecursivo(s.getDir());

            // 1. Regras de Subtração com Zero
            if (dir instanceof ValorInteiro && ((ValorInteiro) dir).valor() == 0) return esq;
            if (esq instanceof ValorInteiro && ((ValorInteiro) esq).valor() == 0) return new ExpMenos(dir);

            // 2. Subtração de Constantes
            if (esq instanceof ValorInteiro && dir instanceof ValorInteiro) {
                int v = ((ValorInteiro) esq).valor() - ((ValorInteiro) dir).valor();
                return new ValorInteiro(v);
            }

            return new ExpSub(esq, dir);
        }

        if (e instanceof ExpMenos) {
            ExpMenos m = (ExpMenos) e;
            Expressao exp = simplificarRecursivo(m.getExp());

            // 1. Regra do -0 e -(-e)
            if (exp instanceof ValorInteiro && ((ValorInteiro) exp).valor() == 0) return new ValorInteiro(0);
            if (exp instanceof ExpMenos) return simplificarRecursivo(((ExpMenos) exp).getExp());

            return new ExpMenos(exp);
        }

        if (e instanceof ExpMult) {
            ExpMult m = (ExpMult) e;
            Expressao esq = simplificarRecursivo(m.getEsq());
            Expressao dir = simplificarRecursivo(m.getDir());

            // 1. Regras de Multiplicação por Zero
            if (esq instanceof ValorInteiro && ((ValorInteiro) esq).valor() == 0) return new ValorInteiro(0);
            if (dir instanceof ValorInteiro && ((ValorInteiro) dir).valor() == 0) return new ValorInteiro(0);

            // 2. Regras de Multiplicação por Um
            if (esq instanceof ValorInteiro && ((ValorInteiro) esq).valor() == 1) return dir;
            if (dir instanceof ValorInteiro && ((ValorInteiro) dir).valor() == 1) return esq;

            // 3. Multiplicação de Constantes
            if (esq instanceof ValorInteiro && dir instanceof ValorInteiro) {
                int v = ((ValorInteiro) esq).valor() * ((ValorInteiro) dir).valor();
                return new ValorInteiro(v);
            }

            return new ExpMult(esq, dir);
        }

        return e;
    }
}