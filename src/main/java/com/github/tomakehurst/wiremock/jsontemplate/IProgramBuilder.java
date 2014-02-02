package com.github.tomakehurst.wiremock.jsontemplate;


public interface IProgramBuilder {

	public Section getRoot();

	public void append(IStatement statement);

	public void appendSubstitution(String name, String... formatters);

	public void newSection(boolean repeated, String sectionName);

	public void newClause(String token);

	public void endSection();

}
