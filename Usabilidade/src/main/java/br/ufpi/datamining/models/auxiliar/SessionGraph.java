package br.ufpi.datamining.models.auxiliar;

import java.util.Map;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedPseudograph;

public class SessionGraph {
	private String description;
	private SessionResultDataMining session;
	private DirectedPseudograph<String, DefaultWeightedEdge> graph;
	private Map<String, String> info;
	
	public SessionGraph(String description, SessionResultDataMining session, DirectedPseudograph<String, DefaultWeightedEdge> graph, Map<String, String> info) {
		super();
		this.description = description;
		this.session = session;
		this.graph = graph;
		this.info = info;
	}
	
	public String getDescription() {
		return description;
	}
	public DirectedPseudograph<String, DefaultWeightedEdge> getGraph() {
		return graph;
	}
	public SessionResultDataMining getSession() {
		return session;
	}
	public Map<String, String> getInfo() {
		return info;
	}
	
}
