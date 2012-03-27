package br.ufpi.componets;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;

import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.SessionScoped;
import br.ufpi.models.Acao;
import br.ufpi.models.Tarefa;

/**
 * Usado para gravação do fluxo. Quando terminar a gravação do fluxo deleta a
 * seção.
 * 
 * @author Cleiton
 * 
 */
@SessionScoped
@Component
public class SessionActions {
	/**
	 * Lista de ações de um determinado Fluxo
	 */
	private List<Acao> acoes;
	/**
	 * False se nao for a primeira vez que a pagina foi aberta True se for a
	 * primeira vez.
	 */
	private boolean primeiraPagina;

	/**
	 * Url que o browser vai estar
	 */
	//private String urlProxima;

//	public String getUrlproxima() {
//		return urlProxima;
//	}
//
//	public void setUrlProxima(String urlEmAndamento) {
//		this.urlProxima = urlEmAndamento;
//	}

	@PreDestroy
	public void destroy() {
		acoes = null;
	}

	public List<Acao> getAcoes() {
		return acoes;
	}

	public void setAcoes(List<Acao> acoes) {
		this.acoes = acoes;
	}

	public SessionActions() {
		primeiraPagina = true;
	}

	public boolean addAction(List<Acao> actions) {
		if (acoes == null)
			this.acoes = new ArrayList<Acao>();
		return this.acoes.addAll(actions);
	}

	public boolean isPrimeiraPagina() {
		return primeiraPagina;
	}

	public void setPrimeiraPagina(boolean primeiraPagina) {
		this.primeiraPagina = primeiraPagina;
	}

}
