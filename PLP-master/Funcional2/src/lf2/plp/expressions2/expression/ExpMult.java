package lf2.plp.expressions2.expression;

import lf2.plp.expressions1.util.Tipo;
import lf2.plp.expressions2.memory.AmbienteCompilacao;
import lf2.plp.expressions2.memory.AmbienteExecucao;
import lf2.plp.expressions2.memory.VariavelNaoDeclaradaException;

/**
 * Expressão de multiplicação inteira: esq * dir
 */
public class ExpMult implements Expressao {

    private Expressao esq;
    private Expressao dir;

    public ExpMult(Expressao esq, Expressao dir) {
        this.esq = esq;
        this.dir = dir;
    }

    public Expressao getEsq() {
        return esq;
    }

    public Expressao getDir() {
        return dir;
    }

    /**
     * Avalia a multiplicação no ambiente.
     * Assumimos que ambos os operandos avaliam para ValorInteiro.
     */
    public Valor avaliar(AmbienteExecucao ambiente)
            throws VariavelNaoDeclaradaException {

        Valor v1 = esq.avaliar(ambiente);
        Valor v2 = dir.avaliar(ambiente);

        int i1 = ((ValorInteiro) v1).valor();
        int i2 = ((ValorInteiro) v2).valor();

        return new ValorInteiro(i1 * i2);
    }

    /**
     * Verificação de tipos: é válida se ambos os operandos forem bem tipados.
     * (Aqui estamos sendo tão permissivos quanto ExpSoma/ExpSub.)
     */
    public boolean checaTipo(AmbienteCompilacao amb)
            throws VariavelNaoDeclaradaException {

        boolean t1 = esq.checaTipo(amb);
        boolean t2 = dir.checaTipo(amb);
        return t1 && t2;
    }

    /**
     * Retorna o tipo da expressão.
     * Segue o padrão de outras expressões binárias: delega para a esquerda.
     */
    public Tipo getTipo(AmbienteCompilacao amb)
            throws VariavelNaoDeclaradaException {

        return esq.getTipo(amb);
    }

    @Override
    public String toString() {
        return esq.toString() + " * " + dir.toString();
    }

    /**
     * Redução: tenta reduzir os operandos; se ambos forem inteiros,
     * calcula o produto diretamente.
     */
    public Expressao reduzir(AmbienteExecucao ambiente) {
        try {
            Expressao rEsq = esq.reduzir(ambiente);
            Expressao rDir = dir.reduzir(ambiente);

            if (rEsq instanceof ValorInteiro && rDir instanceof ValorInteiro) {
                int i1 = ((ValorInteiro) rEsq).valor();
                int i2 = ((ValorInteiro) rDir).valor();
                return new ValorInteiro(i1 * i2);
            }

            return new ExpMult(rEsq, rDir);
        } catch (VariavelNaoDeclaradaException e) {
            // Se der problema na redução, devolvemos a própria expressão
            return this;
        }
    }

    /**
     * Clone superficial, seguindo o padrão usado em Id.
     * (Se quiser um clone profundo, pode fazer new ExpMult(esq.clone(), dir.clone());)
     */
    public ExpMult clone() {
        return this;
    }
}