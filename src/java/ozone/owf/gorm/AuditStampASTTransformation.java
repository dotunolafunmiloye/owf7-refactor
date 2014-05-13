package ozone.owf.gorm;

import grails.util.GrailsNameUtils;
import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import ozone.owf.grails.domain.Person;

/**
 * Performs an ast transformation on a class - adds createdBy/createdDate
 * editedBy/EditedDate id and table properties to the subject class.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class AuditStampASTTransformation implements ASTTransformation {

	private static final ConfigObject CO = new ConfigSlurper()
			.parse(getContents(new File("./grails-app/conf/Config.groovy")));
	private static final Properties CONF = new ConfigSlurper().parse(
			getContents(new File("./grails-app/conf/Config.groovy")))
			.toProperties();

	@Override
	public void visit(final ASTNode[] astNodes, final SourceUnit sourceUnit) {

		final String createdByField = CONF.getProperty("stamp.audit.createdBy");
		final String editedByField = CONF.getProperty("stamp.audit.editedBy");
		final String editedDateField = CONF
				.getProperty("stamp.audit.editedDate");
		final String createdDateField = CONF
				.getProperty("stamp.audit.createdDate");

		for (final ASTNode astNode : astNodes) {
			if (astNode instanceof ClassNode) {
				final ClassNode classNode = (ClassNode) astNode;
				if (editedByField != null) {
					classNode.addProperty(editedByField, Modifier.PUBLIC,
							new ClassNode(Person.class),
							ConstantExpression.NULL, null, null);
					addNullableConstraint(classNode, editedByField);
				}
				if (createdByField != null) {
					classNode.addProperty(createdByField, Modifier.PUBLIC,
							new ClassNode(Person.class),
							ConstantExpression.NULL, null, null);
					addNullableConstraint(classNode, createdByField);
				}
				final Expression now = new ConstructorCallExpression(
						new ClassNode(java.util.Date.class),
						MethodCallExpression.NO_ARGUMENTS);
				if (createdDateField != null) {
					classNode.addProperty(createdDateField, Modifier.PUBLIC,
							new ClassNode(java.util.Date.class), now, null,
							null);
					addNullableConstraint(classNode, createdDateField);
				}
				if (editedDateField != null) {
					classNode.addProperty(editedDateField, Modifier.PUBLIC,
							new ClassNode(java.util.Date.class), now, null,
							null);
					addNullableConstraint(classNode, editedDateField);
				}

				classNode.addInterface(new ClassNode(DomainObject.class));
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void addTableAndIdMapping(final ClassNode classNode) {
		final FieldNode closure = classNode.getDeclaredField("mapping");

		if (closure != null) {
			final boolean hasTable = hasFieldInClosure(closure, "table");
			final boolean hasId = hasFieldInClosure(closure, "id");

			final ClosureExpression exp = (ClosureExpression) closure
					.getInitialExpression();
			final BlockStatement block = (BlockStatement) exp.getCode();

			// this just adds an s to the class name for the table if its not
			// specified
			final Boolean pluralize = (Boolean) getMap(CO,
					"stamp.mapping.pluralTable");
			if (!hasTable && pluralize != null && pluralize) {
				final String tablename = GrailsNameUtils.getShortName(classNode
						.getName()) + "s";
				final MethodCallExpression tableMeth = new MethodCallExpression(
						VariableExpression.THIS_EXPRESSION,
						new ConstantExpression("table"),
						new ArgumentListExpression(new ConstantExpression(
								tablename)));
				block.addStatement(new ExpressionStatement(tableMeth));
			}
			// This adds the ID generator that we use for domian classes
			final Map tableconf = (Map) getMap(CO, "stamp.mapping.id");
			if (!hasId && tableconf != null) {
				final NamedArgumentListExpression namedarg = new NamedArgumentListExpression();
				if (tableconf.get("column") != null) {
					namedarg.addMapEntryExpression(new ConstantExpression(
							"column"),
							new ConstantExpression(tableconf.get("column")
									.toString()));
				}
				if (tableconf.get("generator") != null) {
					namedarg.addMapEntryExpression(new ConstantExpression(
							"generator"),
							new ConstantExpression(tableconf.get("generator")
									.toString()));
				}
				final MethodCallExpression tableMeth = new MethodCallExpression(
						VariableExpression.THIS_EXPRESSION,
						new ConstantExpression("id"), namedarg);
				block.addStatement(new ExpressionStatement(tableMeth));
			}
		}
	}

	public void addNullableConstraint(final ClassNode classNode,
			final String fieldName) {
		final FieldNode closure = classNode.getDeclaredField("constraints");

		if (closure != null) {

			final ClosureExpression exp = (ClosureExpression) closure
					.getInitialExpression();
			final BlockStatement block = (BlockStatement) exp.getCode();

			if (!hasFieldInClosure(closure, fieldName)) {
				final NamedArgumentListExpression namedarg = new NamedArgumentListExpression();
				namedarg.addMapEntryExpression(new ConstantExpression(
						"nullable"), new ConstantExpression(true));
				final MethodCallExpression constExpr = new MethodCallExpression(
						VariableExpression.THIS_EXPRESSION,
						new ConstantExpression(fieldName), namedarg);
				block.addStatement(new ExpressionStatement(constExpr));
			}
		}
	}

	public boolean hasFieldInClosure(final FieldNode closure,
			final String fieldName) {
		if (closure != null) {
			final ClosureExpression exp = (ClosureExpression) closure
					.getInitialExpression();
			final BlockStatement block = (BlockStatement) exp.getCode();
			final List<Statement> ments = block.getStatements();
			for (final Statement expstat : ments) {
				if (expstat instanceof ExpressionStatement
						&& ((ExpressionStatement) expstat).getExpression() instanceof MethodCallExpression) {
					final MethodCallExpression methexp = (MethodCallExpression) ((ExpressionStatement) expstat)
							.getExpression();
					final ConstantExpression conexp = (ConstantExpression) methexp
							.getMethod();
					if (conexp.getValue().equals(fieldName)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	static public String getContents(final File aFile) {
		// ...checks on aFile are elided
		final StringBuilder contents = new StringBuilder();

		try {
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			final BufferedReader input = new BufferedReader(new FileReader(
					aFile));
			try {
				String line = null;
				while ((line = input.readLine()) != null) {
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			} finally {
				input.close();
			}
		} catch (final IOException ex) {
			ex.printStackTrace();
		}

		return contents.toString();
	}

	@SuppressWarnings("rawtypes")
	static public Object getMap(final Map configMap, final String keypath) {
		final String keys[] = keypath.split("\\.");
		Map map = configMap;
		for (final String key : keys) {
			final Object val = map.get(key);
			if (val != null) {
				if (val instanceof Map) {
					map = (Map) map.get(key);
				} else {
					return val;
				}
			} else {
				return null;
			}
		}
		return map;
	}

}
