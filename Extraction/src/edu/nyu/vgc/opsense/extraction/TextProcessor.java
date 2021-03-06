package edu.nyu.vgc.opsense.extraction;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.jgrapht.DirectedGraph;

import edu.nyu.vgc.opsense.extraction.GraphBuilder.RelationshipEdge;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.TypedDependency;

public class TextProcessor {
	Parser parser;
	
	public TextProcessor(String modelsDir){
		System.out.println("TextProcessor: " + modelsDir);
		parser = new Parser(modelsDir);
	}
	
	public JsonArray process(String text, String method){
		JsonArray result = null;
		if(method == null || method.equals("Dependency"))
			result = getJSON(parser.selectRelations(text));
		else if(method.equals("Distance"))
			result = getJSON(parser.selectRelationsByDistance(text,5));
		return result;
	}
	
	private JsonArray getJSON(List<TypedDependency> relations) {
		JsonArrayBuilder result = Json.createArrayBuilder();
		relations.forEach(rln -> {
			JsonObjectBuilder relation = Json.createObjectBuilder();
			relation.add("r", rln.reln().getShortName());
			relation.add("p", rln.gov().tag() + " " + rln.dep().tag());
			relation.add("pr", rln.dep().tag() + " " + rln.gov().tag());
			relation.add("g", indexedWordToJson(rln.gov()));
			relation.add("d", indexedWordToJson(rln.dep()));
			result.add(relation.build());
		});
		
		return result.build();
	}

	public JsonObject getJSONGraph(DirectedGraph<IndexedWord, RelationshipEdge<IndexedWord>> graph){
		JsonObjectBuilder builder = Json.createObjectBuilder();
		JsonArrayBuilder vertices = Json.createArrayBuilder();
		JsonArrayBuilder edges = Json.createArrayBuilder();
		graph.vertexSet().forEach(v -> {
			vertices.add(indexedWordToJson(v));
		});
		graph.edgeSet().forEach(e ->{
			edges.add(EdgeToJson(e));
		});
		
		builder.add("vertex",vertices);
		builder.add("edges",edges);
		return builder.build();
	}
	
	public JsonObject indexedWordToJson(IndexedWord word){
		return Json.createObjectBuilder()
				.add("wd", word.word())
				.add("lm", word.lemma())
				.add("sr", word.beginPosition())
				.add("ed", word.endPosition())
				.add("tg", word.tag())
				.add("st", word.sentIndex())
				.add("ix", word.index())
				.build();
	}
	public JsonObject EdgeToJson(RelationshipEdge<IndexedWord> e){
		return Json.createObjectBuilder()
				.add("gS", e.gov().sentIndex())
				.add("gI", e.gov().index())
				.add("dS", e.dep().sentIndex())
				.add("dI", e.dep().index())
				.add("rl", e.label())
				.build();
	}
	
	
}
