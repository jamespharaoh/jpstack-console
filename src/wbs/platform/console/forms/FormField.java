package wbs.platform.console.forms;

import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.record.PermanentRecord;
import wbs.platform.console.html.ScriptRef;

public
interface FormField<Container,Generic,Native,Interface> {

	final static
	int defaultSize = 48;

	Boolean virtual ();

	String name ();

	String label ();

	Set<ScriptRef> scriptRefs ();

	void init (
			String fieldSetName);

	void renderTableCell (
			PrintWriter out,
			Container object,
			boolean link);

	void renderFormRow (
			PrintWriter out,
			Container object);

	void update (
			Container container,
			UpdateResult<Generic,Native> updateResult);

	void runUpdateHook (
			UpdateResult<Generic,Native> updateResult,
			Container container,
			PermanentRecord<?> linkObject,
			Object objectRef,
			String objectType);

	@Accessors (fluent = true)
	@Data
	public static
	class UpdateResult<Generic,Native> {

		Boolean updated;

		FormField<?,Generic,Native,?> formField;

		Generic oldGenericValue;
		Generic newGenericValue;

		Native oldNativeValue;
		Native newNativeValue;

		List<String> errors;

	}

}