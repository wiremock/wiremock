package com.github.tomakehurst.wiremock.jsontemplate;


import java.util.Collection;
import java.util.Iterator;

class RepeatedSectionStatement implements IStatement {

	private Section block;

	public RepeatedSectionStatement(Section block) {
		this.block = block;
	}

	public void execute(ScopedContext context, ITemplateRenderCallback callback) {
		boolean pushed;
		Collection items;
		if ("@".equals(this.block.getSectionName())) {
			// If the name is @, we stay in the enclosing context, but assume it's a
		    // list, and repeat this block many times.
			Object cursorValue = context.getCursorValue();
			if (cursorValue instanceof Collection || cursorValue == null) {
				items = (Collection) cursorValue;
				pushed = false;
			}
			else {
				throw new EvaluationError(String.format("sectionName is '@' but the cursor value is not a collection (is %s)", cursorValue.toString()));
			}
		}
		else {
			Object newContext = context.pushSection(this.block.getSectionName());
			if (newContext instanceof Collection || newContext == null) {
				items = (Collection) newContext;
				pushed = true;
			}
			else {
				throw new EvaluationError(String.format("cursor value for section '%s' is not a collection (is %s)", this.block.getSectionName(), newContext));
			}
		}
		
		if (!context.isEmptyContext(items)) {
			int lastIndex = items.size() - 1;
			int i = 0;
			StatementList statements = this.block.getStatements();
			StatementList altStatements = this.block.getStatements("alternates with");
			for (Iterator iterator = items.iterator(); iterator
					.hasNext();) {
				Object item = iterator.next();
				context.pushObject(item);
				TemplateExecutor.execute(statements, context, callback);
				if (i != lastIndex) {
					TemplateExecutor.execute(altStatements, context, callback);
				}
				context.pop();
				i++;
			}
		}
		else {
			TemplateExecutor.execute(this.block.getStatements("or"), context, callback);
		}
		
		if (pushed) context.pop();
	}

}
