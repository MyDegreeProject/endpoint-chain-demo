package com.wust.endpoint.chain.quetsal.configuration;

import com.fluidops.fedx.algebra.StatementSource;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.List;
import java.util.Set;

public interface Summary extends com.fluidops.fedx.Summary {
	/**
	 * CostFed Index lookup for sources values
	 * @return value Set<String> set of endpoint identifiers
	 */
	Set<String> lookupSources(StatementPattern sp);
	Set<String> lookupSources(String s, String p, String o);
	
	Set<String> lookupSbjPrefixes(StatementPattern stmt, String eid);
	Set<String> lookupObjPrefixes(StatementPattern stmt, String eid);
	
	long getTriplePatternCardinality(StatementPattern stmt, List<StatementSource> stmtSrces);
	double getTriplePatternObjectMVKoef(StatementPattern stmt, List<StatementSource> stmtSrces);
	double getTriplePatternSubjectMVKoef(StatementPattern stmt, List<StatementSource> stmtSrces);
	
	RepositoryConnection getConnection();
	void shutDown();
}
