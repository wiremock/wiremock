package com.github.tomakehurst.wiremock.jsontemplate;


import java.util.HashMap;
import java.util.Map;

class Section {
	private String sectionName;
	private StatementList currentClause;
	private Map<String, StatementList> statements;

	public Section(String sectionName) {
		this.sectionName = sectionName;
		this.currentClause = new StatementList();
		this.statements = new HashMap<String, StatementList>();
		this.statements.put("default", this.currentClause);
	}

	public Section() {
		this(null);
	}

	public StatementList getStatements() {
		return this.getStatements("default");
	}

	public StatementList getStatements(String clauseName) {
		if (this.statements.containsKey(clauseName)) {
			return this.statements.get(clauseName);
		}
		return new StatementList();
	}

	public String toString() {
		return "<Block " + sectionName + ">";
	}

	public void append(IStatement statement) {
		this.currentClause.add(statement);
	}

	public String getSectionName() {
		return sectionName;
	}

	public void newClause(String name) {
		StatementList newClause = new StatementList();
		this.statements.put(name, newClause);
		this.currentClause = newClause;
	}
	
	
}
