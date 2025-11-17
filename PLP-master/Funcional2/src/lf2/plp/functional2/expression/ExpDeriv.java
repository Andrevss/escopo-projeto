package lf2.plp.functional2.expression;

import lf2.plp.expressions1.util.Tipo;
import lf2.plp.expressions1.util.TipoPrimitivo;
import lf2.plp.expressions2.expression.Expressao;
import lf2.plp.expressions2.expression.Id;
import lf2.plp.expressions2.expression.Valor;
import lf2.plp.expressions2.memory.AmbienteCompilacao;
import lf2.plp.expressions2.memory.AmbienteExecucao;
import lf2.plp.expressions2.memory.VariavelJaDeclaradaException;
import lf2.plp.expressions2.memory.VariavelNaoDeclaradaException;
import lf2.plp.functional2.util.Derivador;

/**
 * Expressão que representa a avaliação da derivada de uma função
 * em relação a uma variável.
 *
 * Semanticamente: ExpDeriv(f, x) avalia f'(x) no ambiente corrente.
 */
public class ExpDeriv implements Expressao {

    private Expressao funcao;
    private Id variavel;

    public ExpDeriv(Expressao funcao, Id variavel) {
        this.funcao = funcao;
        this.variavel = variavel;
    }

    public Expressao getFuncao() {
        return funcao;
    }

    public Id getVariavel() {
        return variavel;
    }

    @Override
    public Valor avaliar(AmbienteExecucao amb)
            throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {

        // 1) Deriva simbolicamente f em relação a "x"
        Expressao derivada =
            Derivador.derivarESimplificar(funcao, variavel.getIdName());

        // 2) Avalia a expressão derivada no mesmo ambiente
        return derivada.avaliar(amb);
    }

    @Override
    public boolean checaTipo(AmbienteCompilacao amb)
            throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {

        // Assumimos que f é bem-tipada e resulta em inteiro; logo, f' também.
        return funcao.checaTipo(amb);
    }

    @Override
    public Tipo getTipo(AmbienteCompilacao amb)
            throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {

        // Derivamos expressões inteiras -> resultado inteiro
        return TipoPrimitivo.INTEIRO;
    }

    @Override
    public Expressao reduzir(AmbienteExecucao amb) {
        try {
            // Redução: deriva, simplifica e reduz a derivada
            Expressao derivada =
                Derivador.derivarESimplificar(funcao, variavel.getIdName());
            return derivada.reduzir(amb);
        } catch (VariavelNaoDeclaradaException | VariavelJaDeclaradaException e) {
            // Se algo der errado, mantém como está
            return this;
        }
    }

    @Override
    public ExpDeriv clone() {
        // Clone profundo (funcao e variavel também são Expressao/Id clonáveis)
        return new ExpDeriv(funcao.clone(), (Id) variavel.clone());
    }

    @Override
    public String toString() {
        return "deriv(" + funcao.toString() + ", " + variavel.toString() + ")";
    }
}