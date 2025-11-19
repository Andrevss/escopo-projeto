package lf2.plp.functional2.expression;

import java.util.Collections;
import java.util.List;

import lf2.plp.expressions1.util.Tipo;
import lf2.plp.expressions1.util.TipoPrimitivo;
import lf2.plp.expressions2.expression.Expressao;
import lf2.plp.expressions2.expression.Id;
import lf2.plp.expressions2.expression.Valor;
import lf2.plp.expressions2.memory.AmbienteCompilacao;
import lf2.plp.expressions2.memory.AmbienteExecucao;
import lf2.plp.expressions2.memory.VariavelJaDeclaradaException;
import lf2.plp.expressions2.memory.VariavelNaoDeclaradaException;
import lf2.plp.functional1.util.TipoFuncao;
import lf2.plp.functional2.util.Derivador;
import lf2.plp.functional2.expression.ValorFuncao;
/**
 * Expressão que representa a avaliação da derivada simbólica de uma função
 * em relação a uma variável.
 *
 * Semanticamente: ExpDeriv(f, x) retorna a AST de f'(x) no ambiente corrente.
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
        
        // Pega o corpo original da função a ser derivada.
        Expressao corpoOriginal = this.funcao; 
        
        Expressao corpoParaDerivar;
        if (corpoOriginal instanceof ValorFuncao) {
            // Caso: let fun f x = derive x*x by x in f(3)
            // Se realizar a redução do corpo original antes, o a variável x será substituída pelo valor dela (Ex: 3).
            // Por isso, precisamos extrair o corpo da função diretamente do ValorFuncao.
            ValorFuncao vf = (ValorFuncao) corpoOriginal;
            corpoParaDerivar = vf.getCorpo();
        } else {
            // Caso: derive x*x + 1 by x (AST normal)
            corpoParaDerivar = corpoOriginal;
        }

        Expressao derivadaAST =
            Derivador.derivarESimplificar(corpoParaDerivar, variavel.getIdName());

        // Resolve as constantes na AST derivada (Ex: x = 3, f(3)).
        Expressao ResultadoFinal = derivadaAST.reduzir(amb);

        // Caso: Para let f x = x*x in derive f by x
        // Por não ter feito a redução do corpo original antes, o expDeriv ver apenas Id(f).
        // Por isso, o resultado da derivada seria 0, o que está incorreto.
        // Neste caso, temos reduzir o corpo original primeiro.
        if (ResultadoFinal instanceof lf2.plp.expressions2.expression.ValorInteiro && 
            ((lf2.plp.expressions2.expression.ValorInteiro) ResultadoFinal).valor() == 0) {
            
            Expressao corpoReduzido = this.funcao.reduzir(amb); 

            ValorFuncao vf2 = (ValorFuncao) corpoReduzido;
            Expressao corpoParaDerivar2 = vf2.getCorpo();
            
            derivadaAST =
                Derivador.derivarESimplificar(corpoParaDerivar2, variavel.getIdName());
            
            ResultadoFinal = derivadaAST.reduzir(amb);
        }
        
        // A variável de derivação ("x") se torna o parâmetro formal da nova função.
        List<Id> parametros = Collections.singletonList(this.variavel);
        
        // Retorna a função derivada como um ValorFuncao
        return new ValorFuncao(parametros, ResultadoFinal);
    }


    
    @Override
    public boolean checaTipo(AmbienteCompilacao amb)
            throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {

        // Verifica se a função é bem tipada.
        if (!funcao.checaTipo(amb)) {
            return false;
        }
        
        // Pega o tipo de resultado da função: INTEIRO ou STRING.
        Tipo tipoCorpo = funcao.getTipo(amb);
        
        //  Queremos que seja inteiro INTEIRO
        if (tipoCorpo.eIgual(TipoPrimitivo.INTEIRO)) { 
            return true;
        }

        return false;
    }

    @Override
    public Tipo getTipo(AmbienteCompilacao amb)
        throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
        
        // Define o Tipo do Domínio 
        // A variável de derivação ("x") é assumida ser INTEIRO para que a derivada seja válida.
        Tipo tipoDominio = TipoPrimitivo.INTEIRO;
        
        // Cria a lista de Tipos do Domínio (mesmo que seja apenas um parâmetro)
        List<Tipo> dominio = Collections.singletonList(tipoDominio);
        
        // Define o Tipo da Imagem 
        Tipo tipoImagem = TipoPrimitivo.INTEIRO; 

        // Retorna a assinatura completa como um TipoFuncao ([Inteiro] -> Inteiro)
        return new TipoFuncao(dominio, tipoImagem);
    }

    @Override
    public Expressao reduzir(AmbienteExecucao amb) {
        try {
            // Chamamos o avaliar para gerar o ValorFuncao.
            // Isso é necessário para cumprir o contrato da interface Expressao
            // e para garantir que a expressão 'derive...' seja executada (avaliada)
            // quando o fluxo do interpretador exigir uma redução.
            
            Valor resultadoAvaliado = this.avaliar(amb);
            
            return (Expressao) resultadoAvaliado; 
            
        } catch (VariavelNaoDeclaradaException | VariavelJaDeclaradaException e) {
            // Se a avaliação falhar, retorna a expressão original.
            return this;
        }
    }

    @Override
    public ExpDeriv clone() {
        return new ExpDeriv(funcao.clone(), (Id) variavel.clone());
    }

    @Override
    public String toString() {
        return "derive(" + funcao.toString() + " by " + variavel.toString() + ")";
    }
}